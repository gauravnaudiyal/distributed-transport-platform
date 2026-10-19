package com.gtbs.notificationservice.user.service;

import com.gtbs.notificationservice.common.exception.ResourceNotFoundException;
import com.gtbs.notificationservice.user.dto.CreateUserRequest;
import com.gtbs.notificationservice.user.dto.UserProfileResponse;
import com.gtbs.notificationservice.user.entity.UserProfile;
import com.gtbs.notificationservice.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileResponse createUser(CreateUserRequest request) {
        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());

        return toResponse(userProfileRepository.save(profile));
    }

    public UserProfileResponse getUserByUserId(String userId) {
        return toResponse(userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getEmail());
    }
}