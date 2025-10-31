package com.zoo.zoofantastico.service;

import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.model.Creature;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.repository.CreatureRepository;
import com.zoo.zoofantastico.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import com.zoo.zoofantastico.dto.request.CreateCreatureRequest;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;

import java.util.List;

@Service
public class CreatureService {

  private final CreatureRepository repo;
  private final ZoneRepository zoneRepo;

  public CreatureService(CreatureRepository repo, ZoneRepository zoneRepo) {
    this.repo = repo;
    this.zoneRepo = zoneRepo;
  }

  /* --------- CREATE --------- */
  public Creature createCreature(CreateCreatureRequest req) {
  if (req.getZoneId() == null) throw new IllegalArgumentException("zoneId is required");
  Zone z = zoneRepo.findById(req.getZoneId())
      .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + req.getZoneId()));

  Creature c = new Creature();
  c.setName(req.getName());
  c.setSpecies(req.getSpecies());
  if (req.getSize() != null) c.setSize(req.getSize());
if (req.getDangerLevel() != null) c.setDangerLevel(req.getDangerLevel());
  c.setHealthStatus(req.getHealthStatus());
  c.setZone(z);
  return repo.save(c);
}


  /* --------- READ --------- */
  public List<Creature> getAllCreatures() {
    return repo.findAll();
  }

  public Creature getCreatureById(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Creature not found: " + id));
  }

  /* --------- UPDATE --------- */
  public Creature updateCreature(Long id, UpdateCreatureRequest req) {
    var c = getCreatureById(id);

    // Campos simples
    if (req.getName() != null)         c.setName(req.getName());
    if (req.getSpecies() != null)      c.setSpecies(req.getSpecies());
    if (req.getSize() != null)         c.setSize(req.getSize());
    if (req.getDangerLevel() != null)  c.setDangerLevel(req.getDangerLevel());
    if (req.getHealthStatus() != null) c.setHealthStatus(req.getHealthStatus());

    // Zona (si se envía zoneId)
    if (req.getZoneId() != null) {
        var z = zoneRepo.findById(req.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + req.getZoneId()));
        c.setZone(z);
    }

    return repo.save(c);
}


  /* --------- DELETE --------- */
  public void deleteCreature(Long id) {
    Creature c = getCreatureById(id);
    String hs = c.getHealthStatus();
    if (hs == null || !hs.equalsIgnoreCase("critical")) {
      repo.delete(c);
    } else {
      throw new IllegalStateException("Cannot delete a creature in critical health");
    }
  }
}
