package com.backend.controller;

import com.backend.dto.ApiResponse;
import com.backend.dto.FilterOptionsDTO;
import com.backend.dto.JobResponseDTO;
import com.backend.dto.MatchResult;
import com.backend.entity.Job;
import com.backend.entity.User;
import com.backend.jobfetch.JobFetchService;
import com.backend.jobfetch.JobParsingUtil;
import com.backend.repo.JobRepository;
import com.backend.service.JobService;
import com.backend.service.MatchService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class JobController {

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final JobFetchService jobFetchService;
    private final MatchService matchService; // ✅ ADDED
    private final JobParsingUtil jobParsingUtil;

    @Autowired
    public JobController(JobRepository jobRepository, JobService jobService, MatchService matchService,
            JobFetchService jobFetchService, JobParsingUtil jobParsingUtil) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.matchService = matchService;
        this.jobFetchService = jobFetchService; // ✅ Add this
        this.jobParsingUtil = jobParsingUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponseDTO>>> searchJobs(@RequestParam(required = false) String title,
            @RequestParam(required = false) String location, @RequestParam(required = false) String company,
            @RequestParam(required = false) String experience, @RequestParam(required = false) String skills,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @PageableDefault(size = 20, sort = "postedDate") Pageable pageable) {

        // ✅ ADD LOGGING
        log.info("Searching jobs with filters - title: {}, location: {}, experience: {}, skills: {}", title, location,
                experience, skills);

        Page<Job> jobs = jobRepository.searchJobs(title, location, company, experience, skills, fromDate, pageable);

        log.info("Found {} jobs", jobs.getTotalElements());

        User currentUser = getCurrentUser();

        Page<JobResponseDTO> response = jobs.map(job -> {
            MatchResult match = null;
            if (currentUser != null) {
                match = matchService.calculateMatch(currentUser, job);
            }
            return JobResponseDTO.fromEntity(job, match, getDisplaySkills(job));
        });

        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDTO>> getJobById(@PathVariable Long id) { // ✅ CHANGED: Return type
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        // ✅ ADDED: Get current user (if authenticated)
        User currentUser = getCurrentUser();

        // ✅ ADDED: Calculate match
        MatchResult match = null;
        if (currentUser != null) {
            match = matchService.calculateMatch(currentUser, job);
        }

        JobResponseDTO response = JobResponseDTO.fromEntity(job, match, getDisplaySkills(job));
        return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", response));
    }

    private String getDisplaySkills(Job job) {
        String skills = job.getSkills();
        if (skills == null || skills.isBlank() || skills.contains("<") || skills.equals(job.getDescription())) {
            return jobParsingUtil.extractSkills(job.getDescription());
        }
        return skills;
    }

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<FilterOptionsDTO>> getFilterOptions() {
        FilterOptionsDTO options = jobService.getFilterOptions();
        return ResponseEntity.ok(ApiResponse.success("Filter options retrieved", options));
    }

    // ✅ ADDED: Helper method to get current user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        try {
            return (User) authentication.getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/fetch-jobs")
    public ResponseEntity<ApiResponse<String>> fetchJobsManually() {
        try {
            jobFetchService.fetchAllJobs();
            return ResponseEntity.ok(ApiResponse.success("Jobs fetched successfully!", "Check console for details"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to fetch jobs: " + e.getMessage()));
        }
    }

}