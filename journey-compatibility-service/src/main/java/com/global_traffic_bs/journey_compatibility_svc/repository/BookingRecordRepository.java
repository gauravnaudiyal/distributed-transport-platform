package com.global_traffic_bs.journey_compatibility_svc.repository;

import com.global_traffic_bs.journey_compatibility_svc.entity.BookingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Legacy repository for BookingRecord entity.
 * Used when NOT using the external booking database.
 * Maps to the legacy booking_record table in the primary database.
 */
@Repository
public interface BookingRecordRepository extends JpaRepository<BookingRecord, String> {

    /**
     * Count approved bookings for a given route, date, time window and vehicle type.
     * Used for fallback capacity check and reconciliation.
     */
    @Query("""
        SELECT COUNT(b) FROM BookingRecord b
        WHERE b.routeId = :routeId
          AND b.travelDate = :travelDate
          AND b.travelTime >= :windowStart
          AND b.travelTime < :windowEnd
          AND b.vehicleType = :vehicleType
          AND b.status = 'APPROVED'
        """)
    long countApprovedBookings(
            @Param("routeId") String routeId,
            @Param("travelDate") LocalDate travelDate,
            @Param("windowStart") LocalTime windowStart,
            @Param("windowEnd") LocalTime windowEnd,
            @Param("vehicleType") String vehicleType
    );

    boolean existsByBookingId(String bookingId);
}


