package com.global_traffic_bs.journey_compatibility_svc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityResponse {

    private String bookingId;
    private CompatibilityStatus status;
    private String message;
    private long currentLoad;
    private long maxCapacity;
    private boolean fallbackUsed;

    public enum CompatibilityStatus {
        APPROVED, REJECTED, ERROR
    }
}
