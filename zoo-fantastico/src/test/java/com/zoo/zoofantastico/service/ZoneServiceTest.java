package com.zoo.zoofantastico.service;

import com.zoo.zoofantastico.dto.ZoneSummaryDTO;
import com.zoo.zoofantastico.dto.request.CreateZoneRequest;
import com.zoo.zoofantastico.dto.request.UpdateZoneRequest;
import com.zoo.zoofantastico.exception.ResourceNotFoundException;
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

class ZoneServiceTest {

  @Mock private ZoneRepository repo;
  @Mock private CreatureRepository creatureRepo;

  @InjectMocks private ZoneService service;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  void create_saves_whenNotExists_andTrimsName() {
    CreateZoneRequest req = new CreateZoneRequest();
    req.setName("  Bosque  ");
    req.setDescription("Zona boscosa");
    req.setCapacity(50);

    when(repo.findByNameIgnoreCase("Bosque")).thenReturn(Optional.empty());
    when(repo.save(any(Zone.class))).thenAnswer(inv -> {
      Zone z = inv.getArgument(0);
      z.setId(1L);
      return z;
    });

    Zone out = service.create(req);

    assertNotNull(out);
    assertEquals(1L, out.getId());
    assertEquals("Bosque", out.getName()); // trimmed
    assertEquals("Zona boscosa", out.getDescription());
    assertEquals(50, out.getCapacity());
    verify(repo).findByNameIgnoreCase("Bosque");
    verify(repo).save(any(Zone.class));
  }

  @Test
  void create_returnsExisting_whenNameAlreadyExists_caseInsensitive() {
    CreateZoneRequest req = new CreateZoneRequest();
    req.setName("bosque"); 

    Zone existing = new Zone();
    existing.setId(7L);
    existing.setName("Bosque");

    when(repo.findByNameIgnoreCase("bosque")).thenReturn(Optional.of(existing));

    Zone out = service.create(req);

    assertSame(existing, out);
    verify(repo).findByNameIgnoreCase("bosque");
    verify(repo, never()).save(any());
  }

  @Test
  void create_throws_whenNameMissingOrBlank() {
    CreateZoneRequest reqNull = new CreateZoneRequest();
    reqNull.setName(null);

    CreateZoneRequest reqBlank = new CreateZoneRequest();
    reqBlank.setName("   ");

    assertThrows(IllegalArgumentException.class, () -> service.create(reqNull));
    assertThrows(IllegalArgumentException.class, () -> service.create(reqBlank));
    verifyNoInteractions(repo);
  }


  @Test
  void update_updatesProvidedFields_andTrimsName() {
    Zone cur = new Zone();
    cur.setId(10L);
    cur.setName("Viejo");
    cur.setDescription("Desc");
    cur.setCapacity(10);

    UpdateZoneRequest patch = new UpdateZoneRequest();
    patch.setName("  Nuevo ");
    patch.setDescription("Nueva desc");
    patch.setCapacity(20);

    when(repo.findById(10L)).thenReturn(Optional.of(cur));
    when(repo.save(any(Zone.class))).thenAnswer(inv -> inv.getArgument(0));

    Zone out = service.update(10L, patch);

    assertEquals(10L, out.getId());
    assertEquals("Nuevo", out.getName());          // trimmed
    assertEquals("Nueva desc", out.getDescription());
    assertEquals(20, out.getCapacity());
    verify(repo).save(any(Zone.class));
  }

  @Test
  void update_preservesFields_whenNullsProvided() {
    Zone cur = new Zone();
    cur.setId(11L);
    cur.setName("Actual");
    cur.setDescription("Base");
    cur.setCapacity(5);

    UpdateZoneRequest patch = new UpdateZoneRequest();
    patch.setName(null);
    patch.setDescription(null);
    patch.setCapacity(null);

    when(repo.findById(11L)).thenReturn(Optional.of(cur));
    when(repo.save(any(Zone.class))).thenAnswer(inv -> inv.getArgument(0));

    Zone out = service.update(11L, patch);

    assertEquals("Actual", out.getName());
    assertEquals("Base", out.getDescription());
    assertEquals(5, out.getCapacity());
    verify(repo).save(any(Zone.class));
  }

  @Test
  void update_throws_whenNameBlankAfterTrim() {
    Zone cur = new Zone();
    cur.setId(12L);
    cur.setName("Nombre");

    UpdateZoneRequest patch = new UpdateZoneRequest();
    patch.setName("   "); // debería disparar IllegalArgumentException

    when(repo.findById(12L)).thenReturn(Optional.of(cur));

    assertThrows(IllegalArgumentException.class, () -> service.update(12L, patch));
    verify(repo, never()).save(any());
  }

  @Test
  void update_throws_whenZoneNotFound() {
    when(repo.findById(404L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.update(404L, new UpdateZoneRequest()));
    verify(repo, never()).save(any());
  }

  @Test
  void findSummary_returnsListFromRepository() {
    List<ZoneSummaryDTO> summaries = List.of(
        mock(ZoneSummaryDTO.class),
        mock(ZoneSummaryDTO.class)
    );
    when(repo.findZoneSummaries()).thenReturn(summaries);

    List<ZoneSummaryDTO> out = service.findSummary();

    assertNotNull(out);
    assertEquals(2, out.size());
    verify(repo).findZoneSummaries();
  }

  @Test
  void findOne_returns_whenExists() {
    Zone z = new Zone(); z.setId(30L); z.setName("Desierto");
    when(repo.findById(30L)).thenReturn(Optional.of(z));

    Zone out = service.findOne(30L);

    assertEquals(30L, out.getId());
    verify(repo).findById(30L);
  }

  @Test
  void findOne_throws_whenNotFound() {
    when(repo.findById(31L)).thenReturn(Optional.empty());
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.findOne(31L));
    assertTrue(ex.getMessage().contains("31"));
  }


  @Test
  void delete_throws_whenZoneHasAssignedCreatures() {
    Zone z = new Zone(); z.setId(1L); z.setName("Bosque");
    when(repo.findById(1L)).thenReturn(Optional.of(z));
    when(creatureRepo.countByZoneId(1L)).thenReturn(3L);

    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.delete(1L));
    assertTrue(ex.getMessage().toLowerCase().contains("cannot delete zone"));
    verify(repo, never()).delete(any());
  }

  @Test
  void delete_ok_whenNoAssignedCreatures() {
    Zone z = new Zone(); z.setId(2L); z.setName("Desierto");
    when(repo.findById(2L)).thenReturn(Optional.of(z));
    when(creatureRepo.countByZoneId(2L)).thenReturn(0L);

    assertDoesNotThrow(() -> service.delete(2L));
    verify(repo).delete(z);
  }

  @Test
  void delete_throws_whenZoneNotFound() {
    when(repo.findById(99L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    verify(repo, never()).delete(any());
  }
}

