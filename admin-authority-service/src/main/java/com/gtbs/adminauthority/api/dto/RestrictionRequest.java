package com.gtbs.adminauthority.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record RestrictionRequest(
        @NotBlank String correlationId,
        @NotBlank String jurisdictionId,
        @NotEmpty List<@NotBlank String> segmentIds,
        @NotNull Instant effectiveFrom,
        Instant effectiveUntil,
        @NotBlank String restrictionType,
        String parametersJson,
        String reason) {}
