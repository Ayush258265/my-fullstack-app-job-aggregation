package com.backend.jobfetch;

import com.backend.entity.Job;
import com.backend.repo.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobProcessor {

	private final JobRepository jobRepository;

	public List<Job> processJobs(List<Job> jobs) {
		List<Job> newJobs = new ArrayList<>();
		int duplicateCount = 0;

		for (Job job : jobs) {
			// Generate dedupe hash (SHA-256)
			String dedupeHash = generateDedupeHash(job);
			job.setDedupeHash(dedupeHash);

			// Check if job already exists
			if (!jobRepository.existsByDedupeHash(dedupeHash)) {
				job.setIsActive(true);
				if (job.getPostedDate() == null) {
					job.setPostedDate(LocalDate.now());
				}
				newJobs.add(job);
			} else {
				duplicateCount++;
			}
		}

		// Save all new jobs
		if (!newJobs.isEmpty()) {
			jobRepository.saveAll(newJobs);
			log.info("Saved {} new jobs, skipped {} duplicates", newJobs.size(), duplicateCount);
		} else {
			log.info("No new jobs to save, skipped {} duplicates", duplicateCount);
		}

		return newJobs;
	}

	private String generateDedupeHash(Job job) {
		try {
			String input = String.format("%s|%s|%s", job.getTitle() != null ? job.getTitle().toLowerCase().trim() : "",
					job.getCompanyName() != null ? job.getCompanyName().toLowerCase().trim() : "",
					job.getLocation() != null ? job.getLocation().toLowerCase().trim() : "");

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes("UTF-8"));

			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			log.error("Error generating hash for job: {}", e.getMessage());
			return String.valueOf(System.currentTimeMillis());
		}
	}
}