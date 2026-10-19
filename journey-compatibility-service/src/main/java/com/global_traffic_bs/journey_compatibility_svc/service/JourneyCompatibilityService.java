package com.global_traffic_bs.journey_compatibility_svc.service;

import com.global_traffic_bs.journey_compatibility_svc.dto.BookingRequest;
import com.global_traffic_bs.journey_compatibility_svc.dto.CompatibilityResponse;
import com.global_traffic_bs.journey_compatibility_svc.entity.BookingRecord;
import com.global_traffic_bs.journey_compatibility_svc.repository.BookingRecordRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class JourneyCompatibilityService {

    private final RedisCapacityService redisCapacityService;
    private final CapacityFallbackService capacityFallbackService;
    private final BookingRecordRepository bookingRecordRepository;

    @Autowired
    @Lazy
    private JourneyCompatibilityService self;

    /**
     * Main entry point: validates journey compatibility.
     * 1. Checks idempotency (already processed?)
     * 2. Tries Redis reservation (with circuit breaker)
     * 3. Falls back to DB on Redis failure
     * 4. Persists result asynchronously
     */
    public CompatibilityResponse checkCompatibility(BookingRequest request) {

        // --- Idempotency check ---
        if (bookingRecordRepository.existsByBookingId(request.getBookingId())) {
            log.info("Duplicate request — bookingId={} already processed", request.getBookingId());
            BookingRecord existing = bookingRecordRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Inconsistent state: booking exists but not found"));
            return buildResponseFromRecord(existing);
        }

        // --- Resolve max capacity from active route policy (with Redis caching) ---
        Integer cachedMaxCapacity = redisCapacityService.getPolicyCapacity(
                request.getRouteId(),
                request.getTravelDate(),
                request.getTravelTime(),
                request.getVehicleType());

        int maxCapacity;
        if (cachedMaxCapacity != null) {
            maxCapacity = cachedMaxCapacity;
            log.debug("Cache hit for route policy: routeId={}, maxCapacity={}", request.getRouteId(), maxCapacity);
        } else {
            maxCapacity = capacityFallbackService.resolveMaxCapacity(
                    request.getRouteId(),
                    request.getTravelDate().toString(),
                    request.getTravelTime().toString(),
                    request.getVehicleType());

            redisCapacityService.setPolicyCapacity(
                    request.getRouteId(),
                    request.getTravelDate(),
                    request.getTravelTime(),
                    request.getVehicleType(),
                    maxCapacity);
            log.info("Cache miss for route policy: routeId={}, maxCapacity={} (fetched from DB)", request.getRouteId(), maxCapacity);
        }

        // --- Try Redis (with circuit breaker + fallback) ---
        return self.checkWithRedisAndFallback(request, maxCapacity);
    }

    @CircuitBreaker(name = "redisCapacity", fallbackMethod = "fallbackToDatabase")
    public CompatibilityResponse checkWithRedisAndFallback(BookingRequest request, int maxCapacity) {
        long slotCount = redisCapacityService.tryReserveSlot(
                request.getRouteId(),
                request.getTravelDate(),
                request.getTravelTime(),
                request.getVehicleType(),
                maxCapacity
        );

        boolean approved = slotCount >= 0;
        CompatibilityResponse response = buildResponse(request, approved, slotCount, maxCapacity, false);
        self.persistAsync(request, response);
        return response;
    }

    /**
     * Fallback method invoked by Resilience4j when Redis circuit opens.
     */
    @SuppressWarnings("unused")
    private CompatibilityResponse fallbackToDatabase(BookingRequest request, int maxCapacity, Throwable t) {
        log.error("Circuit breaker opened for Redis — using DB fallback. Cause: {}", t.getMessage());
        boolean available = capacityFallbackService.hasCapacity(request);
        CompatibilityResponse response = buildResponse(request, available, -1L, maxCapacity, true);
        self.persistAsync(request, response);
        return response;
    }

    /**
     * Persists the booking record asynchronously so the API response is not blocked by DB write.
     */
    @Async
    public void persistAsync(BookingRequest request, CompatibilityResponse response) {
        if (CompatibilityResponse.CompatibilityStatus.APPROVED.equals(response.getStatus())) {
            log.debug("Booking approved, skipping DB persistence for bookingId={}", request.getBookingId());
            return;
        }

        try {
            BookingRecord record = BookingRecord.builder()
                    .bookingId(request.getBookingId())
                    .routeId(request.getRouteId())
                    .routeName(request.getRouteName())
                    .travelDate(request.getTravelDate())
                    .travelTime(request.getTravelTime())
                    .vehicleType(request.getVehicleType())
                    .passengerCount(request.getPassengerCount())
                    .driverId(request.getDriverId())
                    .status(response.getStatus().name())
                    .fallbackUsed(response.isFallbackUsed())
                    .createdAt(LocalDateTime.now())
                    .build();

            bookingRecordRepository.save(record);
            log.debug("Persisted booking record for bookingId={}", request.getBookingId());
        } catch (Exception e) {
            log.error("Failed to persist booking record for bookingId={}: {}", request.getBookingId(), e.getMessage());
        }
    }

    private CompatibilityResponse buildResponse(BookingRequest request, boolean approved,
                                                 long currentLoad, int maxCapacity, boolean fallbackUsed) {
        return CompatibilityResponse.builder()
                .bookingId(request.getBookingId())
                .status(approved
                        ? CompatibilityResponse.CompatibilityStatus.APPROVED
                        : CompatibilityResponse.CompatibilityStatus.REJECTED)
                .message(approved
                        ? "Journey slot reserved successfully"
                        : "Route capacity exceeded for the requested time window")
                .currentLoad(currentLoad >= 0 ? currentLoad : 0)
                .maxCapacity(maxCapacity)
                .fallbackUsed(fallbackUsed)
                .build();
    }

    private CompatibilityResponse buildResponseFromRecord(BookingRecord record) {
        return CompatibilityResponse.builder()
                .bookingId(record.getBookingId())
                .status(CompatibilityResponse.CompatibilityStatus.valueOf(record.getStatus()))
                .message("Duplicate request — returning previously processed result")
                .fallbackUsed(record.isFallbackUsed())
                .build();
    }
}
