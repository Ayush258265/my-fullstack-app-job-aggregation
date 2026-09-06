package com.backend.repo;

import com.backend.entity.ApplyClick;
import com.backend.entity.User;
import com.backend.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplyClickRepository extends JpaRepository<ApplyClick, Long> {

    // ✅ UPDATED: Added isActive = true
    Page<ApplyClick> findByUserAndAppliedAndIsActiveTrue(User user, Boolean applied, Pageable pageable);

    // ✅ UPDATED: Added isActive = true
    Optional<ApplyClick> findByUserAndJobAndIsActiveTrue(User user, Job job);

    @Query("SELECT a FROM ApplyClick a WHERE a.expiresAt < :now AND a.isActive = true")
    List<ApplyClick> findExpiredActiveEntries(@Param("now") LocalDateTime now);

    // ✅ UPDATED: Added isActive = true
    long countByUserAndAppliedAndIsActiveTrue(User user, Boolean applied);
}