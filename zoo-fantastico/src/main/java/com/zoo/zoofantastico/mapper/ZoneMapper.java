package com.zoo.zoofantastico.mapper;

import com.zoo.zoofantastico.dto.ZoneDTO;
import com.zoo.zoofantastico.model.Zone;

public final class ZoneMapper {

    private ZoneMapper() {}

    public static ZoneDTO toDTO(Zone z) {
  if (z == null) return null;
  ZoneDTO dto = new ZoneDTO();
  dto.setId(z.getId());
  dto.setName(z.getName());
  dto.setDescription(z.getDescription());   
  dto.setCapacity(z.getCapacity());         
  return dto;
}

}