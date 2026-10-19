package com.groupb.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupb.booking.model.entity.Booking;
import com.groupb.booking.model.entity.OutboxEvent;
import com.groupb.booking.model.enums.BookingEventType;
import com.groupb.booking.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Writes an event to the outbox table. This MUST be called within the same
     * @Transactional boundary as the booking state change to guarantee atomicity.
     */
    public void publish(Booking booking, BookingEventType eventType) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "booking_id", booking.getBookingId(),
                    "user_id", booking.getUserId(),
                    "state", booking.getState().name(),
                    "origin", booking.getOrigin(),
                    "destination", booking.getDestination(),
                    "map_version", booking.getMapVersion() != null ? booking.getMapVersion() : "",
                    "time_window_start", booking.getTimeWindowStart().toString(),
                    "time_window_end", booking.getTimeWindowEnd().toString()
            ));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload for booking {}", booking.getBookingId(), e);
            payload = "{}";
        }

        OutboxEvent event = new OutboxEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setBookingId(booking.getBookingId());
        event.setUserId(booking.getUserId());
        event.setMapVersion(booking.getMapVersion());
        event.setPayload(payload);
        event.setPublished(false);

        outboxEventRepository.save(event);
        log.info("Outbox event {} written for booking {}", eventType, booking.getBookingId());
    }
}
