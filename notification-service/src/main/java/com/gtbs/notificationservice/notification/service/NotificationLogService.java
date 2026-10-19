package com.gtbs.notificationservice.notification.service;

import com.gtbs.notificationservice.common.exception.ResourceNotFoundException;
import com.gtbs.notificationservice.notification.dto.BookingEvent;
import com.gtbs.notificationservice.notification.dto.NotificationLogResponse;
import com.gtbs.notificationservice.notification.entity.NotificationLog;
import com.gtbs.notificationservice.notification.repository.NotificationLogRepository;
import com.gtbs.notificationservice.user.entity.UserProfile;
import com.gtbs.notificationservice.user.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationLogService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationLogService.class);

    private final NotificationLogRepository notificationLogRepository;
    private final UserProfileRepository userProfileRepository;

    public NotificationLogService(NotificationLogRepository notificationLogRepository,
                                  UserProfileRepository userProfileRepository) {
        this.notificationLogRepository = notificationLogRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public void processBookingEvent(BookingEvent event) {
        UserProfile user = userProfileRepository.findByUserId(event.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + event.userId()));

        logger.info("Sending EMAIL notification to {} for booking {} with status {}",
            user.getEmail(), event.bookingId(), event.status());

        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setUserId(event.userId());
        notificationLog.setBookingId(event.bookingId());
        notificationLog.setStatus(event.status());
        notificationLog.setMessage("Booking " + event.bookingId() + " is " + event.status()
            + " from " + event.origin() + " to " + event.destination());
        notificationLog.setChannel("EMAIL");

        notificationLogRepository.save(notificationLog);

        logger.info("Notification saved for user {}", event.userId());
    }

    public List<NotificationLogResponse> getNotificationsForUser(String userId) {
        return notificationLogRepository.findByUserId(userId)
            .stream()
            .map(entry -> new NotificationLogResponse(
                entry.getId(),
                entry.getUserId(),
                entry.getBookingId(),
                entry.getStatus(),
                entry.getMessage(),
                entry.getSentAt()))
            .toList();
    }
}