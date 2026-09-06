package com.backend.repo;

import com.backend.entity.InterviewSession;
import com.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

	// ✅ WITH Pageable (for paginated results)
	Page<InterviewSession> findByUserAndCompletedTrueOrderByStartedAtDesc(User user, Pageable pageable);

	// ✅ WITHOUT Pageable (for all results - for history)
	List<InterviewSession> findByUserAndCompletedTrueOrderByStartedAtDesc(User user);

	List<InterviewSession> findByUserAndCompletedFalse(User user);

	long countByUserAndCompletedTrue(User user);

	@Query("SELECT COUNT(s) FROM InterviewSession s WHERE s.user = :user AND s.startedAt >= :startOfDay")
	long countTodaySessions(@Param("user") User user, @Param("startOfDay") LocalDate startOfDay);
}