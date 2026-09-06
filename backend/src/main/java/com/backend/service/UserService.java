package com.backend.service;

import com.backend.config.JwtUtil;
import com.backend.dto.AuthResponse;
import com.backend.entity.User;
import com.backend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AuthenticationManager authenticationManager;

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found with email: " + email));
	}

	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
	}

	/**
	 * Register a new user
	 */
	@Transactional
	public User registerUser(String email, String password, String firstName, String lastName, String primarySkills,
			Integer yearsOfExperience, String preferredLocation) {

		// Check if email already exists
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("Email already registered: " + email);
		}

		// Validate password strength (optional)
		if (password == null || password.length() < 6) {
			throw new RuntimeException("Password must be at least 6 characters");
		}

		// Create new user
		User user = User.builder().email(email).password(passwordEncoder.encode(password)).firstName(firstName)
				.lastName(lastName).role(User.Role.USER).freeInterviewsUsed(0)
				.yearsOfExperience(yearsOfExperience != null ? yearsOfExperience : 0).primarySkills(primarySkills)
				.preferredLocation(preferredLocation).build();

		log.info("Registering new user: {}", email);
		return userRepository.save(user);
	}

	/**
	 * Login user and generate JWT token
	 */
	@Transactional
	public AuthResponse loginUser(String email, String password) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(email, password));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

			String token = jwtUtil.generateToken(email);

			log.info("User logged in: {}", email);

			return AuthResponse.builder().token(token).user(user).message("Login successful").build();

		} catch (Exception e) {
			log.error("Login failed for email: {}", email);
			throw new RuntimeException("Invalid email or password");
		}
	}

	/**
	 * Generate JWT token for user
	 */
	public String generateToken(User user) {
		return jwtUtil.generateToken(user.getEmail());
	}

	// ===== ✅ ADDED: CRUD Operations =====

	/**
	 * Get all users
	 */
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	/**
	 * Create a new user (for admin)
	 */
	@Transactional
	public User createUser(User user) {
		// Check if email already exists
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new RuntimeException("Email already registered: " + user.getEmail());
		}

		// Encode password if provided
		if (user.getPassword() != null && !user.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}

		// Set default role if not set
		if (user.getRole() == null) {
			user.setRole(User.Role.USER);
		}

		log.info("Creating new user: {}", user.getEmail());
		return userRepository.save(user);
	}

	/**
	 * Update user
	 */
	@Transactional
	public User updateUser(Long userId, User userDetails) {
		User existingUser = getUserById(userId);

		// Update fields
		if (userDetails.getFirstName() != null) {
			existingUser.setFirstName(userDetails.getFirstName());
		}
		if (userDetails.getLastName() != null) {
			existingUser.setLastName(userDetails.getLastName());
		}
		if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
			// Check if new email is already taken
			if (userRepository.existsByEmail(userDetails.getEmail())) {
				throw new RuntimeException("Email already taken: " + userDetails.getEmail());
			}
			existingUser.setEmail(userDetails.getEmail());
		}
		if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
			existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
		}
		if (userDetails.getPrimarySkills() != null) {
			existingUser.setPrimarySkills(userDetails.getPrimarySkills());
		}
		if (userDetails.getYearsOfExperience() != null) {
			existingUser.setYearsOfExperience(userDetails.getYearsOfExperience());
		}
		if (userDetails.getPreferredLocation() != null) {
			existingUser.setPreferredLocation(userDetails.getPreferredLocation());
		}
		if (userDetails.getRole() != null) {
			existingUser.setRole(userDetails.getRole());
		}

		log.info("Updating user: {}", existingUser.getEmail());
		return userRepository.save(existingUser);
	}

	/**
	 * Delete user (soft delete or hard delete)
	 */
	@Transactional
	public void deleteUser(Long userId) {
		User user = getUserById(userId);
		userRepository.delete(user);
		log.info("Deleted user: {}", user.getEmail());
	}

	/**
	 * Search users by name
	 */
	public List<User> searchUsersByName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return userRepository.findAll();
		}
		// Simple implementation - you can improve this with custom queries
		return userRepository.findAll().stream().filter(user -> {
			String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
			return fullName.contains(name.toLowerCase());
		}).toList();
	}

	/**
	 * Update user profile (for current user)
	 */
	@Transactional
	public User updateUserProfile(Long userId, String primarySkills, Integer yearsOfExperience,
			String preferredLocation) {
		User user = getUserById(userId);

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
	 * Check if user has free interviews remaining
	 */
	public boolean hasFreeInterviews(User user) {
		return user.getFreeInterviewsUsed() < 5;
	}

	/**
	 * Get remaining free interviews count
	 */
	public int getRemainingFreeInterviews(User user) {
		return Math.max(0, 5 - user.getFreeInterviewsUsed());
	}
}