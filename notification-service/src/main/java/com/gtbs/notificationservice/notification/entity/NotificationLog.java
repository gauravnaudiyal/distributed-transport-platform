package com.gtbs.notificationservice.notification.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String message;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public NotificationLog() {
    }

    public Long getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}