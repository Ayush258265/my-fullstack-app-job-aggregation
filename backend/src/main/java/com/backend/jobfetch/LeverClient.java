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
public class LeverClient {

	private final ApiClient apiClient;
	private final ObjectMapper objectMapper;
	private final DateParserUtil dateParserUtil;
	private final JobParsingUtil jobParsingUtil;

	@Value("${lever.api.url:https://api.lever.co/v0/postings}")
	private String apiUrl;

	private final String[] companies = { "company1", "company2" };

	public List<Job> fetchJobs() {
		List<Job> jobs = new ArrayList<>();

		try {
			for (String company : companies) {
				String url = String.format("%s/%s", apiUrl, company);
				String response = apiClient.get(url);

				if (response == null) {
					log.warn("No response from Lever for company: {}", company);
					continue;
				}

				JsonNode root = objectMapper.readTree(response);

				if (root.isArray()) {
					for (JsonNode jobNode : root) {
						Job job = parseJob(jobNode, company);
						if (job != null) {
							jobs.add(job);
						}
					}
				}
			}
			log.info("Fetched {} jobs from Lever", jobs.size());
		} catch (Exception e) {
			log.error("Error fetching jobs from Lever: {}", e.getMessage());
		}

		return jobs;
	}

	private Job parseJob(JsonNode node, String company) {
		try {
			String title = getString(node, "title");
			String description = getString(node, "description");

			// Parse date using DateParserUtil
			String dateStr = getString(node, "createdAt");
			LocalDate postedDate = dateParserUtil.parseDate(dateStr);

			// ✅ FIXED: Use the correct builder method name
			return Job.builder().title(title).companyName(company).location(getLocation(node))
					.experienceRequired(extractExperience(description)).skills(extractSkills(description))
					.description(description).source("Lever").sourceJobId(getString(node, "id"))
					.applyUrl(getString(node, "applyUrl")).postedDate(postedDate) // ✅ This matches the field name
					.build();
		} catch (Exception e) {
			log.error("Error parsing Lever job: {}", e.getMessage());
			return null;
		}
	}

	private String getString(JsonNode node, String field) {
		return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
	}

	private String getLocation(JsonNode node) {
		if (node.has("categories") && !node.get("categories").isNull()) {
			JsonNode categories = node.get("categories");
			if (categories.has("location") && !categories.get("location").isNull()) {
				return categories.get("location").asText();
			}
		}
		return null;
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