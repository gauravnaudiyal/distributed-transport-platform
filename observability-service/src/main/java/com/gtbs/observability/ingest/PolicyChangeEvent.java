package com.gtbs.observability.ingest;

import java.time.Instant;

public record PolicyChangeEvent(
    String eventType,
    String segmentId,
    String regionId,
    String sourceService,
    String correlationId,
    String mapVersion,
    Instant eventTimestamp
) {
}
