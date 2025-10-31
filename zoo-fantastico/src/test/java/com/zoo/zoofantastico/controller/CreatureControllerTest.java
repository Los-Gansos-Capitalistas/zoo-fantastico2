package com.zoo.zoofantastico.controller;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.zoofantastico.dto.request.CreateCreatureRequest;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;
import com.zoo.zoofantastico.exception.ApiExceptionHandler;
import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.model.Creature;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.service.CreatureService;

@ActiveProfiles("test")
@WebMvcTest(CreatureController.class)
@Import(ApiExceptionHandler.class)
class CreatureControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper om;

@MockBean
private CreatureService service;


  @Test
  void getAll_returnsDTOList() throws Exception {
    Creature c = new Creature();
    c.setId(1L); c.setName("Fénix");
    Zone z = new Zone(); z.setId(10L); z.setName("Desierto");
    c.setZone(z);

    Mockito.when(service.getAllCreatures()).thenReturn(List.of(c));

    mvc.perform(get("/api/creatures"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id", is(1)))
      .andExpect(jsonPath("$[0].name", is("Fénix")))
      .andExpect(jsonPath("$[0].zone.id", is(10)))
      .andExpect(jsonPath("$[0].zone.name", is("Desierto")));
  }

  @Test
  void create_returnsDTO201() throws Exception {
    CreateCreatureRequest body = new CreateCreatureRequest();
body.setName("Fénix");
body.setSpecies("Ave de fuego");
body.setSize(1.0);           // cumple validación
body.setDangerLevel(5);      // dentro del rango
body.setHealthStatus("healthy");
body.setZoneId(10L);

    Creature saved = new Creature();
    saved.setId(2L); saved.setName("Fénix");
    Zone z = new Zone(); z.setId(10L); z.setName("Desierto");
    saved.setZone(z);


Mockito.when(service.createCreature(any(CreateCreatureRequest.class))).thenReturn(saved);

    mvc.perform(post("/api/creatures")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(body)))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "/api/creatures/2"))
      .andExpect(jsonPath("$.id", is(2)))
      .andExpect(jsonPath("$.zone.name", is("Desierto")));
  }

  @Test
  void update_returnsDTO200() throws Exception {
    UpdateCreatureRequest req = new UpdateCreatureRequest();
req.setName("Nuevo");
req.setSpecies(null);
req.setSize(null);
req.setDangerLevel(null);
req.setHealthStatus(null);
req.setZoneId(10L);

    Creature updated = new Creature();
    updated.setId(3L); updated.setName("Nuevo");
    Zone z = new Zone(); z.setId(10L); z.setName("Desierto");
    updated.setZone(z);

    Mockito.when(service.updateCreature(eq(3L), any(UpdateCreatureRequest.class))).thenReturn(updated);

    mvc.perform(put("/api/creatures/3")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(req)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(3)))
      .andExpect(jsonPath("$.name", is("Nuevo")))
      .andExpect(jsonPath("$.zone.name", is("Desierto")));
  }

  @Test
  void delete_returns204() throws Exception {
    mvc.perform(delete("/api/creatures/5"))
      .andExpect(status().isNoContent());
    Mockito.verify(service).deleteCreature(5L);
  }



@Test
void update_returns400_whenBodyInvalid_andDoesNotCallService() throws Exception {
  var bad = new UpdateCreatureRequest();
  bad.setSize(0.0);       // inválido (min 0.1)
  bad.setDangerLevel(0);  // inválido (min 1)

  mvc.perform(put("/api/creatures/123")
      .contentType(MediaType.APPLICATION_JSON)
      .content(om.writeValueAsString(bad)))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.fieldErrors").isArray());

  verifyNoInteractions(service);
}

@Test
void getById_returns200_withDTO() throws Exception {
  Creature c = new Creature();
  c.setId(1L);
  c.setName("Fénix");
  Zone z = new Zone(); z.setId(10L); z.setName("Desierto");
  c.setZone(z);

  Mockito.when(service.getCreatureById(1L)).thenReturn(c);

  mvc.perform(get("/api/creatures/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id", is(1)))
    .andExpect(jsonPath("$.name", is("Fénix")))
    .andExpect(jsonPath("$.zone.id", is(10)))
    .andExpect(jsonPath("$.zone.name", is("Desierto")));
}

@Test
void getById_returns404_withErrorResponse_whenNotFound() throws Exception {
  Mockito.when(service.getCreatureById(99L))
    .thenThrow(new ResourceNotFoundException("Creature not found: 99"));

  mvc.perform(get("/api/creatures/99"))
    .andExpect(status().isNotFound())
    .andExpect(jsonPath("$.status", is(404)))
    .andExpect(jsonPath("$.error", is("Not Found")))
    .andExpect(jsonPath("$.message", is("Creature not found: 99")))
    .andExpect(jsonPath("$.path", is("/api/creatures/99")))
    .andExpect(jsonPath("$.fieldErrors").isArray())
    .andExpect(jsonPath("$.fieldErrors.length()", is(0))); // sin fieldErrors en 404
}

  @Test
void create_returns400_withFieldErrors_whenBodyInvalid() throws Exception {
  // size < 0.1 y name en blanco
  var bad = new CreateCreatureRequest();
  bad.setName(" ");
  bad.setSpecies("");        // NotBlank
  bad.setSize(0.0);          // DecimalMin 0.1
  bad.setDangerLevel(0);     // Min 1
  bad.setHealthStatus("");
  bad.setZoneId(null);       // NotNull

  mvc.perform(post("/api/creatures")
      .contentType(MediaType.APPLICATION_JSON)
      .content(om.writeValueAsString(bad)))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.error").value("Bad Request"))
    .andExpect(jsonPath("$.message").value("Validation failed"))
    .andExpect(jsonPath("$.fieldErrors").isArray())
    // verifica algunos campos específicos
    .andExpect(jsonPath("$.fieldErrors[*].field", hasItems(
        "name","species","size","dangerLevel","healthStatus","zoneId"
    )));

  verifyNoInteractions(service);

}

}
