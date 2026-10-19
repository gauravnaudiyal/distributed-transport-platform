package com.gtbs.notificationservice.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookingEvent(
                @JsonProperty("booking_id") String bookingId,
                @JsonProperty("user_id") String userId,
                @JsonProperty("state") String status,
                @JsonProperty("origin") String origin,
                @JsonProperty("destination") String destination) {
}
