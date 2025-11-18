package com.zoo.zoofantastico.service;

import com.zoo.zoofantastico.dto.ZoneSummaryDTO;
import com.zoo.zoofantastico.dto.request.CreateZoneRequest;
import com.zoo.zoofantastico.dto.request.UpdateZoneRequest;
import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.it.BaseIntegrationTest;
import com.zoo.zoofantastico.model.Creature;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.repository.CreatureRepository;
import com.zoo.zoofantastico.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ZoneServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private ZoneService zoneService;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private CreatureRepository creatureRepository;

    private Long baseZoneId;

    @BeforeEach
    void setup() {
        // Limpieza por FK: primero criaturas, luego zonas
        creatureRepository.deleteAll();
        zoneRepository.deleteAll();

        // Zona base con nombre único para evitar colisiones por UNIQUE(name)
        Zone base = new Zone();
        base.setName("Bosque-" + UUID.randomUUID());
        base.setDescription("Zona base");
        base.setCapacity(40);
        zoneRepository.saveAndFlush(base);

        baseZoneId = base.getId();
        assertNotNull(baseZoneId);
    }

    @Test
    void create_whenNewName_shouldPersist() {
        String unique = "Desierto-" + UUID.randomUUID();

        CreateZoneRequest req = new CreateZoneRequest();
        req.setName(unique);
        req.setDescription("Zona árida");
        req.setCapacity(30);

        Zone created = zoneService.create(req);

        assertNotNull(created.getId());
        assertEquals(unique, created.getName());
        assertEquals("Zona árida", created.getDescription());
        assertEquals(30, created.getCapacity());

        Optional<Zone> fromDb = zoneRepository.findById(created.getId());
        assertTrue(fromDb.isPresent());
    }

    @Test
    void create_whenNameExistsIgnoreCase_shouldReturnExistingWithoutDuplicating() {
        String canonical = "Valle-" + UUID.randomUUID();

        CreateZoneRequest first = new CreateZoneRequest();
        first.setName(canonical);
        first.setDescription("v1");
        first.setCapacity(10);
        Zone z1 = zoneService.create(first);

        CreateZoneRequest second = new CreateZoneRequest();
        second.setName(canonical.toUpperCase());
        second.setDescription("v2-should-be-ignored");
        second.setCapacity(999);
        Zone z2 = zoneService.create(second);

        assertEquals(z1.getId(), z2.getId(), "Debe retornar la zona ya existente (idempotente por nombre)");

        long count = zoneRepository.findAll().stream()
                .filter(z -> z.getName().equals(canonical))
                .count();
        assertEquals(1, count, "No debe duplicarse la zona");
    }

    @Test
    void update_shouldModifyFields() {
        UpdateZoneRequest upd = new UpdateZoneRequest();
        String newName = "Bosque-Encantado-" + UUID.randomUUID();
        upd.setName(newName);
        upd.setDescription("Bosque con criaturas mágicas");
        upd.setCapacity(80);

        Zone updated = zoneService.update(baseZoneId, upd);

        assertEquals(newName, updated.getName());
        assertEquals("Bosque con criaturas mágicas", updated.getDescription());
        assertEquals(80, updated.getCapacity());

        Zone fromDb = zoneRepository.findById(baseZoneId).orElseThrow();
        assertEquals(newName, fromDb.getName());
        assertEquals(80, fromDb.getCapacity());
    }

    @Test
    void update_withBlankName_shouldThrowIllegalArgument() {
        UpdateZoneRequest upd = new UpdateZoneRequest();
        upd.setName("   "); // blank

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> zoneService.update(baseZoneId, upd));
        assertTrue(ex.getMessage().toLowerCase().contains("zone name cannot be blank"));
    }

    @Test
    void create_withBlankName_shouldThrowIllegalArgument() {
        CreateZoneRequest req = new CreateZoneRequest();
        req.setName("  "); // blank
        req.setDescription("No importa");
        req.setCapacity(10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> zoneService.create(req));
        assertTrue(ex.getMessage().toLowerCase().contains("zone name is required"));
    }

    @Test
    void delete_whenCreaturesAssigned_shouldThrowIllegalState() {
        Zone z = zoneRepository.findById(baseZoneId).orElseThrow();

        Creature c = new Creature();
        c.setName("Grifo");
        c.setSpecies("Gryphon");
        c.setSize(2.5);
        c.setDangerLevel(4);
        c.setHealthStatus("stable");
        c.setZone(z);
        creatureRepository.saveAndFlush(c);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> zoneService.delete(baseZoneId));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot delete zone with assigned creatures"));

        assertTrue(zoneRepository.findById(baseZoneId).isPresent());
    }

    @Test
    void delete_whenNoCreatures_shouldDelete() {
        creatureRepository.deleteAll();

        zoneService.delete(baseZoneId);

        assertFalse(zoneRepository.findById(baseZoneId).isPresent());
    }

    @Test
    void findOne_whenNotExists_shouldThrowResourceNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> zoneService.findOne(999_999L));
    }

    @Test
    void findSummary_shouldReturnList() {
        List<ZoneSummaryDTO> summaries = zoneService.findSummary();
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty(), "Debería retornar al menos 1 zona (la base)");
    }
}
