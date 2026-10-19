package com.gtbs.observability.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtbs.observability.audit.domain.AuditEventType;
import com.gtbs.observability.audit.domain.AuditRecord;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookingOutboxMapper {

    private static final Logger log = LoggerFactory.getLogger(BookingOutboxMapper.class);

    private final ObjectMapper objectMapper;
    private final String defaultRegionId;
    private final String defaultSourceService;

    public BookingOutboxMapper(
        ObjectMapper objectMapper,
        @Value("${app.audit.default-region-id}") String defaultRegionId,
        @Value("${app.audit.default-booking-source-service}") String defaultSourceService
    ) {
        this.objectMapper = objectMapper;
        this.defaultRegionId = defaultRegionId;
        this.defaultSourceService = defaultSourceService;
    }

    /**
     * Maps booking outbox JSON to an audit row. Returns empty when JSON is invalid or booking {@code state}
     * does not match a known {@link AuditEventType}.
     */
    public Optional<AuditRecord> mapBookingOutboxJson(String rawJson) {
        BookingOutboxPayload payload;
        try {
            payload = objectMapper.readValue(rawJson, BookingOutboxPayload.class);
        } catch (JsonProcessingException e) {
            log.warn("Skipping booking outbox message: invalid JSON ({})", e.getMessage());
            return Optional.empty();
        }
        if (payload.bookingId() == null) {
            log.warn("Skipping booking outbox message: missing booking_id");
            return Optional.empty();
        }
        if (payload.state() == null || payload.state().isBlank()) {
            log.warn("Skipping booking outbox message for booking {}: missing state", payload.bookingId());
            return Optional.empty();
        }
        final AuditEventType eventType;
        try {
            eventType = AuditEventType.valueOf(payload.state());
        } catch (IllegalArgumentException e) {
            log.warn(
                "Skipping booking outbox message for booking {}: unknown state '{}'",
                payload.bookingId(),
                payload.state()
            );
            return Optional.empty();
        }
        Instant eventTimestamp = Instant.now();
        String mapVersion = payload.mapVersion() != null && !payload.mapVersion().isEmpty() ? payload.mapVersion() : null;
        return Optional.of(
            AuditRecord.create(
                eventType,
                payload.bookingId(),
                null,
                defaultRegionId,
                defaultSourceService,
                null,
                mapVersion,
                eventTimestamp,
                rawJson
            )
        );
    }
}
