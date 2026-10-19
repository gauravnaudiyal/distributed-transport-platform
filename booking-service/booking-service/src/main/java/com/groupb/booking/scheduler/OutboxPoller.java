package com.groupb.booking.scheduler;

import com.groupb.booking.model.entity.OutboxEvent;
import com.groupb.booking.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${booking.kafka.topic:booking-events}")
    private String topic;

    public OutboxPoller(OutboxEventRepository outboxEventRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Polls the outbox_events table for unpublished events and sends them to Kafka.
     * Runs at the configured interval (default: every 1 second).
     */
    @Scheduled(fixedDelayString = "${booking.outbox.poll-interval-ms:1000}")
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> pending = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(topic, event.getBookingId(), event.getPayload());
                event.setPublished(true);
                outboxEventRepository.save(event);
                log.debug("Published outbox event {} ({}) to Kafka topic {}",
                        event.getEventId(), event.getEventType(), topic);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {} to Kafka. Will retry on next poll.",
                        event.getEventId(), e);
                break;
            }
        }
    }
}
