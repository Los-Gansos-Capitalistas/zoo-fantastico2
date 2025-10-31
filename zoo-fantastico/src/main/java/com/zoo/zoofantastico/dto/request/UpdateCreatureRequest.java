package com.zoo.zoofantastico.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import com.zoo.zoofantastico.validation.NullOrNotBlank;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateCreatureRequest {

  @NullOrNotBlank(message = "name must not be blank when provided")
  private String name;

  private String species;

  @DecimalMin(value = "0.1", inclusive = true, message = "size must be at least 0.1")
  private Double size;

  @Min(value = 1, message = "dangerLevel must be between 1 and 5")
  @Max(value = 5, message = "dangerLevel must be between 1 and 5")
  private Integer dangerLevel;

  private String healthStatus;

  private Long zoneId;
}
