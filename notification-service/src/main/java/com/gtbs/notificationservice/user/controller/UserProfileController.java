package com.gtbs.notificationservice.user.controller;

import com.gtbs.notificationservice.user.dto.CreateUserRequest;
import com.gtbs.notificationservice.user.dto.UserProfileResponse;
import com.gtbs.notificationservice.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.createUser(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(userProfileService.getUserByUserId(userId));
    }
}