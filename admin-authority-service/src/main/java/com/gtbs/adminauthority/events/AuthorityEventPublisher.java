package com.gtbs.adminauthority.events;

import com.gtbs.adminauthority.audit.InstructionType;

public interface AuthorityEventPublisher {

    void publishedApplied(InstructionType instructionType, String correlationId, long mapVersion);
}
