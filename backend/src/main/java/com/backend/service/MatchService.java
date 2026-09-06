package com.backend.service;

import com.backend.dto.MatchResult;
import com.backend.entity.Job;
import com.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    /**
     * Calculate match between user and job
     */
    public MatchResult calculateMatch(User user, Job job) {
        if (user == null || job == null) {
            return null;
        }

        // Extract user skills
        List<String> userSkills = extractSkills(user.getPrimarySkills());
        List<String> jobSkills = extractSkills(job.getSkills());

        // Calculate skills match (50% weight)
        SkillResult skillResult = calculateSkillsMatch(userSkills, jobSkills);

        // Calculate experience match (35% weight)
        ExperienceResult expResult = calculateExperienceMatch(
            user.getYearsOfExperience(),
            job.getExperienceRequired()
        );

        // Calculate location bonus (15% weight)
        int locationBonus = calculateLocationBonus(
            user.getPreferredLocation(),
            job.getLocation()
        );

        // Calculate final score
        int finalScore = skillResult.score + expResult.score + locationBonus;

        // Generate recommendations
        List<String> recommendations = generateRecommendations(skillResult.missingSkills, expResult);

        return MatchResult.builder()
            .matchPercentage(Math.min(finalScore, 100))
            .matchingSkills(skillResult.matchingSkills)
            .missingSkills(skillResult.missingSkills)
            .experienceStatus(expResult.status)
            .recommendations(recommendations)
            .build();
    }

    /**
     * Extract skills from comma-separated string
     */
    private List<String> extractSkills(String skillsString) {
        if (skillsString == null || skillsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> skills = new ArrayList<>();
        for (String skill : skillsString.split(",")) {
            String trimmed = skill.trim();
            if (!trimmed.isEmpty()) {
                skills.add(trimmed.toLowerCase());
            }
        }
        return skills;
    }

    /**
     * Calculate skills match (50% weight)
     */
    private SkillResult calculateSkillsMatch(List<String> userSkills, List<String> jobSkills) {
        if (jobSkills.isEmpty()) {
            return new SkillResult(0, new ArrayList<>(), new ArrayList<>());
        }

        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String jobSkill : jobSkills) {
            boolean found = userSkills.stream()
                .anyMatch(userSkill -> userSkill.contains(jobSkill) || jobSkill.contains(userSkill));
            if (found) {
                matchingSkills.add(jobSkill);
            } else {
                missingSkills.add(jobSkill);
            }
        }

        int score = (int) ((double) matchingSkills.size() / jobSkills.size() * 50);

        return new SkillResult(score, matchingSkills, missingSkills);
    }

    /**
     * Calculate experience match (35% weight)
     */
    private ExperienceResult calculateExperienceMatch(Integer userYears, String jobExperienceRequired) {
        if (jobExperienceRequired == null || jobExperienceRequired.trim().isEmpty()) {
            return new ExperienceResult(0, "Not Specified");
        }

        if (userYears == null) {
            userYears = 0;
        }

        String exp = jobExperienceRequired.toLowerCase().trim();
        int minYears = 0;
        int maxYears = 99;

        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\s*-\\s*(\\d+)\\s*(?:years?|yrs?)");
            java.util.regex.Matcher m = p.matcher(exp);
            if (m.find()) {
                minYears = Integer.parseInt(m.group(1));
                maxYears = Integer.parseInt(m.group(2));
            } else {
                p = java.util.regex.Pattern.compile("(\\d+)\\s*\\+?\\s*(?:years?|yrs?)");
                m = p.matcher(exp);
                if (m.find()) {
                    minYears = Integer.parseInt(m.group(1));
                    maxYears = 99;
                }
            }
        } catch (Exception e) {
            return new ExperienceResult(0, "Not Specified");
        }

        int score;
        String status;

        if (userYears >= minYears && userYears <= maxYears) {
            score = 35;
            status = "Good Fit ✅";
        } else if (userYears < minYears) {
            int gap = minYears - userYears;
            score = Math.max(10, 35 - (gap * 10));
            status = "Underqualified ⚠️ (Requires " + minYears + "+ years, you have " + userYears + ")";
        } else if (userYears > maxYears) {
            score = 35;
            status = "Overqualified ℹ️ (Requires up to " + maxYears + " years, you have " + userYears + ")";
        } else {
            score = 0;
            status = "Not Specified";
        }

        return new ExperienceResult(score, status);
    }

    /**
     * Calculate location bonus (15% weight)
     */
    private int calculateLocationBonus(String userPreferredLocation, String jobLocation) {
        if (userPreferredLocation == null || userPreferredLocation.trim().isEmpty()) {
            return 0;
        }
        if (jobLocation == null || jobLocation.trim().isEmpty()) {
            return 0;
        }

        String userLoc = userPreferredLocation.toLowerCase().trim();
        String jobLoc = jobLocation.toLowerCase().trim();

        if (userLoc.equals(jobLoc)) {
            return 15;
        }

        if (jobLoc.contains("remote") || jobLoc.contains("anywhere")) {
            return 10;
        }

        if (jobLoc.contains(userLoc) || userLoc.contains(jobLoc)) {
            return 8;
        }

        return 0;
    }

    /**
     * Generate recommendations based on missing skills and experience
     */
    private List<String> generateRecommendations(List<String> missingSkills, ExperienceResult expResult) {
        List<String> recommendations = new ArrayList<>();

        if (missingSkills != null && !missingSkills.isEmpty()) {
            String skills = String.join(", ", missingSkills.subList(0, Math.min(3, missingSkills.size())));
            recommendations.add("Consider learning: " + skills);
        }

        if (expResult.status != null && expResult.status.contains("Underqualified")) {
            recommendations.add("Gain more experience in this field or look for entry-level roles");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Your profile matches well! Keep exploring.");
        }

        return recommendations;
    }

    // Inner classes for results
    private static class SkillResult {
        int score;
        List<String> matchingSkills;
        List<String> missingSkills;

        SkillResult(int score, List<String> matchingSkills, List<String> missingSkills) {
            this.score = score;
            this.matchingSkills = matchingSkills;
            this.missingSkills = missingSkills;
        }
    }

    private static class ExperienceResult {
        int score;
        String status;

        ExperienceResult(int score, String status) {
            this.score = score;
            this.status = status;
        }
    }
}