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
public class ArbeitnowClient {

	private final ApiClient apiClient;
	private final ObjectMapper objectMapper;
	private final DateParserUtil dateParserUtil;
	private final JobParsingUtil jobParsingUtil;

	@Value("${arbeitnow.api.url:https://api.arbeitnow.com/api/v1/jobs}")
	private String apiUrl;

	public List<Job> fetchJobs() {
		List<Job> jobs = new ArrayList<>();

		try {
			String response = apiClient.get(apiUrl);
			if (response == null) {
				log.warn("No response from Arbeitnow API");
				return jobs;
			}

			JsonNode root = objectMapper.readTree(response);
			JsonNode data = root.get("data");

			if (data != null && data.isArray()) {
				for (JsonNode jobNode : data) {
					Job job = parseJob(jobNode);
					if (job != null) {
						jobs.add(job);
					}
				}
				log.info("Fetched {} jobs from Arbeitnow", jobs.size());
			}
		} catch (Exception e) {
			log.error("Error fetching jobs from Arbeitnow: {}", e.getMessage());
		}

		return jobs;
	}

	private Job parseJob(JsonNode node) {
		try {
			// ✅ CORRECTED: Using getString (capital 'S')
			String title = jobParsingUtil.getString(node, "title");
			String companyName = jobParsingUtil.getString(node, "company_name");
			String location = jobParsingUtil.getString(node, "location");
			String description = jobParsingUtil.getString(node, "description");
			String sourceJobId = jobParsingUtil.getString(node, "slug");
			String applyUrl = jobParsingUtil.getString(node, "url");

			// Parse date using DateParserUtil
			String dateStr = jobParsingUtil.getString(node, "published_at");
			LocalDate postedDate = dateParserUtil.parseDate(dateStr);

			// ✅ CORRECTED: Fixed all method names
			return Job.builder().title(title).companyName(companyName).location(location)
					.experienceRequired(jobParsingUtil.extractExperience(description))
					.skills(jobParsingUtil.extractSkills(description)) // ✅ Fixed: extractSkills
					.description(description).source("Arbeitnow").sourceJobId(sourceJobId).applyUrl(applyUrl)
					.postedDate(postedDate).build();

		} catch (Exception e) {
			log.error("Error parsing Arbeitnow job: {}", e.getMessage());
			return null;
		}
	}
}