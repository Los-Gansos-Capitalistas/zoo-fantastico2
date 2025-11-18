package com.zoo.zoofantastico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.zoofantastico.dto.ZoneSummaryDTO;
import com.zoo.zoofantastico.dto.request.CreateZoneRequest;
import com.zoo.zoofantastico.dto.request.UpdateZoneRequest;
import com.zoo.zoofantastico.exception.ApiExceptionHandler;
import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.service.ZoneService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(ZoneController.class)
@Import(ApiExceptionHandler.class)
class ZoneControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;

    @MockBean private ZoneService service;

    // ---------------------------
    // GET /api/zones  (summary)
    // ---------------------------
    @Test
    void get_allZones_returns200_andList() throws Exception {
        List<ZoneSummaryDTO> list = List.of(
            ZoneSummaryDTO.builder().id(1L).name("Bosque").description("F").capacity(80).creaturesCount(2L).build(),
            ZoneSummaryDTO.builder().id(2L).name("Desierto").description("A").capacity(50).creaturesCount(0L).build()
        );
        Mockito.when(service.findSummary()).thenReturn(list);

        mvc.perform(get("/api/zones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("Bosque")))
            .andExpect(jsonPath("$[0].creaturesCount", is(2)))
            .andExpect(jsonPath("$[1].name", is("Desierto")));
    }

    // Si además mantienes /api/zones/summary:
    @Test
    void get_summary_returns200_andList() throws Exception {
        List<ZoneSummaryDTO> list = List.of(
            ZoneSummaryDTO.builder().id(1L).name("Bosque").description("F").capacity(80).creaturesCount(2L).build()
        );
        Mockito.when(service.findSummary()).thenReturn(list);

        mvc.perform(get("/api/zones/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Bosque")));
    }

    // ---------------------------
    // GET /api/zones/{id}
    // ---------------------------
    @Test
    void get_byId_returns200_withDTO() throws Exception {
        Zone z = new Zone();
        z.setId(10L);
        z.setName("Bosque");
        z.setDescription("Frondosa");
        z.setCapacity(80);

        Mockito.when(service.findOne(10L)).thenReturn(z);

        mvc.perform(get("/api/zones/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(10)))
            .andExpect(jsonPath("$.name", is("Bosque")))
            .andExpect(jsonPath("$.description", is("Frondosa")))
            .andExpect(jsonPath("$.capacity", is(80)));
    }

    @Test
    void get_byId_returns404_whenNotFound() throws Exception {
        Mockito.when(service.findOne(999L)).thenThrow(new ResourceNotFoundException("Zone not found: 999"));

        mvc.perform(get("/api/zones/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", anyOf(is("Not Found"), is("NOT_FOUND"))))
            .andExpect(jsonPath("$.message", containsString("Zone not found")));
    }

    // ---------------------------
    // POST /api/zones
    // ---------------------------
    @Test
    void post_createZone_returns201_andDTO() throws Exception {
        CreateZoneRequest body = new CreateZoneRequest();
        body.setName("Bosque");
        body.setDescription("Frondosa");
        body.setCapacity(80);

        Zone saved = new Zone();
        saved.setId(10L); saved.setName("Bosque");
        saved.setDescription("Frondosa"); saved.setCapacity(80);
        Mockito.when(service.create(any(CreateZoneRequest.class))).thenReturn(saved);

        mvc.perform(post("/api/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/zones/10"))
            .andExpect(jsonPath("$.id", is(10)))
            .andExpect(jsonPath("$.name", is("Bosque")))
            .andExpect(jsonPath("$.description", is("Frondosa")))
            .andExpect(jsonPath("$.capacity", is(80)));
    }

    @Test
void post_createZone_400_whenNameBlank() throws Exception {
    CreateZoneRequest body = new CreateZoneRequest();
    body.setName(" "); // inválido
    body.setCapacity(10);

    Mockito.when(service.create(any(CreateZoneRequest.class)))
           .thenThrow(new IllegalArgumentException("Zone name is required"));

    mvc.perform(post("/api/zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(body)))
        .andExpect(status().isBadRequest())
        // cambio aquí: mensaje general fijo
        .andExpect(jsonPath("$.message").value("Validation failed"))
        // y validación específica en fieldErrors para el campo 'name'
        .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')].message").value(hasItem("Zone name must not be blank")));
}

    @Test
    void post_createZone_409_whenNameDuplicated() throws Exception {
        CreateZoneRequest body = new CreateZoneRequest();
        body.setName("Bosque"); body.setDescription("F"); body.setCapacity(80);

        Mockito.when(service.create(any(CreateZoneRequest.class)))
               .thenThrow(new DataIntegrityViolationException("Duplicate entry 'Bosque' for key 'uk_zone_name'"));

        mvc.perform(post("/api/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", anyOf(is("Conflict"), is("CONFLICT"))))
            .andExpect(jsonPath("$.message", containsString("Zone")));
    }

    // ---------------------------
    // PUT /api/zones/{id}
    // ---------------------------
    @Test
    void put_updateZone_returns200_andDTO() throws Exception {
        UpdateZoneRequest body = new UpdateZoneRequest();
        body.setDescription("Más frondosa");
        body.setCapacity(90);

        Zone updated = new Zone();
        updated.setId(10L); updated.setName("Bosque");
        updated.setDescription("Más frondosa"); updated.setCapacity(90);

        Mockito.when(service.update(eq(10L), any(UpdateZoneRequest.class))).thenReturn(updated);

        mvc.perform(put("/api/zones/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(10)))
            .andExpect(jsonPath("$.description", is("Más frondosa")))
            .andExpect(jsonPath("$.capacity", is(90)));
    }

    @Test
    void put_updateZone_400_whenNameBlank() throws Exception {
        UpdateZoneRequest body = new UpdateZoneRequest();
        body.setName("   "); // inválido

        Mockito.when(service.update(eq(10L), any(UpdateZoneRequest.class)))
               .thenThrow(new IllegalArgumentException("Zone name cannot be blank"));

        mvc.perform(put("/api/zones/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("cannot be blank")));
    }

    @Test
    void put_updateZone_404_whenNotFound() throws Exception {
        UpdateZoneRequest body = new UpdateZoneRequest();
        body.setDescription("Nueva");

        Mockito.when(service.update(eq(999L), any(UpdateZoneRequest.class)))
               .thenThrow(new ResourceNotFoundException("Zone not found: 999"));

        mvc.perform(put("/api/zones/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("Zone not found")));
    }

    @Test
    void put_updateZone_409_whenNameDuplicate() throws Exception {
        UpdateZoneRequest body = new UpdateZoneRequest();
        body.setName("Bosque");

        Mockito.when(service.update(eq(10L), any(UpdateZoneRequest.class)))
               .thenThrow(new DataIntegrityViolationException("Duplicate entry 'Bosque' for key 'uk_zone_name'"));

        mvc.perform(put("/api/zones/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", anyOf(is("Conflict"), is("CONFLICT"))));
    }

    // ---------------------------
    // DELETE /api/zones/{id}
    // ---------------------------
    @Test
    void delete_zone_returns204() throws Exception {
        doNothing().when(service).delete(10L);

        mvc.perform(delete("/api/zones/10"))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_zone_404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Zone not found: 777"))
                .when(service).delete(777L);

        mvc.perform(delete("/api/zones/777"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("Zone not found")));
    }

    @Test
    void delete_zone_422_whenCreaturesAssigned() throws Exception {
        doThrow(new IllegalStateException("Cannot delete zone with assigned creatures"))
                .when(service).delete(10L);

        mvc.perform(delete("/api/zones/10"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error", anyOf(is("Unprocessable Entity"), is("UNPROCESSABLE_ENTITY"))))
            .andExpect(jsonPath("$.message", containsString("assigned creatures")));
    }

    @Test
    void delete_zone_500_onUnexpectedError() throws Exception {
        doThrow(new RuntimeException("Boom"))
                .when(service).delete(10L);

        mvc.perform(delete("/api/zones/10"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error", anyOf(is("Internal Server Error"), is("INTERNAL_SERVER_ERROR"))));
    }
}
