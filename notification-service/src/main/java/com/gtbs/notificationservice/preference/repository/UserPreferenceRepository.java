package com.gtbs.notificationservice.preference.repository;

import com.gtbs.notificationservice.preference.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserId(String userId);
}