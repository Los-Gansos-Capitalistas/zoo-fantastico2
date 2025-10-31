package com.zoo.zoofantastico.service;

import com.zoo.zoofantastico.exception.ResourceNotFoundException;
import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.repository.CreatureRepository;
import com.zoo.zoofantastico.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import com.zoo.zoofantastico.dto.ZoneSummaryDTO;
import com.zoo.zoofantastico.dto.request.CreateZoneRequest;
import com.zoo.zoofantastico.dto.request.UpdateZoneRequest;

import java.util.List;

@Service
public class ZoneService {

    private final ZoneRepository repo;
    private final CreatureRepository creatureRepo;

    public ZoneService(ZoneRepository repo, CreatureRepository creatureRepo) {
  this.repo = repo;
  this.creatureRepo = creatureRepo;
}

// CREATE (desde DTO) — idempotente por nombre (case-insensitive)
public Zone create(CreateZoneRequest req) {
  String name = req.getName() != null ? req.getName().trim() : null;
  if (name == null || name.isBlank()) {
    throw new IllegalArgumentException("Zone name is required");
  }

  return repo.findByNameIgnoreCase(name)
      .orElseGet(() -> {
        Zone z = new Zone();
        z.setName(name);
        z.setDescription(req.getDescription());
        z.setCapacity(req.getCapacity());
        return repo.save(z);
      });
}

// UPDATE (desde DTO)
public Zone update(Long id, UpdateZoneRequest req) {
  Zone cur = findOne(id);

  if (req.getName() != null) {
    String n = req.getName().trim();
    if (n.isEmpty()) throw new IllegalArgumentException("Zone name cannot be blank");
    cur.setName(n);
  }
  if (req.getDescription() != null) cur.setDescription(req.getDescription());
  if (req.getCapacity() != null)    cur.setCapacity(req.getCapacity());

  return repo.save(cur);
}



    // READ - all
    public List<ZoneSummaryDTO> findSummary() {
  return repo.findZoneSummaries();
}

    // READ - one 
    public Zone findOne(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + id));
    }

    // DELETE
    public void delete(Long id) {
    Zone cur = findOne(id);
    long assigned = creatureRepo.countByZoneId(id);
    if (assigned > 0) {
        throw new IllegalStateException("Cannot delete zone with assigned creatures");
    }
    repo.delete(cur);
}


    
}

