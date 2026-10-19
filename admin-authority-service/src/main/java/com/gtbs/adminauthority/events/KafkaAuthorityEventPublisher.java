package com.gtbs.adminauthority.events;

import com.gtbs.adminauthority.audit.InstructionType;
import com.gtbs.adminauthority.config.GtbsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.publish-enabled", havingValue = "true", matchIfMissing = true)
public class KafkaAuthorityEventPublisher implements AuthorityEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaAuthorityEventPublisher.class);

    private final KafkaTemplate<String, AuthorityInstructionAppliedEvent> kafkaTemplate;
    private final GtbsProperties gtbsProperties;

    public KafkaAuthorityEventPublisher(
            KafkaTemplate<String, AuthorityInstructionAppliedEvent> kafkaTemplate, GtbsProperties gtbsProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.gtbsProperties = gtbsProperties;
    }

    @Override
    public void publishedApplied(InstructionType instructionType, String correlationId, long mapVersion) {
        AuthorityInstructionAppliedEvent event =
                AuthorityInstructionAppliedEvent.of(gtbsProperties.regionId(), instructionType, correlationId, mapVersion);
        kafkaTemplate
                .send(gtbsProperties.kafka().authorityTopic(), correlationId, event)
                .whenComplete((r, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish authority instruction event for {}", correlationId, ex);
                    }
                });
    }
}
