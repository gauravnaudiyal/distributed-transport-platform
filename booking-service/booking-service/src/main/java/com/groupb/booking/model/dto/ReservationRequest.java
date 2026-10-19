package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ReservationRequest {

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("region_id")
    private String regionId;

    @JsonProperty("time_window_start")
    private Instant timeWindowStart;

    @JsonProperty("time_window_end")
    private Instant timeWindowEnd;

    private String action;

    public ReservationRequest() {}

    public ReservationRequest(String bookingId, String segmentId, String regionId,
                              Instant timeWindowStart, Instant timeWindowEnd, String action) {
        this.bookingId = bookingId;
        this.segmentId = segmentId;
        this.regionId = regionId;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.action = action;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

    public String getRegionId() { return regionId; }
    public void setRegionId(String regionId) { this.regionId = regionId; }

    public Instant getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(Instant timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public Instant getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(Instant timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
