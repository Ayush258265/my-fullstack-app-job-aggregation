package com.backend.controller;

import com.backend.dto.*;
import com.backend.entity.User;
import com.backend.service.InterviewService;
import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final UserService userService;

    /**
     * Start new interview
     * POST /api/interview/start
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<QuestionResponse>> startInterview(@RequestBody InterviewSetupRequest request) {
        try {
            User user = getCurrentUser();

            // Check remaining interviews
            int remaining = interviewService.getRemainingInterviewsForUser(user.getId());
            if (remaining == 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Daily limit reached! You can take maximum 6 interviews per day."));
            }

            QuestionResponse response = interviewService.startInterview(user.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(
                    "Interview started successfully. Remaining interviews today: " + (remaining - 1), response));
        } catch (RuntimeException e) {
            log.error("Error starting interview: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get remaining interviews for today
     * GET /api/interview/remaining
     */
    @GetMapping("/remaining")
    public ResponseEntity<ApiResponse<Integer>> getRemainingInterviews() {
        try {
            User user = getCurrentUser();
            int remaining = interviewService.getRemainingInterviewsForUser(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Remaining interviews today", remaining));
        } catch (RuntimeException e) {
            log.error("Error getting remaining interviews: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get next question
     * GET /api/interview/question/{sessionId}
     */
    @GetMapping("/question/{sessionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getNextQuestion(@PathVariable Long sessionId) {
        try {
            QuestionResponse response = interviewService.getNextQuestion(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Next question", response));
        } catch (RuntimeException e) {
            log.error("Error getting next question: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Submit answer
     * POST /api/interview/answer
     */
    @PostMapping("/answer")
    public ResponseEntity<ApiResponse<AnswerResponse>> submitAnswer(
            @RequestParam Long sessionId,
            @RequestParam Integer questionNumber,
            @RequestBody String answer) {
        try {
            interviewService.updateLastActivity(sessionId);
            AnswerResponse response = interviewService.submitAnswer(sessionId, questionNumber, answer);
            return ResponseEntity.ok(ApiResponse.success("Answer submitted successfully", response));
        } catch (RuntimeException e) {
            log.error("Error submitting answer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if user is active (silence detection)
     * GET /api/interview/active/{sessionId}
     */
    @GetMapping("/active/{sessionId}")
    public ResponseEntity<ApiResponse<Boolean>> isUserActive(@PathVariable Long sessionId) {
        try {
            boolean active = interviewService.isUserActive(sessionId);
            return ResponseEntity.ok(ApiResponse.success("User activity status", active));
        } catch (RuntimeException e) {
            log.error("Error checking activity: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * End session
     * POST /api/interview/end/{sessionId}
     */
    @PostMapping("/end/{sessionId}")
    public ResponseEntity<ApiResponse<SessionSummary>> endSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false, defaultValue = "user_requested") String reason) {
        try {
            SessionSummary summary = interviewService.endSession(sessionId, reason);
            return ResponseEntity.ok(ApiResponse.success("Session ended", summary));
        } catch (RuntimeException e) {
            log.error("Error ending session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get session summary
     * GET /api/interview/summary/{sessionId}
     */
    @GetMapping("/summary/{sessionId}")
    public ResponseEntity<ApiResponse<SessionSummary>> getSummary(@PathVariable Long sessionId) {
        try {
            SessionSummary summary = interviewService.getSessionSummary(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Session summary retrieved", summary));
        } catch (RuntimeException e) {
            log.error("Error getting summary: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get interview history
     * GET /api/interview/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SessionSummary>>> getHistory() {
        try {
            User user = getCurrentUser();
            List<SessionSummary> history = interviewService.getInterviewHistory(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Interview history retrieved", history));
        } catch (RuntimeException e) {
            log.error("Error getting history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== HELPER METHOD ====================

    /**
     * Get current authenticated user from SecurityContext
     * Always fetches fresh user data from database
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User not authenticated");
            throw new RuntimeException("User not authenticated");
        }
        
        // Get email from authentication
        String email = authentication.getName();
        log.debug("Fetching user by email: {}", email);
        
        try {
            return userService.getUserByEmail(email);
        } catch (Exception e) {
            log.error("Error fetching user by email {}: {}", email, e.getMessage());
            throw new RuntimeException("Invalid user details");
        }
    }
}