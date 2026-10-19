package com.gtbs.notificationservice.user.dto;

public record UserProfileResponse(
        Long id,
        String userId,
        String fullName,
        String email) {
}