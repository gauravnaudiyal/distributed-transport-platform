package com.groupb.booking.model.entity;

import com.groupb.booking.model.enums.BookingState;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingState state;

    @Column(name = "map_version", length = 50)
    private String mapVersion;

    @Column(name = "coordinator_region", length = 100)
    private String coordinatorRegion;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "time_window_start", nullable = false)
    private Instant timeWindowStart;

    @Column(name = "time_window_end", nullable = false)
    private Instant timeWindowEnd;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BookingSegment> segments = new ArrayList<>();

    public Booking() {}

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public BookingState getState() { return state; }
    public void setState(BookingState state) { this.state = state; }

    public String getMapVersion() { return mapVersion; }
    public void setMapVersion(String mapVersion) { this.mapVersion = mapVersion; }

    public String getCoordinatorRegion() { return coordinatorRegion; }
    public void setCoordinatorRegion(String coordinatorRegion) { this.coordinatorRegion = coordinatorRegion; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(Instant timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public Instant getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(Instant timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<BookingSegment> getSegments() { return segments; }
    public void setSegments(List<BookingSegment> segments) { this.segments = segments; }
}
