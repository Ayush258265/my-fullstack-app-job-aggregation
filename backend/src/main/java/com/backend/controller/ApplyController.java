package com.backend.controller;

import com.backend.dto.ApiResponse;
import com.backend.dto.ApplyRequest;
import com.backend.entity.ApplyClick;
import com.backend.entity.User;
import com.backend.service.ApplyService;
import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;
    private final UserService userService;  // ✅ ADD THIS

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplyClick>> trackApply(@RequestBody ApplyRequest request) {
        try {
            User user = getCurrentUser();  // ✅ Now returns User entity
            ApplyClick applyClick = applyService.trackApply(user, request.getJobId(), request.getApplied());
            String message = request.getApplied() ? "Application tracked as APPLIED" : "Application tracked as NOT APPLIED";
            return ResponseEntity.ok(ApiResponse.success(message, applyClick));
        } catch (Exception e) {
            log.error("Error tracking apply: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/applied")
    public ResponseEntity<ApiResponse<Page<ApplyClick>>> getAppliedJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        User user = getCurrentUser();
        Page<ApplyClick> applied = applyService.getAppliedJobs(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Applied jobs retrieved", applied));
    }

    @GetMapping("/not-applied")
    public ResponseEntity<ApiResponse<Page<ApplyClick>>> getNotAppliedJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        User user = getCurrentUser();
        Page<ApplyClick> notApplied = applyService.getNotAppliedJobs(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Not applied jobs retrieved", notApplied));
    }

    @GetMapping("/applied/count")
    public ResponseEntity<ApiResponse<Long>> getAppliedCount() {
        User user = getCurrentUser();
        long count = applyService.countAppliedJobs(user);
        return ResponseEntity.ok(ApiResponse.success("Applied jobs count", count));
    }

    @GetMapping("/tracked/{jobId}")
    public ResponseEntity<ApiResponse<Boolean>> isJobTracked(@PathVariable Long jobId) {
        User user = getCurrentUser();
        boolean tracked = applyService.hasTrackedJob(user, jobId);
        return ResponseEntity.ok(ApiResponse.success("Tracked status", tracked));
    }

    // ✅ FIXED: Get current user from database
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();  // Get email from authentication
        return userService.getUserByEmail(email);  // ✅ Get User entity from database
    }
}