package com.gtbs.notificationservice.preference.controller;

import com.gtbs.notificationservice.preference.dto.UserPreferenceRequest;
import com.gtbs.notificationservice.preference.dto.UserPreferenceResponse;
import com.gtbs.notificationservice.preference.service.UserPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @PutMapping
    public ResponseEntity<UserPreferenceResponse> saveOrUpdate(
            @PathVariable String userId,
            @RequestBody UserPreferenceRequest request) {
        return ResponseEntity.ok(userPreferenceService.saveOrUpdatePreference(userId, request));
    }

    @GetMapping
    public ResponseEntity<UserPreferenceResponse> get(@PathVariable String userId) {
        return ResponseEntity.ok(userPreferenceService.getPreference(userId));
    }
}