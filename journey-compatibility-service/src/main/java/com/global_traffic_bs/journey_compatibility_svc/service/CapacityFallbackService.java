package com.global_traffic_bs.journey_compatibility_svc.service;

import com.global_traffic_bs.journey_compatibility_svc.dto.BookingRequest;
import com.global_traffic_bs.journey_compatibility_svc.entity.RouteCapacity;
import com.global_traffic_bs.journey_compatibility_svc.repository.BookingRecordRepository;
import com.global_traffic_bs.journey_compatibility_svc.repository.RouteCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Fallback service used when Redis is unavailable (circuit breaker open).
 * Reads current booking count directly from PostgreSQL and compares
 * against the max_capacity from route_capacity_policy.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapacityFallbackService {

    private final BookingRecordRepository bookingRecordRepository;
    private final RouteCapacityRepository routeCapacityRepository;

    @Value("${app.route.default-capacity.car:100}")
    private int defaultCapacity;

    /**
     * Checks if capacity is available using the DB (fallback path).
     *
     * @return true if a slot is available
     */
    public boolean hasCapacity(BookingRequest request) {
        log.warn("FALLBACK activated for routeId={} date={} time={} vehicle={}",
                request.getRouteId(), request.getTravelDate(),
                request.getTravelTime(), request.getVehicleType());

        int maxCapacity = resolveMaxCapacity(
                request.getRouteId(),
                request.getTravelDate().toString(),
                request.getTravelTime().toString(),
                request.getVehicleType());

        // Count approved bookings in the 1-hour window containing this travel time
        LocalTime windowStart = request.getTravelTime().withMinute(0).withSecond(0).withNano(0);
        LocalTime windowEnd = windowStart.plusHours(1);

        long currentCount = bookingRecordRepository.countApprovedBookings(
                request.getRouteId(),
                request.getTravelDate(),
                windowStart,
                windowEnd,
                request.getVehicleType()
        );

        log.info("DB fallback — current={}/{} for route={} window={}–{}",
                currentCount, maxCapacity, request.getRouteId(), windowStart, windowEnd);

        return currentCount < maxCapacity;
    }

    /**
     * Looks up max capacity from route_capacity_policy.
     * Falls back to configured default if no matching policy is found.
     */
    public int resolveMaxCapacity(String routeId, String travelDate,
                                   String travelTime, String vehicleType) {
        LocalDate date = LocalDate.parse(travelDate);
        LocalTime time = LocalTime.parse(travelTime);

        return routeCapacityRepository
                .findActivePolicy(routeId, vehicleType.toUpperCase(), date, time)
                .map(RouteCapacity::getMaxCapacity)
                .orElseGet(() -> {
                    log.warn("No capacity policy for route={} vehicle={} — using default={}",
                            routeId, vehicleType, defaultCapacity);
                    return defaultCapacity;
                });
    }
}
