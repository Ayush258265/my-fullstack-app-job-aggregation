package com.backend.jobfetch;

import com.backend.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemotiveClient {

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final DateParserUtil dateParserUtil;
    private final JobParsingUtil jobParsingUtil;

    @Value("${remotive.api.url:https://remotive.com/api/remote-jobs}")
    private String apiUrl;

    public List<Job> fetchJobs() {
        List<Job> jobs = new ArrayList<>();

        try {
            String response = apiClient.get(apiUrl);
            if (response == null) {
                log.warn("No response from Remotive API");
                return jobs;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode jobsNode = root.get("jobs");

            if (jobsNode != null && jobsNode.isArray()) {
                for (JsonNode jobNode : jobsNode) {
                    Job job = parseJob(jobNode);
                    if (job != null) {
                        jobs.add(job);
                    }
                }
                log.info("Fetched {} jobs from Remotive", jobs.size());
            }
        } catch (Exception e) {
            log.error("Error fetching jobs from Remotive: {}", e.getMessage());
        }

        return jobs;
    }

    private Job parseJob(JsonNode node) {
        try {
            String title = getString(node, "title");
            String companyName = getString(node, "company_name");
            String location = getString(node, "candidate_required_location");
            String description = getString(node, "description");

            // Parse date using DateParserUtil
            String dateStr = getString(node, "publication_date");
            LocalDate postedDate = dateParserUtil.parseDate(dateStr);

            return Job.builder()
                    .title(title)
                    .companyName(companyName)
                    .location(location)
                    .experienceRequired(extractExperience(description))
                    .skills(extractSkills(description))
                    .description(description)
                    .source("Remotive")
                    .sourceJobId(getString(node, "id"))
                    .applyUrl(getString(node, "url"))
                    .postedDate(postedDate)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing Remotive job: {}", e.getMessage());
            return null;
        }
    }

    private String getString(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull()
                ? node.get(field).asText()
                : null;
    }

    private String extractExperience(String description) {
        if (description == null)
            return null;
        String[] patterns = { "\\d+\\+?\\s*(?:-\\s*\\d+)?\\s*years?", "\\d+\\s*to\\s*\\d+\\s*years?" };
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern,
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(description);
            if (m.find()) {
                return m.group();
            }
        }
        return null;
    }

    private String extractSkills(String description) {
        return jobParsingUtil.extractSkills(description);
    }
}