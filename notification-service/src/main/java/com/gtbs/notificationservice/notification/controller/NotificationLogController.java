package com.gtbs.notificationservice.notification.controller;

import com.gtbs.notificationservice.notification.dto.NotificationLogResponse;
import com.gtbs.notificationservice.notification.service.NotificationLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationLogController {

    private final NotificationLogService notificationLogService;

    public NotificationLogController(NotificationLogService notificationLogService) {
        this.notificationLogService = notificationLogService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationLogResponse>> getNotifications(@PathVariable String userId) {
        List<NotificationLogResponse> notifications = notificationLogService.getNotificationsForUser(userId);
        return notifications.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(notifications);
    }
}