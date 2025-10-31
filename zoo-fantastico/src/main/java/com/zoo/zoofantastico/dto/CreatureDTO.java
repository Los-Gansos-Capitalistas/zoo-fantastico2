package com.zoo.zoofantastico.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreatureDTO {
    private Long id;
    private String name;
    private String species;
    private Double size;
    private Integer dangerLevel;
    private String healthStatus;
    private ZoneDTO zone;
}
