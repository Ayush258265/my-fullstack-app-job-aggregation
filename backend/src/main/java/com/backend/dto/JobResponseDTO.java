package com.backend.dto;

import com.backend.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDTO {
    private Long id;
    private String title;
    private String companyName;
    private String location;
    private String experienceRequired;
    private String skills;
    private String description;
    private String source;
    private String applyUrl;
    private LocalDate postedDate;
    private MatchResult match; // ✅ ADDED: Match information (null if user not logged in)

    /**
     * Convert Job entity to JobResponseDTO
     */
    public static JobResponseDTO fromEntity(Job job, MatchResult match) {
        return fromEntity(job, match, job.getSkills());
    }

    public static JobResponseDTO fromEntity(Job job, MatchResult match, String skills) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompanyName())
                .location(job.getLocation())
                .experienceRequired(job.getExperienceRequired())
                .skills(skills)
                .description(job.getDescription())
                .source(job.getSource())
                .applyUrl(job.getApplyUrl())
                .postedDate(job.getPostedDate())
                .match(match) // ✅ ADDED
                .build();
    }
}