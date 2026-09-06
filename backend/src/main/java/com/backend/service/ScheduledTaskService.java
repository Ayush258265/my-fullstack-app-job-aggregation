package com.backend.service;

import com.backend.jobfetch.JobFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final JobFetchService jobFetchService;
    private final ApplyService applyService;  // ✅ ADDED

    // Runs every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void fetchJobs() {
        log.info("Starting scheduled job fetch...");
        try {
            jobFetchService.fetchAllJobs();
            log.info("Scheduled job fetch completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled job fetch: {}", e.getMessage());
        }
    }

    // ✅ ADDED: Cleanup expired apply entries (runs daily at 1 AM)
    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupExpiredEntries() {
        log.info("Starting cleanup of expired apply entries...");
        try {
            int count = applyService.cleanupExpiredEntries();
            log.info("Cleaned up {} expired apply entries", count);
        } catch (Exception e) {
            log.error("Error in cleanup: {}", e.getMessage());
        }
    }
}