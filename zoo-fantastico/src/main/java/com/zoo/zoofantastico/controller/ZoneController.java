package com.zoo.zoofantastico.controller;

import com.zoo.zoofantastico.model.Zone;
import com.zoo.zoofantastico.service.ZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zoo.zoofantastico.dto.ZoneDTO;
import com.zoo.zoofantastico.dto.request.CreateZoneRequest;
import com.zoo.zoofantastico.dto.request.UpdateZoneRequest;
import com.zoo.zoofantastico.mapper.ZoneMapper;
import jakarta.validation.Valid;
import com.zoo.zoofantastico.dto.ZoneSummaryDTO;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService service;

    public ZoneController(ZoneService service) { this.service = service; }

@GetMapping("/summary")
public List<ZoneSummaryDTO> summary() {
  return service.findSummary();
}

@PostMapping
public ResponseEntity<ZoneDTO> create(@Valid @RequestBody CreateZoneRequest req) {
    Zone saved = service.create(req); // pásale el DTO al service
    return ResponseEntity
            .created(URI.create("/api/zones/" + saved.getId()))
            .body(ZoneMapper.toDTO(saved));
}



@GetMapping("/{id}")
public ResponseEntity<ZoneDTO> findOne(@PathVariable Long id) {
    return ResponseEntity.ok(ZoneMapper.toDTO(service.findOne(id)));
}


@PutMapping("/{id}")
public ResponseEntity<ZoneDTO> update(@PathVariable Long id,
                                      @Valid @RequestBody UpdateZoneRequest req) {
    Zone updated = service.update(id, req); //  pásale el DTO al service
    return ResponseEntity.ok(ZoneMapper.toDTO(updated));
}



    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    
@GetMapping
public List<ZoneSummaryDTO> findAll() {
    return service.findSummary();
}

}
