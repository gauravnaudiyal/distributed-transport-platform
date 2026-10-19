package com.gtbs.notificationservice.notification.dto;

import java.time.LocalDateTime;

public record NotificationLogResponse(
        Long id,
        String userId,
        String bookingId,
        String status,
        String message,
        LocalDateTime sentAt) {
}