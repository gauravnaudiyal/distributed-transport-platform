package com.global_traffic_bs.journey_compatibility_svc.service;

import com.global_traffic_bs.journey_compatibility_svc.entity.RouteCapacity;
import com.global_traffic_bs.journey_compatibility_svc.repository.BookingRecordRepository;
import com.global_traffic_bs.journey_compatibility_svc.repository.booking.BookingRepository;
import com.global_traffic_bs.journey_compatibility_svc.repository.RouteCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

/**
 * Enhanced Reconciliation Service supporting multiple database sources.
 *
 * Periodically reconciles Redis capacity counters against PostgreSQL sources of truth.
 * Corrects drift caused by Redis restarts, mid-flight crashes or split-brain scenarios.
 *
 * Supports two independent booking data sources:
 * 1. Legacy BookingRecord (primary DB) - used for internal audit
 * 2. External Booking (Azure PostgreSQL) - new canonical booking source
 *
 * The service detects which source to use based on feature/use case configuration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final RouteCapacityRepository routeCapacityRepository;
    private final BookingRecordRepository bookingRecordRepository;
    private final BookingRepository bookingRepository;
    private final RedisCapacityService redisCapacityService;

    @Value("${app.reconciliation.interval-ms:3600000}")
    private long reconciliationIntervalMs;

    @Value("${app.reconciliation.use-external-booking-db:false}")
    private boolean useExternalBookingDb;

    @Value("${app.reconciliation.log-queries:false}")
    private boolean logQueries;

    @Scheduled(fixedDelayString = "${app.reconciliation.interval-ms:3600000}")
    public void reconcile() {
        log.info("Starting reconciliation run (scanning 7-day lookahead, using {} source)...",
                useExternalBookingDb ? "EXTERNAL BOOKING DB" : "LEGACY DB");

        List<RouteCapacity> allPolicies = routeCapacityRepository.findAll();
        log.info("Found {} policies to evaluate.", allPolicies.size());

        LocalDate today = LocalDate.now();
        int evaluations = 0;
        int driftsDetected = 0;

        for (int i = 0; i <= 7; i++) {
            LocalDate targetDate = today.plusDays(i);
            for (RouteCapacity policy : allPolicies) {
                // Check if policy is active on this specific date
                boolean isActiveOnDate = !policy.getEffectiveFrom().isAfter(targetDate)
                        && (policy.getEffectiveTo() == null || !policy.getEffectiveTo().isBefore(targetDate));

                if (isActiveOnDate) {
                    if (reconcileWindow(policy, targetDate)) {
                        driftsDetected++;
                    }
                    evaluations++;
                }
            }
        }

        log.info("Reconciliation run complete. Evaluated {} policy-date windows, {} drifts detected.",
                evaluations, driftsDetected);
    }

    /**
     * Reconciles all time buckets for a given policy and date.
     *
     * @return true if any drift was detected and corrected in this window
     */
    private boolean reconcileWindow(RouteCapacity policy, LocalDate date) {
        String routeId = policy.getRouteId();
        String vehicleType = policy.getVehicleType();
        boolean driftFound = false;

        // Align to the start of the hour for the first bucket.
        // RedisCapacityService uses 1-hour buckets, so we must reconcile each bucket
        // that overlaps with the policy window.
        LocalTime bucket = policy.getWindowStart().withMinute(0).withSecond(0).withNano(0);

        log.debug("Reconciling buckets for route={} policy={}-{}", routeId, policy.getWindowStart(), policy.getWindowEnd());

        while (bucket.isBefore(policy.getWindowEnd())) {
            if (reconcileBucket(routeId, date, bucket, vehicleType)) {
                driftFound = true;
            }
            if (bucket.getHour() == 23) break; // End of day
            bucket = bucket.plusHours(1);
        }

        return driftFound;
    }

    /**
     * Reconciles a single time bucket across the policy window.
     *
     * @return true if drift was detected and corrected
     */
    private boolean reconcileBucket(String routeId, LocalDate date, LocalTime bucketStart,
                                    String vehicleType) {
        LocalTime bucketEnd = (bucketStart.getHour() == 23) ? LocalTime.MAX : bucketStart.plusHours(1);

        try {
            long dbCount;

            if (useExternalBookingDb) {
                // Use external Booking database (Azure PostgreSQL)
                logExternalQuery(routeId, date, bucketStart, bucketEnd);
                dbCount = countBookingsFromExternalDb(routeId, date, bucketStart, bucketEnd);
            } else {
                // Use legacy BookingRecord database (primary DB)
                logLegacyQuery(routeId, date, bucketStart, bucketEnd, vehicleType);
                dbCount = bookingRecordRepository.countApprovedBookings(
                        routeId, date, bucketStart, bucketEnd, vehicleType);
            }

            logRedisRead(routeId, date, bucketStart, vehicleType);
            Long redisCount = redisCapacityService.getCurrentCount(
                    routeId, date, bucketStart, vehicleType);

            // A missing key is treated as 0 and will be populated if dbCount > 0.
            long currentRedisCount = (redisCount != null) ? redisCount : 0L;

            if (currentRedisCount != dbCount) {
                log.warn("Drift detected for route={} date={} bucket={}: Redis={}, DB={}. Synchronizing Redis with DB.",
                        routeId, date, bucketStart, currentRedisCount, dbCount);
                logRedisWrite(routeId, date, bucketStart, vehicleType, dbCount);
                redisCapacityService.setCount(routeId, date, bucketStart, vehicleType, dbCount);
                return true;
            } else {
                log.debug("Counts are synchronized for route={} bucket={}: [Redis={}, DB={}]",
                        routeId, bucketStart, currentRedisCount, dbCount);
                return false;
            }

        } catch (Exception e) {
            log.error("Error during reconciliation for route={} bucket={}: {}", routeId, bucketStart, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Counts confirmed bookings from external Booking database using origin/destination.
     *
     * The external database schema uses:
     * - origin: source location (e.g., "DUBLIN")
     * - destination: target location (e.g., "CORK")
     * - time_window_start/time_window_end: timestamp-based windows
     * - state: booking state (e.g., "CONFIRMED")
     *
     * @return count of confirmed bookings matching the route and time window
     */
    private long countBookingsFromExternalDb(String routeId, LocalDate date, LocalTime bucketStart,
                                             LocalTime bucketEnd) {
        String[] endpoints = resolveOriginDestination(routeId);
        if (endpoints == null) {
            log.warn("Unable to parse origin/destination from routeId={}", routeId);
            return 0;
        }
        String origin = endpoints[0];
        String destination = endpoints[1];

        // Create timestamp windows (combine date with time window)
        LocalDateTime windowStart = LocalDateTime.of(date, bucketStart);
        LocalDateTime windowEnd = LocalDateTime.of(date, bucketEnd);

        log.debug("Counting bookings from external DB: origin={} destination={} window={} to {}",
                origin, destination, windowStart, windowEnd);

        // Query external booking database for confirmed bookings
        long count = bookingRepository.countBookingsByRouteAndTimeWindow(
                origin, destination, windowStart, windowEnd, "CONFIRMED");

        log.debug("External DB booking count: origin={} destination={} count={}", origin, destination, count);
        return count;
    }

    private String[] resolveOriginDestination(String routeId) {
        if (routeId == null || routeId.isBlank()) {
            return null;
        }

        String normalized = routeId.trim().toUpperCase(Locale.ROOT);
        String[] parts = normalized.split("-");
        // Required format for policy route_id: ROUTE-SOURCE-DESTINATION (e.g., ROUTE-DUB-CORK)
        if (parts.length < 3) {
            return null;
        }

        return new String[]{normalizeLocation(parts[1]), normalizeLocation(parts[2])};
    }


    private String normalizeLocation(String location) {
        String value = location.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "DUB" -> "DUBLIN";
            case "GAL" -> "GALWAY";
            case "LIM" -> "LIMERICK";
            default -> value;
        };
    }

    private void logLegacyQuery(String routeId, LocalDate date, LocalTime bucketStart,
                                LocalTime bucketEnd, String vehicleType) {
        if (!logQueries) {
            return;
        }
        log.info("[reconciliation-query][LEGACY-DB] SELECT COUNT(*) FROM booking_record " +
                        "WHERE route_id='{}' AND travel_date='{}' AND travel_time>='{}' AND travel_time<'{}' " +
                        "AND vehicle_type='{}' AND status='APPROVED'",
                routeId, date, bucketStart, bucketEnd, vehicleType);
    }

    private void logExternalQuery(String routeId, LocalDate date, LocalTime bucketStart, LocalTime bucketEnd) {
        if (!logQueries) {
            return;
        }
        log.info("[reconciliation-query][EXTERNAL-DB] SELECT COUNT(*) FROM bookings " +
                        "WHERE origin,destination derived from route_id='{}' AND state='CONFIRMED' " +
                        "AND time_window_start>='{}T{}' AND time_window_start<'{}T{}'",
                routeId, date, bucketStart, date, bucketEnd);
    }

    private void logRedisRead(String routeId, LocalDate date, LocalTime bucketStart, String vehicleType) {
        if (!logQueries) {
            return;
        }
        String key = redisCapacityService.buildCapacityKey(routeId, date, bucketStart, vehicleType);
        log.info("[reconciliation-query][REDIS] GET {}", key);
    }

    private void logRedisWrite(String routeId, LocalDate date, LocalTime bucketStart,
                               String vehicleType, long dbCount) {
        if (!logQueries) {
            return;
        }
        String key = redisCapacityService.buildCapacityKey(routeId, date, bucketStart, vehicleType);
        log.info("[reconciliation-query][REDIS] SET {} {} (with TTL)", key, dbCount);
    }
}
