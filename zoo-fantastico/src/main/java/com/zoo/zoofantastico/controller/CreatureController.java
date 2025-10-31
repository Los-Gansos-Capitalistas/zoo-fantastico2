package com.zoo.zoofantastico.controller;

import com.zoo.zoofantastico.mapper.CreatureMapper;
import com.zoo.zoofantastico.service.CreatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zoo.zoofantastico.dto.CreatureDTO;
import com.zoo.zoofantastico.dto.request.CreateCreatureRequest;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CreatureController {

    private final CreatureService service;

    public CreatureController(CreatureService service) {
        this.service = service;
    }

    @GetMapping("/creatures")
    public List<CreatureDTO> findAll() {
        return service.getAllCreatures()
                      .stream()
                      .map(CreatureMapper::toDTO)
                      .toList();
    }

    @PostMapping("/creatures")
    public ResponseEntity<CreatureDTO> create(@Valid @RequestBody CreateCreatureRequest req) {
        var saved = service.createCreature(req);
        return ResponseEntity
                .created(URI.create("/api/creatures/" + saved.getId()))
                .body(CreatureMapper.toDTO(saved));
    }

    @GetMapping("/creatures/{id}")
    public ResponseEntity<CreatureDTO> getById(@PathVariable Long id) {
        var c = service.getCreatureById(id);
        return ResponseEntity.ok(CreatureMapper.toDTO(c));
    }

    @PutMapping("/creatures/{id}")
    public ResponseEntity<CreatureDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateCreatureRequest req) {
        var updated = service.updateCreature(id, req);
        return ResponseEntity.ok(CreatureMapper.toDTO(updated));
    }

    @DeleteMapping("/creatures/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCreature(id);
        return ResponseEntity.noContent().build();
    }
}

