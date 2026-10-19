package com.gtbs.adminauthority.events;

import com.gtbs.adminauthority.audit.InstructionType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.publish-enabled", havingValue = "false")
public class NoOpAuthorityEventPublisher implements AuthorityEventPublisher {

    @Override
    public void publishedApplied(InstructionType instructionType, String correlationId, long mapVersion) {
        // Kafka fan-out disabled
    }
}
