package com.global_traffic_bs.journey_compatibility_svc.config;

import com.global_traffic_bs.journey_compatibility_svc.dto.CompatibilityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation failures — returns 400 with field-level error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (a, b) -> a
                ));

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.badRequest().body(Map.of(
                "status", "VALIDATION_ERROR",
                "errors", fieldErrors
        ));
    }

    /**
     * Handles unexpected runtime errors — returns 500 with error details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CompatibilityResponse> handleGeneralError(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        CompatibilityResponse response = CompatibilityResponse.builder()
                .status(CompatibilityResponse.CompatibilityStatus.ERROR)
                .message("An internal error occurred: " + ex.getMessage())
                .build();

        return ResponseEntity.internalServerError().body(response);
    }
}
