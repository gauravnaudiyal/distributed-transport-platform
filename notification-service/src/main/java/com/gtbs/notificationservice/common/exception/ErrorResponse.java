package com.gtbs.notificationservice.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now().toString(), status, error, message);
    }
}