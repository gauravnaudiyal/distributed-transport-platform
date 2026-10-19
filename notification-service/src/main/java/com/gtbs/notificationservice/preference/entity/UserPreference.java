package com.gtbs.notificationservice.preference.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private boolean emailEnabled = true;

    public UserPreference() {
    }

    public UserPreference(String userId, boolean emailEnabled) {
        this.userId = userId;
        this.emailEnabled = emailEnabled;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
}