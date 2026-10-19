package com.global_traffic_bs.journey_compatibility_svc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "booking_record", indexes = {
        @Index(name = "idx_route_date_time", columnList = "route_id, travel_date, travel_time"),
        @Index(name = "idx_booking_id", columnList = "booking_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRecord {

    @Id
    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "travel_time", nullable = false)
    private LocalTime travelTime;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @Column(name = "passenger_count", nullable = false)
    private int passengerCount;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "status", nullable = false)
    private String status; // APPROVED, REJECTED

    @Column(name = "fallback_used", nullable = false)
    private boolean fallbackUsed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
