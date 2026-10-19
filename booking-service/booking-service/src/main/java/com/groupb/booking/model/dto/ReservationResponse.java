package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationResponse {

    @JsonProperty("reservation_id")
    private String reservationId;

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("segment_id")
    private String segmentId;

    private String status;
    private String reason;

    public ReservationResponse() {}

    public ReservationResponse(String reservationId, String bookingId, String segmentId,
                               String status, String reason) {
        this.reservationId = reservationId;
        this.bookingId = bookingId;
        this.segmentId = segmentId;
        this.status = status;
        this.reason = reason;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
