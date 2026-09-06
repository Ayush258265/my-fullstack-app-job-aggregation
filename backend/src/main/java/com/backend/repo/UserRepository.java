package com.backend.repo;

import com.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;     // ✅ ADDED
import java.util.Optional;

import javax.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(User.Role role);

    // ===== ✅ ADDED: Profile-related queries =====
    List<User> findByPrimarySkillsContainingIgnoreCase(String skill);

    List<User> findByPreferredLocationIgnoreCase(String location);

    List<User> findByYearsOfExperienceGreaterThanEqual(Integer years);
    // ===== ✅ END ADDED =====
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.dailyInterviewsCount = 0 WHERE u.lastInterviewDate != :today OR u.lastInterviewDate IS NULL")
    void resetDailyInterviewCounts(@Param("today") LocalDate today);
    
    
}