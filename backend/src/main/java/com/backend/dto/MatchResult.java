package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    private Integer matchPercentage;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String experienceStatus;
    private List<String> recommendations;
}