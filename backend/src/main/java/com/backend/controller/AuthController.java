package com.backend.controller;

import com.backend.dto.ApiResponse;
import com.backend.dto.AuthRequest;
import com.backend.dto.AuthResponse;
import com.backend.dto.RegisterRequest;
import com.backend.entity.User;
import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            log.info("Register request for email: {}", request.getEmail());

            User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPrimarySkills(),
                request.getYearsOfExperience(),
                request.getPreferredLocation()
            );

            String token = userService.generateToken(user);

            AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(user)
                .message("Registration successful")
                .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));

        } catch (RuntimeException e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        try {
            log.info("Login request for email: {}", request.getEmail());

            AuthResponse response = userService.loginUser(request.getEmail(), request.getPassword());

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (RuntimeException e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}