package com.backend.controller;

import com.backend.dto.ApiResponse;
import com.backend.dto.UpdateProfileRequest;
import com.backend.entity.User;
import com.backend.service.UserProfileService;
import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;  // ✅ ADD THIS

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody UpdateProfileRequest request) {
        User currentUser = getCurrentUser();
        User updatedUser = userProfileService.updateProfile(
            currentUser.getId(),
            request.getPrimarySkills(),
            request.getYearsOfExperience(),
            request.getPreferredLocation()
        );
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    // ✅ FIXED: Get current user from database
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
}