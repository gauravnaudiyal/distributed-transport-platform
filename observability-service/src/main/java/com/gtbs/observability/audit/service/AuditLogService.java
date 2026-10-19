package com.gtbs.observability.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtbs.observability.audit.api.AuditRecordResponse;
import com.gtbs.observability.audit.domain.AuditEventType;
import com.gtbs.observability.audit.domain.AuditRecord;
import com.gtbs.observability.audit.repository.AuditRecordRepository;
import com.gtbs.observability.ingest.BookingOutboxMapper;
import com.gtbs.observability.ingest.PolicyChangeEvent;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;
    private final BookingOutboxMapper bookingOutboxMapper;

    public AuditLogService(
        AuditRecordRepository auditRecordRepository,
        ObjectMapper objectMapper,
        BookingOutboxMapper bookingOutboxMapper
    ) {
        this.auditRecordRepository = auditRecordRepository;
        this.objectMapper = objectMapper;
        this.bookingOutboxMapper = bookingOutboxMapper;
    }

    @Transactional
    public void recordBookingOutboxJson(String rawJson) {
        bookingOutboxMapper.mapBookingOutboxJson(rawJson).ifPresent(auditRecordRepository::save);
    }

    @Transactional
    public void recordPolicyEvent(PolicyChangeEvent event) {
        if (event.regionId() == null || event.regionId().isBlank()) {
            log.warn("Skipping policy event: missing regionId (eventType={})", event.eventType());
            return;
        }
        if (event.sourceService() == null || event.sourceService().isBlank()) {
            log.warn("Skipping policy event: missing sourceService (eventType={})", event.eventType());
            return;
        }
        AuditEventType eventType;
        try {
            eventType = AuditEventType.valueOf(event.eventType());
        } catch (IllegalArgumentException e) {
            log.warn(
                "Skipping policy event: unknown eventType '{}' (segmentId={})",
                event.eventType(),
                event.segmentId()
            );
            return;
        }
        auditRecordRepository.save(
            AuditRecord.create(
                eventType,
                null,
                event.segmentId(),
                event.regionId(),
                event.sourceService(),
                event.correlationId(),
                event.mapVersion(),
                event.eventTimestamp(),
                toJson(event)
            )
        );
    }

    @Transactional(readOnly = true)
    public List<AuditRecordResponse> findByBookingId(UUID bookingId) {
        return auditRecordRepository.findByBookingIdOrderByEventTimestampAsc(bookingId)
            .stream()
            .map(AuditRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditRecordResponse> findBySegmentId(String segmentId) {
        return auditRecordRepository.findBySegmentIdOrderByEventTimestampAsc(segmentId)
            .stream()
            .map(AuditRecordResponse::from)
            .toList();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize audit payload", exception);
        }
    }
}
