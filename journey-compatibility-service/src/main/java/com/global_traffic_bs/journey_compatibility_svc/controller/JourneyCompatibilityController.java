package com.global_traffic_bs.journey_compatibility_svc.controller;

import com.global_traffic_bs.journey_compatibility_svc.dto.BookingRequest;
import com.global_traffic_bs.journey_compatibility_svc.dto.CompatibilityResponse;
import com.global_traffic_bs.journey_compatibility_svc.service.JourneyCompatibilityService;
import com.global_traffic_bs.journey_compatibility_svc.util.RouteNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JourneyCompatibilityController {

    private final JourneyCompatibilityService journeyCompatibilityService;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/v1/check-compatibility
     * Called by the Booking Service to validate route capacity for a journey.
     */
    @PostMapping("/check-compatibility")
    public ResponseEntity<CompatibilityResponse> checkCompatibility(
            @Valid @RequestBody BookingRequest request) {

        // Log the original request received
        try {
            String requestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("Received original request payload:\n{}", requestJson);
        } catch (Exception e) {
            log.warn("Failed to serialize request to JSON", e);
        }

        // Normalize route ID and route name to ensure consistent alphabetical ordering
        String originalRouteId = request.getRouteId();
        String originalRouteName = request.getRouteName();

        request.setRouteId(RouteNormalizer.normalizeRouteId(originalRouteId));
        request.setRouteName(RouteNormalizer.normalizeRouteName(originalRouteName));

        // Log if normalization occurred
        if (!originalRouteId.equals(request.getRouteId()) || !originalRouteName.equals(request.getRouteName())) {
            log.info("Route normalization applied:");
            if (!originalRouteId.equals(request.getRouteId())) {
                log.info("  routeId: '{}' -> '{}'", originalRouteId, request.getRouteId());
            }
            if (!originalRouteName.equals(request.getRouteName())) {
                log.info("  routeName: '{}' -> '{}'", originalRouteName, request.getRouteName());
            }
        }

        // Log the normalized request
        try {
            String normalizedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("Processing normalized request payload:\n{}", normalizedJson);
        } catch (Exception e) {
            log.warn("Failed to serialize normalized request to JSON", e);
        }

        log.info("Received compatibility check for bookingId={} route={} date={} time={}",
                request.getBookingId(), request.getRouteId(),
                request.getTravelDate(), request.getTravelTime());

        CompatibilityResponse response = journeyCompatibilityService.checkCompatibility(request);

        return switch (response.getStatus()) {
            case APPROVED -> ResponseEntity.ok(response);
            case REJECTED -> ResponseEntity.status(409).body(response); // Conflict
            default -> ResponseEntity.internalServerError().body(response);
        };
    }

    /**
     * GET /api/v1/health — lightweight liveness check beyond Actuator
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Journey Compatibility Service is running");
    }
}
