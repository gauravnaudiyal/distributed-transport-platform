package com.gtbs.notificationservice.preference.dto;

public record UserPreferenceResponse(
        String userId,
        boolean emailEnabled) {
}