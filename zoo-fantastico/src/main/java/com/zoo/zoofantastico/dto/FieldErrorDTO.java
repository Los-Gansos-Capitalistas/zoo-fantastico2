package com.zoo.zoofantastico.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FieldErrorDTO {
private String field;
private String message;
}