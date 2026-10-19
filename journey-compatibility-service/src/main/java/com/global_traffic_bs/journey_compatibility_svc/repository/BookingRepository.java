package com.global_traffic_bs.journey_compatibility_svc.repository.booking;

import com.global_traffic_bs.journey_compatibility_svc.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

/**
 * Repository for the external Booking entity (Azure PostgreSQL database).
 * Queries the new bookings table schema.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    /**
     * Count confirmed bookings for a given route (origin-destination) within a time window.
     * Used for fallback capacity check and reconciliation.
     *
     * @param origin source location (e.g., "DUBLIN")
     * @param destination target location (e.g., "CORK")
     * @param windowStart time window start (inclusive)
     * @param windowEnd time window end (exclusive)
     * @param state booking state to count (e.g., "CONFIRMED")
     * @return count of bookings matching criteria
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE UPPER(b.origin) = UPPER(:origin)
          AND UPPER(b.destination) = UPPER(:destination)
          AND b.timeWindowStart >= :windowStart
          AND b.timeWindowStart < :windowEnd
          AND UPPER(b.state) = UPPER(:state)
        """)
    long countBookingsByRouteAndTimeWindow(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("state") String state
    );

    /**
     * Find a booking by idempotency key for duplicate detection.
     *
     * @param idempotencyKey unique request identifier
     * @return true if booking with this key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find a booking by idempotency key.
     *
     * @param idempotencyKey unique request identifier
     * @return Booking if found
     */
    Booking findByIdempotencyKey(String idempotencyKey);
}

