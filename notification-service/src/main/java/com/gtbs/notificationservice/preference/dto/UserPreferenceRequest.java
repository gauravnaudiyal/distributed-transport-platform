package com.gtbs.notificationservice.preference.dto;

public class UserPreferenceRequest {

    private boolean emailEnabled;

    public UserPreferenceRequest() {
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
}