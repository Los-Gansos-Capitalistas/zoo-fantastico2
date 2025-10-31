package com.zoo.zoofantastico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Instant;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    public record FieldErrorDTO(String field, String message) {}
    public record ErrorDTO(Instant timestamp, int status, String error, String message, String path, String traceId, List<FieldErrorDTO> fieldErrors) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex, ServletWebRequest req) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldErrorDTO(fe.getField(), StringUtils.hasText(fe.getDefaultMessage()) ? fe.getDefaultMessage() : "Invalid value"))
                .toList();

        var dto = new ErrorDTO(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",                               // <- mensaje fijo
                req.getRequest().getRequestURI(),
                req.getRequest().getHeader("X-Request-Id"),        // si no hay, quedará null
                fieldErrors
        );
        return ResponseEntity.badRequest().body(dto);
    }
}
