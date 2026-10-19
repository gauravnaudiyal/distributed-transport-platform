package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class BookingRequest {

    @JsonProperty("user_id")
    private String userId;

    private String origin;
    private String destination;

    @JsonProperty("time_window_start")
    private Instant timeWindowStart;

    @JsonProperty("time_window_end")
    private Instant timeWindowEnd;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    public BookingRequest() {}

    public BookingRequest(String userId, String origin, String destination,
                          Instant timeWindowStart, Instant timeWindowEnd, String idempotencyKey) {
        this.userId = userId;
        this.origin = origin;
        this.destination = destination;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.idempotencyKey = idempotencyKey;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Instant getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(Instant timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public Instant getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(Instant timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
