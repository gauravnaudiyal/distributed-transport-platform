package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groupb.booking.model.enums.BookingState;

import java.time.Instant;

public class BookingResponse {

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("user_id")
    private String userId;

    private BookingState state;

    @JsonProperty("map_version")
    private String mapVersion;

    @JsonProperty("coordinator_region")
    private String coordinatorRegion;

    @JsonProperty("created_at")
    private Instant createdAt;

    public BookingResponse() {}

    public BookingResponse(String bookingId, String userId, BookingState state,
                           String mapVersion, String coordinatorRegion, Instant createdAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.state = state;
        this.mapVersion = mapVersion;
        this.coordinatorRegion = coordinatorRegion;
        this.createdAt = createdAt;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BookingState getState() { return state; }
    public void setState(BookingState state) { this.state = state; }

    public String getMapVersion() { return mapVersion; }
    public void setMapVersion(String mapVersion) { this.mapVersion = mapVersion; }

    public String getCoordinatorRegion() { return coordinatorRegion; }
    public void setCoordinatorRegion(String coordinatorRegion) { this.coordinatorRegion = coordinatorRegion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
