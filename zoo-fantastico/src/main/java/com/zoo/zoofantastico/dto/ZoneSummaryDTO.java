package com.zoo.zoofantastico.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ZoneSummaryDTO {
  private Long id;
  private String name;
  private String description;
  private Integer capacity;
  private Long creaturesCount;
}

