package com.groupb.booking.controller;

import com.groupb.booking.model.dto.BookingRequest;
import com.groupb.booking.model.dto.BookingResponse;
import com.groupb.booking.model.dto.VerificationProof;
import com.groupb.booking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        log.info("Received booking request from user {} ({} -> {})",
                request.getUserId(), request.getOrigin(), request.getDestination());

        BookingResponse response = bookingService.createBooking(request);
        HttpStatus status = response.getState().name().equals("CONFIRMED")
                ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingId) {
        log.info("Received cancellation request for booking {}", bookingId);
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}/verify")
    public ResponseEntity<VerificationProof> verifyBooking(@PathVariable String bookingId) {
        VerificationProof proof = bookingService.getVerificationProof(bookingId);
        return ResponseEntity.ok(proof);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBadState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        log.error("Unhandled exception in BookingController", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}
