package com.gtbs.adminauthority.api;

import com.gtbs.adminauthority.api.dto.ErrorResponse;
import com.gtbs.adminauthority.integration.booking.BookingServiceException;
import com.gtbs.adminauthority.integration.rms.RouteManagementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> badRequest(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of("VALIDATION", msg));
    }

    @ExceptionHandler(RouteManagementException.class)
    public ResponseEntity<ErrorResponse> rmsDown(RouteManagementException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of("ROUTE_MANAGEMENT", ex.getMessage()));
    }

    @ExceptionHandler(BookingServiceException.class)
    public ResponseEntity<ErrorResponse> bookingError(BookingServiceException ex) {
        if (ex.getCause() instanceof RestClientResponseException rce && rce.getStatusCode().value() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("BOOKING_NOT_FOUND", "Booking not found"));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of("BOOKING_SERVICE", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> fallback(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL", "Unexpected error"));
    }
}
