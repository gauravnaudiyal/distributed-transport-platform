package com.gtbs.notificationservice.notification.consumer;

import com.gtbs.notificationservice.notification.dto.BookingEvent;
import com.gtbs.notificationservice.notification.service.NotificationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final NotificationLogService notificationLogService;

    public BookingEventConsumer(NotificationLogService notificationLogService) {
        this.notificationLogService = notificationLogService;
    }

    @KafkaListener(topics = "${booking.kafka.topic:booking-events}")
    public void consumeBookingEvent(BookingEvent event) {
        logger.info("Received booking event for userId: {}", event.userId());
        notificationLogService.processBookingEvent(event);
    }
}