package com.global_traffic_bs.journey_compatibility_svc.repository;

import com.global_traffic_bs.journey_compatibility_svc.entity.RouteCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteCapacityRepository extends JpaRepository<RouteCapacity, UUID> {

    /**
     * Finds the active capacity policy for a route, vehicle type, and travel time.
     *
     * Conditions:
     *  - routeId and vehicleType must match
     *  - travelTime must fall within window_start (inclusive) and window_end (exclusive)
     *  - policy must be effective on travelDate (effective_from <= date <= effective_to, or effective_to is null)
     */
    @Query("""
        SELECT r FROM RouteCapacity r
        WHERE r.routeId = :routeId
          AND r.vehicleType = :vehicleType
          AND r.windowStart <= :travelTime
          AND r.windowEnd > :travelTime
          AND r.effectiveFrom <= :travelDate
          AND (r.effectiveTo IS NULL OR r.effectiveTo >= :travelDate)
        ORDER BY r.effectiveFrom DESC
        """)
    Optional<RouteCapacity> findActivePolicy(
            @Param("routeId") String routeId,
            @Param("vehicleType") String vehicleType,
            @Param("travelDate") LocalDate travelDate,
            @Param("travelTime") LocalTime travelTime
    );

    /**
     * Finds the active capacity policy by origin and destination (source/destination based).
     * Used for new booking schema that uses origin/destination instead of routeId.
     *
     * @param origin source location (e.g., "DUBLIN")
     * @param destination target location (e.g., "CORK")
     * @param vehicleType vehicle type (e.g., "CAR")
     * @param travelDate date to check policy effectiveness
     * @param travelTime time to check window match
     * @return active policy if found
     */
    @Query("""
        SELECT r FROM RouteCapacity r
        WHERE (r.routeId = CONCAT(:origin, '-', :destination) OR r.routeId = CONCAT(:destination, '-', :origin))
          AND r.vehicleType = :vehicleType
          AND r.windowStart <= :travelTime
          AND r.windowEnd > :travelTime
          AND r.effectiveFrom <= :travelDate
          AND (r.effectiveTo IS NULL OR r.effectiveTo >= :travelDate)
        ORDER BY r.effectiveFrom DESC
        """)
    Optional<RouteCapacity> findActivePolicyByOriginDestination(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("vehicleType") String vehicleType,
            @Param("travelDate") LocalDate travelDate,
            @Param("travelTime") LocalTime travelTime
    );
}
