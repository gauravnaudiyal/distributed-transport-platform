package com.global_traffic_bs.journey_compatibility_svc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "route_capacity_policy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteCapacity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "route_id", nullable = false, length = 50)
    private String routeId;                 // e.g. "ROUTE-DUB-CORK"

    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType;             // CAR, BUS, TRUCK

    @Column(name = "window_start", nullable = false)
    private LocalTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalTime windowEnd;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;          // NULL = open-ended

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
