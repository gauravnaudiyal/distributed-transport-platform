package com.gtbs.observability.audit.api;

import com.gtbs.observability.audit.domain.AuditRecord;
import java.time.Instant;
import java.util.UUID;

public record AuditRecordResponse(
    UUID id,
    String eventType,
    UUID bookingId,
    String segmentId,
    String regionId,
    String sourceService,
    String correlationId,
    String mapVersion,
    Instant eventTimestamp,
    Instant createdAt,
    String payloadJson
) {

    public static AuditRecordResponse from(AuditRecord auditRecord) {
        return new AuditRecordResponse(
            auditRecord.getId(),
            auditRecord.getEventType().name(),
            auditRecord.getBookingId(),
            auditRecord.getSegmentId(),
            auditRecord.getRegionId(),
            auditRecord.getSourceService(),
            auditRecord.getCorrelationId(),
            auditRecord.getMapVersion(),
            auditRecord.getEventTimestamp(),
            auditRecord.getCreatedAt(),
            auditRecord.getPayloadJson()
        );
    }
}
