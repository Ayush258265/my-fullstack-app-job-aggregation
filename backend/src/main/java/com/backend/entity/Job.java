package com.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ✅ ADD THIS IMPORT
import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(length = 255)
    private String location;

    @Column(name = "experience_required", length = 100)
    private String experienceRequired;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String source;

    @Column(name = "source_job_id", length = 255)
    private String sourceJobId;

    @Column(name = "apply_url", length = 500)
    private String applyUrl;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "dedupe_hash", unique = true, nullable = false, length = 64)
    private String dedupeHash;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore  // ✅ ADD THIS if this field exists
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApplyClick> applyClicks;
}