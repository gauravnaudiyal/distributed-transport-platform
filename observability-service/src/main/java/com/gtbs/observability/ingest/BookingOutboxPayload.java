package com.gtbs.observability.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * JSON shape produced by booking-service {@code OutboxPublisher} (snake_case keys).
 */
public record BookingOutboxPayload(
    @JsonProperty("booking_id") UUID bookingId,
    @JsonProperty("user_id") String userId,
    @JsonProperty("state") String state,
    @JsonProperty("origin") String origin,
    @JsonProperty("destination") String destination,
    @JsonProperty("map_version") String mapVersion,
    @JsonProperty("time_window_start") String timeWindowStart,
    @JsonProperty("time_window_end") String timeWindowEnd
) {
}
