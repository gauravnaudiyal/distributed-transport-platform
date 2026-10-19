package com.gtbs.adminauthority.api.dto;

import com.gtbs.adminauthority.audit.InstructionStatus;
import com.gtbs.adminauthority.audit.InstructionType;
import java.time.Instant;
import java.util.List;

public record AuditEntryResponse(
        String id,
        InstructionType instructionType,
        String correlationId,
        List<String> segmentIds,
        long mapVersion,
        InstructionStatus status,
        Instant createdAt,
        String externalRef) {}
