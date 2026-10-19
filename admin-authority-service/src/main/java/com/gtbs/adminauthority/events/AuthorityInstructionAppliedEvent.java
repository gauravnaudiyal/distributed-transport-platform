package com.gtbs.adminauthority.events;

import com.gtbs.adminauthority.audit.InstructionType;
import java.time.Instant;

public record AuthorityInstructionAppliedEvent(
        String eventType,
        String regionId,
        String instructionType,
        String correlationId,
        Long mapVersion,
        Instant occurredAt) {

    public static AuthorityInstructionAppliedEvent of(
            String regionId, InstructionType type, String correlationId, long mapVersion) {
        return new AuthorityInstructionAppliedEvent(
                "AUTHORITY_INSTRUCTION_APPLIED",
                regionId,
                type.name(),
                correlationId,
                mapVersion,
                Instant.now());
    }
}
