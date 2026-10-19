package com.groupb.booking.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "booking_segments")
public class BookingSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "segment_id", nullable = false, length = 100)
    private String segmentId;

    @Column(name = "authoritative_region", nullable = false, length = 100)
    private String authoritativeRegion;

    @Column(name = "reservation_status", nullable = false, length = 20)
    private String reservationStatus;

    @Column(name = "time_window_start", nullable = false)
    private Instant timeWindowStart;

    @Column(name = "time_window_end", nullable = false)
    private Instant timeWindowEnd;

    public BookingSegment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

    public String getAuthoritativeRegion() { return authoritativeRegion; }
    public void setAuthoritativeRegion(String authoritativeRegion) { this.authoritativeRegion = authoritativeRegion; }

    public String getReservationStatus() { return reservationStatus; }
    public void setReservationStatus(String reservationStatus) { this.reservationStatus = reservationStatus; }

    public Instant getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(Instant timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public Instant getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(Instant timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }
}
