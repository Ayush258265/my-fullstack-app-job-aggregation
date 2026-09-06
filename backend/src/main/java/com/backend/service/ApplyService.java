package com.backend.service;

import com.backend.entity.ApplyClick;
import com.backend.entity.Job;
import com.backend.entity.User;
import com.backend.repo.ApplyClickRepository;
import com.backend.repo.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyClickRepository applyClickRepository;
    private final JobRepository jobRepository;

    @Transactional
    public ApplyClick trackApply(User user, Long jobId, Boolean applied) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Check if already tracked
        ApplyClick existing = applyClickRepository.findByUserAndJobAndIsActiveTrue(user, job)
                .orElse(null);

        if (existing != null) {
            existing.setApplied(applied);
            existing.setClickedAt(LocalDateTime.now());
            existing.setExpiresAt(LocalDateTime.now().plusDays(30));
            return applyClickRepository.save(existing);
        }

        ApplyClick applyClick = ApplyClick.builder()
                .user(user)
                .job(job)
                .applied(applied)
                .clickedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        log.info("Tracked apply: user={}, job={}, applied={}", user.getEmail(), jobId, applied);
        return applyClickRepository.save(applyClick);
    }

    public Page<ApplyClick> getAppliedJobs(User user, Pageable pageable) {
        return applyClickRepository.findByUserAndAppliedAndIsActiveTrue(user, true, pageable);
    }

    public Page<ApplyClick> getNotAppliedJobs(User user, Pageable pageable) {
        return applyClickRepository.findByUserAndAppliedAndIsActiveTrue(user, false, pageable);
    }

    public long countAppliedJobs(User user) {
        return applyClickRepository.countByUserAndAppliedAndIsActiveTrue(user, true);
    }

    public boolean hasTrackedJob(User user, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        return applyClickRepository.findByUserAndJobAndIsActiveTrue(user, job).isPresent();
    }

    // ✅ ADD THIS METHOD
    @Transactional
    public int cleanupExpiredEntries() {
        List<ApplyClick> expired = applyClickRepository.findExpiredActiveEntries(LocalDateTime.now());
        int count = expired.size();

        expired.forEach(click -> click.setIsActive(false));
        applyClickRepository.saveAll(expired);

        log.info("Cleaned up {} expired apply entries", count);
        return count;
    }
}