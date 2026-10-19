package com.gtbs.observability.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtbs.observability.audit.service.AuditLogService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventConsumers {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditEventConsumers(AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.booking-lifecycle}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBookingLifecycle(String payload) {
        auditLogService.recordBookingOutboxJson(payload);
    }

    @KafkaListener(topics = "${app.kafka.topics.route-policy-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePolicyChange(String payload) {
        auditLogService.recordPolicyEvent(readValue(payload, PolicyChangeEvent.class));
    }

    private <T> T readValue(String payload, Class<T> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not deserialize Kafka payload", exception);
        }
    }
}
