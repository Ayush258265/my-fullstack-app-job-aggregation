package com.backend.service;

import com.backend.entity.User;
import com.backend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * Update user profile (skills, experience, location)
     */
    @Transactional
    public User updateProfile(Long userId, String primarySkills, Integer yearsOfExperience, String preferredLocation) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (primarySkills != null) {
            user.setPrimarySkills(primarySkills);
        }
        if (yearsOfExperience != null) {
            user.setYearsOfExperience(yearsOfExperience);
        }
        if (preferredLocation != null) {
            user.setPreferredLocation(preferredLocation);
        }

        return userRepository.save(user);
    }

    /**
     * Get user profile
     */
    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}