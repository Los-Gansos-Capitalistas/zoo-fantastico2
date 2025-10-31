package com.zoo.zoofantastico.service;

import com.zoo.zoofantastico.dto.request.CreateCreatureRequest;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;
import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.model.Creature;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.repository.CreatureRepository;
import com.zoo.zoofantastico.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreatureServiceTest {

  @Mock private CreatureRepository creatureRepository;
  @Mock private ZoneRepository zoneRepository;

  @InjectMocks private CreatureService service;

  @BeforeEach
  void setup() { MockitoAnnotations.openMocks(this); }


  @Test
  void getCreatureById_returnsCreature_whenExists() {
    Creature c = new Creature();
    c.setId(1L);
    when(creatureRepository.findById(1L)).thenReturn(Optional.of(c));

    Creature out = service.getCreatureById(1L);

    assertNotNull(out);
    assertEquals(1L, out.getId());
    verify(creatureRepository).findById(1L);
  }

  @Test
  void getCreatureById_throws_whenNotFound() {
    when(creatureRepository.findById(9L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.getCreatureById(9L));
    verify(creatureRepository).findById(9L);
  }


  @Test
  void createCreature_loadsZone_andSaves() {
    Zone z = new Zone(); z.setId(2L); z.setName("Desierto");

    CreateCreatureRequest req = new CreateCreatureRequest();
    req.setName("Fénix");
    req.setSpecies("Ave");
    req.setSize(1.0);
    req.setDangerLevel(5);
    req.setHealthStatus("stable");
    req.setZoneId(2L);

    when(zoneRepository.findById(2L)).thenReturn(Optional.of(z));
    when(creatureRepository.save(any(Creature.class))).thenAnswer(inv -> inv.getArgument(0));

    Creature saved = service.createCreature(req);

    assertNotNull(saved);
    assertEquals("Desierto", saved.getZone().getName());
    verify(zoneRepository).findById(2L);
    verify(creatureRepository).save(any(Creature.class));
  }

  @Test
  void createCreature_saves_allMappedFields() {
    Zone z = new Zone(); z.setId(2L); z.setName("Desierto");
    when(zoneRepository.findById(2L)).thenReturn(Optional.of(z));
    when(creatureRepository.save(any(Creature.class))).thenAnswer(inv -> inv.getArgument(0));

    CreateCreatureRequest req = new CreateCreatureRequest();
    req.setName("Fénix");
    req.setSpecies("Ave");
    req.setSize(1.0);
    req.setDangerLevel(5);
    req.setHealthStatus("stable");
    req.setZoneId(2L);

    Creature saved = service.createCreature(req);

    assertAll(
        () -> assertEquals("Fénix", saved.getName()),
        () -> assertEquals("Ave", saved.getSpecies()),
        () -> assertEquals(1.0, saved.getSize()),
        () -> assertEquals(5, saved.getDangerLevel()),
        () -> assertEquals("stable", saved.getHealthStatus()),
        () -> assertEquals("Desierto", saved.getZone().getName())
    );
  }

  @Test
  void createCreature_throws_whenZoneIdMissing() {
    CreateCreatureRequest req = new CreateCreatureRequest(); // sin zoneId
    assertThrows(IllegalArgumentException.class, () -> service.createCreature(req));
    verifyNoInteractions(creatureRepository);
  }

  @Test
  void createCreature_throws_whenZoneNotFound() {
    CreateCreatureRequest req = new CreateCreatureRequest();
    req.setName("Fénix");
    req.setSpecies("Ave");
    req.setSize(1.0);
    req.setDangerLevel(5);
    req.setHealthStatus("stable");
    req.setZoneId(999L);

    when(zoneRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.createCreature(req));
    verify(creatureRepository, never()).save(any());
  }


  @Test
  void updateCreature_updatesFields_andZone() {
    Creature existing = new Creature();
    existing.setId(1L);
    existing.setName("Old");

    Zone z = new Zone(); z.setId(3L); z.setName("Bosque");

    UpdateCreatureRequest incoming = new UpdateCreatureRequest();
    incoming.setName("New");
    incoming.setSpecies(null);
    incoming.setSize(null);
    incoming.setDangerLevel(null);
    incoming.setHealthStatus(null);
    incoming.setZoneId(3L);

    when(creatureRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(zoneRepository.findById(3L)).thenReturn(Optional.of(z));
    when(creatureRepository.save(any(Creature.class))).thenAnswer(inv -> inv.getArgument(0));

    Creature out = service.updateCreature(1L, incoming);

    assertEquals("New", out.getName());
    assertEquals("Bosque", out.getZone().getName());
    verify(creatureRepository).save(any(Creature.class));
  }

  @Test
  void updateCreature_throws_whenCreatureNotFound() {
    when(creatureRepository.findById(77L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.updateCreature(77L, new UpdateCreatureRequest()));
    verifyNoInteractions(zoneRepository);
    verify(creatureRepository, never()).save(any());
  }

  @Test
  void updateCreature_throws_whenZoneNotFound() {
    Creature existing = new Creature(); existing.setId(1L);
    when(creatureRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(zoneRepository.findById(404L)).thenReturn(Optional.empty());

    UpdateCreatureRequest req = new UpdateCreatureRequest();
    req.setZoneId(404L);

    assertThrows(ResourceNotFoundException.class, () -> service.updateCreature(1L, req));
    verify(creatureRepository, never()).save(any());
  }

  @Test
  void updateCreature_preservesFields_whenNullsProvided() {
    Creature existing = new Creature();
    existing.setId(1L);
    existing.setName("Old");
    existing.setSpecies("OldSp");
    existing.setSize(2.0);
    existing.setDangerLevel(3);
    existing.setHealthStatus("stable");

    when(creatureRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(creatureRepository.save(any(Creature.class))).thenAnswer(inv -> inv.getArgument(0));

    UpdateCreatureRequest req = new UpdateCreatureRequest();
    // todos null porqueno deben pisar valores actuales
    req.setName(null);
    req.setSpecies(null);
    req.setSize(null);
    req.setDangerLevel(null);
    req.setHealthStatus(null);
    // sin zoneId

    Creature out = service.updateCreature(1L, req);

    assertEquals("Old", out.getName());
    assertEquals("OldSp", out.getSpecies());
    assertEquals(2.0, out.getSize());
    assertEquals(3, out.getDangerLevel());
    assertEquals("stable", out.getHealthStatus());
    assertNull(out.getZone()); // si antes era null, debe conservarse
  }


  @Test
  void deleteCreature_blocks_whenCritical() {
    Creature c = new Creature();
    c.setId(1L); c.setHealthStatus("critical");
    when(creatureRepository.findById(1L)).thenReturn(Optional.of(c));

    assertThrows(IllegalStateException.class, () -> service.deleteCreature(1L));
    verify(creatureRepository, never()).delete(any());
  }

  @Test
  void deleteCreature_deletes_whenNotCritical() {
    Creature c = new Creature();
    c.setId(1L); c.setHealthStatus("stable");
    when(creatureRepository.findById(1L)).thenReturn(Optional.of(c));

    service.deleteCreature(1L);

    verify(creatureRepository).delete(c);
  }

  @Test
  void deleteCreature_throws_whenNotFound() {
    when(creatureRepository.findById(123L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.deleteCreature(123L));
    verify(creatureRepository, never()).delete(any());
  }


  @Test
  void getAllCreatures_returnsList() {
    when(creatureRepository.findAll()).thenReturn(List.of(new Creature(), new Creature()));
    assertEquals(2, service.getAllCreatures().size());
  }

  @Test
void deleteCreature_doesNotDelete_andMessageMentionsCritical() {
  Creature c = new Creature();
  c.setId(42L);
  c.setHealthStatus("critical"); 

  when(creatureRepository.findById(42L)).thenReturn(Optional.of(c));

  IllegalStateException ex =
      assertThrows(IllegalStateException.class, () -> service.deleteCreature(42L));

  assertTrue(
      ex.getMessage().toLowerCase().contains("critical") ||
      ex.getMessage().toLowerCase().contains("crítica"),
      "El mensaje debería mencionar que está en estado crítico"
  );
  verify(creatureRepository, never()).delete(any());
}

}
