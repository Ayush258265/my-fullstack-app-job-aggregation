package com.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(name = "first_name", length = 50)
	private String firstName;

	@Column(name = "last_name", length = 50)
	private String lastName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role = Role.USER;

	@Column(name = "free_interviews_used")
	private Integer freeInterviewsUsed = 0;

	@Column(name = "years_of_experience")
	private Integer yearsOfExperience = 0;

	@Column(name = "primary_skills", columnDefinition = "TEXT")
	private String primarySkills;

	@Column(name = "preferred_location")
	private String preferredLocation;

	@JsonIgnore // ✅ ADD THIS - Prevents infinite recursion
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplyClick> applyClicks;

	@JsonIgnore // ✅ ADD THIS - Prevents infinite recursion
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<InterviewSession> interviewSessions;

	public enum Role {
		USER, PREMIUM, ADMIN
	}

	public boolean hasFreeInterviews() {
		return this.freeInterviewsUsed < 5;
	}

	public int getRemainingFreeInterviews() {
		return Math.max(0, 5 - this.freeInterviewsUsed);
	}

	public boolean isPremium() {
		return this.role == Role.PREMIUM || this.role == Role.ADMIN;
	}

	public boolean isAdmin() {
		return this.role == Role.ADMIN;
	}

	@Column(name = "daily_interviews_count")
	private Integer dailyInterviewsCount = 0;

	@Column(name = "last_interview_date")
	private LocalDate lastInterviewDate;

}