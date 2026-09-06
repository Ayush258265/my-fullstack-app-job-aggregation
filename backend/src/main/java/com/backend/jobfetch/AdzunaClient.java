package com.backend.jobfetch;

import com.backend.entity.Job;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdzunaClient {
    
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final DateParserUtil dateParserUtil;
    
    @Value("${adzuna.api.url:https://api.adzuna.com/v1/api/jobs}")
    private String apiUrl;
    
    @Value("${adzuna.app.id:}")
    private String appId;
    
    @Value("${adzuna.api.key:}")
    private String apiKey;
    
    public List<Job> fetchJobs() {
        List<Job> jobs = new ArrayList<>();
        
        try {
            String[] countries = {"us", "gb", "in"};
            
            for (String country : countries) {
                String url = String.format("%s/%s/search/1?app_id=%s&app_key=%s&max_results=50", 
                    apiUrl, country, appId, apiKey);
                
                String response = apiClient.get(url);
                if (response == null) {
                    log.warn("No response from Adzuna API for country: {}", country);
                    continue;
                }
                
                JsonNode root = objectMapper.readTree(response);
                JsonNode results = root.get("results");
                
                if (results != null && results.isArray()) {
                    for (JsonNode jobNode : results) {
                        Job job = parseJob(jobNode);
                        if (job != null) {
                            jobs.add(job);
                        }
                    }
                }
            }
            log.info("Fetched {} jobs from Adzuna", jobs.size());
        } catch (Exception e) {
            log.error("Error fetching jobs from Adzuna: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private Job parseJob(JsonNode node) {
        try {
            String title = getString(node, "title");
            String companyName = getString(node, "company");
            String location = getString(node, "location");
            String description = getString(node, "description");
            
            // Parse date using DateParserUtil
            String dateStr = getString(node, "created");
            LocalDate postedDate = dateParserUtil.parseDate(dateStr);
            
            return Job.builder()
                .title(title)
                .companyName(companyName)
                .location(location)
                .experienceRequired(extractExperience(description))
                .skills(extractSkills(description))
                .description(description)
                .source("Adzuna")
                .sourceJobId(getString(node, "id"))
                .applyUrl(getString(node, "redirect_url"))
                .postedDate(postedDate)
                .build();
        } catch (Exception e) {
            log.error("Error parsing Adzuna job: {}", e.getMessage());
            return null;
        }
    }
    
    private String getString(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() 
            ? node.get(field).asText() : null;
    }
    
    private String extractExperience(String description) {
        if (description == null) return null;
        String[] patterns = {"\\d+\\+?\\s*(?:-\\s*\\d+)?\\s*years?", "\\d+\\s*to\\s*\\d+\\s*years?"};
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(description);
            if (m.find()) {
                return m.group();
            }
        }
        return null;
    }
    
    private String extractSkills(String description) {
        if (description == null) return null;
        return description;
    }
}