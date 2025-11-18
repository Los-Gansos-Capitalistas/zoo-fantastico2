package com.zoo.zoofantastico.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateCreatureRequest {

  @NotBlank(message = "name must not be blank")
  private String name;

  @NotBlank(message = "species must not be blank")
  private String species;

  @NotNull
  @DecimalMin(value = "0.1", inclusive = true, message = "size must be at least 0.1")
  private Double size;

  @NotNull
  @Min(value = 1, message = "dangerLevel must be between 1 and 5")
  @Max(value = 5, message = "dangerLevel must be between 1 and 5")
  private Integer dangerLevel;

  @NotBlank(message = "healthStatus must not be blank")
  private String healthStatus;

  @jakarta.validation.constraints.NotNull(message = "zoneId must not be null")
  private Long zoneId;
}
