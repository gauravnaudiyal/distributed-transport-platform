package com.gtbs.adminauthority.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Proxied from Route Management forecast read model. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SegmentForecastResponse(
        String segmentId,
        long mapVersion,
        int baselineCapacity,
        int provisionalHolds,
        int confirmedBookings,
        double utilizationRatio) {}
