package com.gtbs.observability.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_records")
public class AuditRecord {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AuditEventType eventType;

    @Column
    private UUID bookingId;

    @Column(length = 128)
    private String segmentId;

    @Column(nullable = false, length = 64)
    private String regionId;

    @Column(nullable = false, length = 128)
    private String sourceService;

    @Column(length = 128)
    private String correlationId;

    @Column(length = 64)
    private String mapVersion;

    @Column(nullable = false)
    private Instant eventTimestamp;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, columnDefinition = "text")
    private String payloadJson;

    protected AuditRecord() {
    }

    private AuditRecord(
        UUID id,
        AuditEventType eventType,
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
        this.id = id;
        this.eventType = eventType;
        this.bookingId = bookingId;
        this.segmentId = segmentId;
        this.regionId = regionId;
        this.sourceService = sourceService;
        this.correlationId = correlationId;
        this.mapVersion = mapVersion;
        this.eventTimestamp = eventTimestamp;
        this.createdAt = createdAt;
        this.payloadJson = payloadJson;
    }

    public static AuditRecord create(
        AuditEventType eventType,
        UUID bookingId,
        String segmentId,
        String regionId,
        String sourceService,
        String correlationId,
        String mapVersion,
        Instant eventTimestamp,
        String payloadJson
    ) {
        Instant now = Instant.now();
        return new AuditRecord(
            UUID.randomUUID(),
            eventType,
            bookingId,
            segmentId,
            regionId,
            sourceService,
            correlationId,
            mapVersion,
            eventTimestamp,
            now,
            payloadJson
        );
    }

    public UUID getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getMapVersion() {
        return mapVersion;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }
}
