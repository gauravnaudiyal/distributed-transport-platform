package com.groupb.booking.model.entity;

import com.groupb.booking.model.enums.BookingEventType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private BookingEventType eventType;

    @Column(name = "booking_id", nullable = false, length = 36)
    private String bookingId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "map_version", length = 50)
    private String mapVersion;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OutboxEvent() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public BookingEventType getEventType() { return eventType; }
    public void setEventType(BookingEventType eventType) { this.eventType = eventType; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMapVersion() { return mapVersion; }
    public void setMapVersion(String mapVersion) { this.mapVersion = mapVersion; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
