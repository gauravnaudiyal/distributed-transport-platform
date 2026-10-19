package com.gtbs.observability.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gtbs.observability.audit.domain.AuditEventType;
import com.gtbs.observability.audit.domain.AuditRecord;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingOutboxMapperTest {

    private static final UUID BOOKING_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private BookingOutboxMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper = new BookingOutboxMapper(objectMapper, "unit-test-region", "booking-service");
    }

    @Test
    void mapsValidBookingOutboxPayload() {
        String json =
            "{\"booking_id\":\"550e8400-e29b-41d4-a716-446655440000\",\"user_id\":\"u1\",\"state\":\"CONFIRMED\","
                + "\"origin\":\"A\",\"destination\":\"B\",\"map_version\":\"mv-1\","
                + "\"time_window_start\":\"2026-01-01T10:00:00Z\",\"time_window_end\":\"2026-01-01T11:00:00Z\"}";

        Optional<AuditRecord> record = mapper.mapBookingOutboxJson(json);

        assertThat(record).isPresent();
        AuditRecord r = record.get();
        assertThat(r.getEventType()).isEqualTo(AuditEventType.CONFIRMED);
        assertThat(r.getBookingId()).isEqualTo(BOOKING_ID);
        assertThat(r.getRegionId()).isEqualTo("unit-test-region");
        assertThat(r.getSourceService()).isEqualTo("booking-service");
        assertThat(r.getMapVersion()).isEqualTo("mv-1");
        assertThat(r.getPayloadJson()).isEqualTo(json);
    }

    @Test
    void skipsUnknownState() {
        String json =
            "{\"booking_id\":\"550e8400-e29b-41d4-a716-446655440000\",\"user_id\":\"u1\",\"state\":\"NOT_A_REAL_STATE\","
                + "\"origin\":\"A\",\"destination\":\"B\",\"map_version\":\"\","
                + "\"time_window_start\":\"2026-01-01T10:00:00Z\",\"time_window_end\":\"2026-01-01T11:00:00Z\"}";

        assertThat(mapper.mapBookingOutboxJson(json)).isEmpty();
    }

    @Test
    void skipsInvalidJson() {
        assertThat(mapper.mapBookingOutboxJson("not-json")).isEmpty();
    }

    @Test
    void mapsEmptyMapVersionToNullOnEntity() {
        String json =
            "{\"booking_id\":\"550e8400-e29b-41d4-a716-446655440000\",\"user_id\":\"u1\",\"state\":\"PENDING\","
                + "\"origin\":\"A\",\"destination\":\"B\",\"map_version\":\"\","
                + "\"time_window_start\":\"2026-01-01T10:00:00Z\",\"time_window_end\":\"2026-01-01T11:00:00Z\"}";

        Optional<AuditRecord> record = mapper.mapBookingOutboxJson(json);
        assertThat(record).isPresent();
        assertThat(record.get().getMapVersion()).isNull();
    }
}
