package com.zoo.zoofantastico.service;

import com.zoo.zoofantastico.dto.request.CreateCreatureRequest;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;
import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.model.Creature;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.repository.CreatureRepository;
import com.zoo.zoofantastico.repository.ZoneRepository;
import com.zoo.zoofantastico.it.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreatureServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private CreatureService creatureService;
    @Autowired private CreatureRepository creatureRepository;
    @Autowired private ZoneRepository zoneRepository;

    private Long zoneAId;
    private Long zoneBId;

    @BeforeEach
    void setup() {
        // Limpieza por FK: primero criaturas, luego zonas
        creatureRepository.deleteAll();
        zoneRepository.deleteAll();

        // Zonas con nombres únicos (evita UNIQUE(name) collisions)
        Zone zoneA = new Zone();
        zoneA.setName("Bosque-" + UUID.randomUUID());
        zoneRepository.saveAndFlush(zoneA);
        zoneAId = zoneA.getId();

        Zone zoneB = new Zone();
        zoneB.setName("Desierto-" + UUID.randomUUID());
        zoneRepository.saveAndFlush(zoneB);
        zoneBId = zoneB.getId();

        assertNotNull(zoneAId);
        assertNotNull(zoneBId);
    }

    @Test
    void createCreature_shouldPersistInDatabase() {
        CreateCreatureRequest req = new CreateCreatureRequest();
        req.setName("Grifo");
        req.setSpecies("Gryphon");
        req.setSize(2.5);
        req.setDangerLevel(4);
        req.setHealthStatus("stable");
        req.setZoneId(zoneAId);

        Creature created = creatureService.createCreature(req);
        assertNotNull(created.getId());

        Optional<Creature> fromDb = creatureRepository.findById(created.getId());
        assertTrue(fromDb.isPresent());

        Creature c = fromDb.get();
        assertEquals("Grifo", c.getName());
        assertEquals("Gryphon", c.getSpecies());
        assertEquals(2.5, c.getSize());
        assertEquals(4, c.getDangerLevel());
        assertEquals("stable", c.getHealthStatus());
        assertNotNull(c.getZone());
        assertEquals(zoneAId, c.getZone().getId());
    }

    @Test
    void updateCreature_shouldModifyFieldsAndZoneInDatabase() {
        Creature base = new Creature();
        base.setName("Niffler");
        base.setSpecies("Niffler");
        base.setSize(0.8);
        base.setDangerLevel(2);
        base.setHealthStatus("stable");
        base.setZone(zoneRepository.findById(zoneAId).orElseThrow());
        creatureRepository.saveAndFlush(base);

        Long id = base.getId();

        UpdateCreatureRequest req = new UpdateCreatureRequest();
        req.setName("Niffler Dorado");
        req.setSpecies("Niffler");
        req.setSize(1.1);
        req.setDangerLevel(3);
        req.setHealthStatus("observation");
        req.setZoneId(zoneBId);

        Creature updated = creatureService.updateCreature(id, req);
        assertNotNull(updated);

        Creature fromDb = creatureRepository.findById(id).orElseThrow();
        assertEquals("Niffler Dorado", fromDb.getName());
        assertEquals("Niffler", fromDb.getSpecies());
        assertEquals(1.1, fromDb.getSize());
        assertEquals(3, fromDb.getDangerLevel());
        assertEquals("observation", fromDb.getHealthStatus());
        assertNotNull(fromDb.getZone());
        assertEquals(zoneBId, fromDb.getZone().getId());
    }

    @Test
    void deleteCreature_whenHealthNotCritical_shouldDelete() {
        Creature base = new Creature();
        base.setName("Kelpie");
        base.setSpecies("Kelpie");
        base.setSize(4.0);
        base.setDangerLevel(3);
        base.setHealthStatus("stable");
        base.setZone(zoneRepository.findById(zoneAId).orElseThrow());
        creatureRepository.saveAndFlush(base);

        Long id = base.getId();
        creatureService.deleteCreature(id);

        assertFalse(creatureRepository.findById(id).isPresent());
    }

    @Test
    void deleteCreature_whenHealthCritical_shouldThrow() {
        Creature base = new Creature();
        base.setName("Basilisk");
        base.setSpecies("Basilisk");
        base.setSize(12.0);
        base.setDangerLevel(5);
        base.setHealthStatus("CRITICAL"); // equalsIgnoreCase("critical")
        base.setZone(zoneRepository.findById(zoneAId).orElseThrow());
        creatureRepository.saveAndFlush(base);

        Long id = base.getId();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> creatureService.deleteCreature(id));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot delete a creature in critical health"));

        assertTrue(creatureRepository.findById(id).isPresent());
    }

    @Test
    void updateCreature_withInvalidZone_shouldThrowResourceNotFound() {
        Creature base = new Creature();
        base.setName("Thestral");
        base.setSpecies("Thestral");
        base.setSize(3.0);
        base.setDangerLevel(3);
        base.setHealthStatus("stable");
        base.setZone(zoneRepository.findById(zoneAId).orElseThrow());
        creatureRepository.saveAndFlush(base);

        Long id = base.getId();
        Long invalidZoneId = 999_999L;

        UpdateCreatureRequest req = new UpdateCreatureRequest();
        req.setZoneId(invalidZoneId);

        assertThrows(ResourceNotFoundException.class,
                () -> creatureService.updateCreature(id, req));
    }

    @Test
    void getCreatureById_whenNotExists_shouldThrowResourceNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> creatureService.getCreatureById(123456L));
    }

    @Test
    void createCreature_withInvalidZone_shouldThrowResourceNotFound() {
        CreateCreatureRequest req = new CreateCreatureRequest();
        req.setName("Augurey");
        req.setSpecies("Augurey");
        req.setSize(0.5);
        req.setDangerLevel(1);
        req.setHealthStatus("stable");
        req.setZoneId(424242L); // inexistente

        assertThrows(ResourceNotFoundException.class,
                () -> creatureService.createCreature(req));
    }

    @Test
    void createCreature_withoutZoneId_shouldThrowIllegalArgument() {
        CreateCreatureRequest req = new CreateCreatureRequest();
        req.setName("Bowtruckle");
        req.setSpecies("Bowtruckle");
        req.setSize(0.2);
        req.setDangerLevel(1);
        req.setHealthStatus("stable");
        // sin zoneId

        assertThrows(IllegalArgumentException.class,
                () -> creatureService.createCreature(req));
    }
}
