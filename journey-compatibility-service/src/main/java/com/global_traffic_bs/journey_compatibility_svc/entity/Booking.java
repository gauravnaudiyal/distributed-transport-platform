package com.global_traffic_bs.journey_compatibility_svc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * New Booking entity mapped to the external Azure PostgreSQL database.
 * Represents bookings from the bkdemo123-postgres database.
 *
 * Schema (Azure PostgreSQL):
 * CREATE TABLE bookings (
 *     booking_id       VARCHAR(36)  PRIMARY KEY,
 *     user_id          VARCHAR(255) NOT NULL,
 *     origin           VARCHAR(255) NOT NULL,
 *     destination      VARCHAR(255) NOT NULL,
 *     state            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
 *     map_version      VARCHAR(50),
 *     coordinator_region VARCHAR(100),
 *     idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
 *     time_window_start TIMESTAMP   NOT NULL,
 *     time_window_end   TIMESTAMP   NOT NULL,
 *     created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
 *     updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
 * );
 */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_user_id", columnList = "user_id"),
        @Index(name = "idx_bookings_state", columnList = "state")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "origin", nullable = false, length = 255)
    private String origin;         // e.g., "DUBLIN"

    @Column(name = "destination", nullable = false, length = 255)
    private String destination;    // e.g., "CORK"

    @Column(name = "state", nullable = false, length = 20)
    private String state;          // PENDING, CONFIRMED, REJECTED, etc.

    @Column(name = "map_version", length = 50)
    private String mapVersion;     // e.g., "v2025.03"

    @Column(name = "coordinator_region", length = 100)
    private String coordinatorRegion;  // e.g., "eu-west-1"

    @Column(name = "idempotency_key", nullable = false, length = 255, unique = true)
    private String idempotencyKey;

    @Column(name = "time_window_start", nullable = false)
    private LocalDateTime timeWindowStart;

    @Column(name = "time_window_end", nullable = false)
    private LocalDateTime timeWindowEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

