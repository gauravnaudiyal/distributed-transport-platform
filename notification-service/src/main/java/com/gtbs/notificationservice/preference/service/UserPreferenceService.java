package com.gtbs.notificationservice.preference.service;

import com.gtbs.notificationservice.common.exception.ResourceNotFoundException;
import com.gtbs.notificationservice.preference.dto.UserPreferenceRequest;
import com.gtbs.notificationservice.preference.dto.UserPreferenceResponse;
import com.gtbs.notificationservice.preference.entity.UserPreference;
import com.gtbs.notificationservice.preference.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public UserPreferenceResponse saveOrUpdatePreference(String userId, UserPreferenceRequest request) {
        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> new UserPreference(userId, request.isEmailEnabled()));

        preference.setEmailEnabled(request.isEmailEnabled());
        userPreferenceRepository.save(preference);

        return new UserPreferenceResponse(preference.getUserId(), preference.isEmailEnabled());
    }

    public UserPreferenceResponse getPreference(String userId) {
        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found for user: " + userId));

        return new UserPreferenceResponse(preference.getUserId(), preference.isEmailEnabled());
    }
}