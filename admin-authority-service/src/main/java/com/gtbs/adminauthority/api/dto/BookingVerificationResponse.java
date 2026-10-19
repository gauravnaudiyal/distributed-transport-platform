package com.gtbs.adminauthority.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;

/** Proxied from Booking Service proof / verification endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingVerificationResponse(
        String bookingId,
        String status,
        long mapVersion,
        Instant windowStart,
        Instant windowEnd,
        List<String> segmentIds,
        String proofReference) {}
