package com.zoo.zoofantastico.mapper;

import com.zoo.zoofantastico.dto.CreatureDTO;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;
import com.zoo.zoofantastico.model.Creature;

public final class CreatureMapper {

  private CreatureMapper() {}

  /** Entidad -> DTO de respuesta */
  public static CreatureDTO toDTO(Creature c) {
    if (c == null) return null;
    return CreatureDTO.builder()
        .id(c.getId())
        .name(c.getName())
        .species(c.getSpecies())
        .size(c.getSize())
        .dangerLevel(c.getDangerLevel())
        .healthStatus(c.getHealthStatus())
        .zone(ZoneMapper.toDTO(c.getZone()))
        .build();
  }

  /**
   * Copia campos del request a la entidad.
   * (La reasignación de zona por zoneId se hace en el Service.)
   */
  public static void applyUpdate(Creature target, UpdateCreatureRequest req) {
    if (target == null || req == null) return;

    if (req.getName() != null)         target.setName(req.getName());
    if (req.getSpecies() != null)      target.setSpecies(req.getSpecies());
    if (req.getSize() != null)         target.setSize(req.getSize());
    if (req.getDangerLevel() != null)  target.setDangerLevel(req.getDangerLevel());
    if (req.getHealthStatus() != null) target.setHealthStatus(req.getHealthStatus());
    // zoneId se procesa en el servicio.
  }
}

