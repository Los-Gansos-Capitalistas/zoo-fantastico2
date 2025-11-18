package com.zoo.zoofantastico.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorResponse {
@JsonFormat(shape = JsonFormat.Shape.STRING)
private Instant timestamp;
private int status;
private String error;
private String message;
private String path;


private String traceId; // para correlacionar logs/requests


@Builder.Default
private java.util.List<FieldErrorDTO> fieldErrors = java.util.List.of();

}