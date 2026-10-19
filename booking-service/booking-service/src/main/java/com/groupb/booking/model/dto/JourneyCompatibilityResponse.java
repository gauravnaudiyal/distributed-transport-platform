package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JourneyCompatibilityResponse {

    @JsonProperty("bookingId")
    private String bookingId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("currentLoad")
    private Integer currentLoad;

    @JsonProperty("maxCapacity")
    private Integer maxCapacity;

    @JsonProperty("fallbackUsed")
    private Boolean fallbackUsed;

    public JourneyCompatibilityResponse() {}

    public JourneyCompatibilityResponse(String bookingId, String status, String message,
                                         Integer currentLoad, Integer maxCapacity, Boolean fallbackUsed) {
        this.bookingId = bookingId;
        this.status = status;
        this.message = message;
        this.currentLoad = currentLoad;
        this.maxCapacity = maxCapacity;
        this.fallbackUsed = fallbackUsed;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(Integer currentLoad) { this.currentLoad = currentLoad; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public Boolean getFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(Boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}

