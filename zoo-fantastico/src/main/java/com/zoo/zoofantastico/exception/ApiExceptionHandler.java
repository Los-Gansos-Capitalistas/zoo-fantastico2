package com.zoo.zoofantastico.exception;

import com.zoo.zoofantastico.dto.ErrorResponse;
import com.zoo.zoofantastico.dto.FieldErrorDTO;
import org.slf4j.MDC;
import java.util.UUID;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

private ErrorResponse buildError(HttpServletRequest request,
                                 HttpStatus status,
                                 String error,
                                 String message,
                                 List<FieldErrorDTO> fields) {

  String traceId = (String) request.getAttribute("traceId");
  if (traceId == null || traceId.isBlank()) {
    traceId = MDC.get("traceId");
  }
  if (traceId == null || traceId.isBlank()) {
    traceId = UUID.randomUUID().toString(); // <-- asegura no-null
  }

  return ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(status.value())
      .error(error)
      .message(message)
      .path(request.getRequestURI())
      .traceId(traceId)
      .fieldErrors(fields == null ? List.of() : fields) // <-- lista vacía, no null
      .build();
}

@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(buildError(req, HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), List.of()));
}


@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
  return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(buildError(req, HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null));
}

@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
  return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
      .body(buildError(req, HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage(), null));
}



@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<FieldErrorDTO> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new FieldErrorDTO(fe.getField(), fe.getDefaultMessage()))
            .toList();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildError(
                    req,
                    HttpStatus.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST.getReasonPhrase(), // "Bad Request"
                    "Validation failed",                       // <-- clave para pasar el test
                    details));
}



@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
  String msg = ex.getConstraintViolations().stream()
      .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
      .findFirst()
      .orElse("Constraint violation");
  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(buildError(req, HttpStatus.BAD_REQUEST, "Bad Request", msg, null));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
  ErrorResponse body = buildError(
      request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
      "Unexpected error", List.of());
  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
}

@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleDataIntegrity(
    org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest req) {

  String raw = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
  String message = "Data integrity violation";
  if (raw != null && raw.toLowerCase().contains("duplicate")) {
    message = "Zone name already exists";
  }

  return ResponseEntity.status(HttpStatus.CONFLICT)
      .body(buildError(req, HttpStatus.CONFLICT, "Conflict", message, null));
}


}
