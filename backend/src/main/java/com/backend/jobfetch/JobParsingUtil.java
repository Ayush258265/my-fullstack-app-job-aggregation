package com.backend.jobfetch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class JobParsingUtil {

	// Common skills database
	private static final String[] COMMON_SKILLS = { "Java", "Python", "JavaScript", "TypeScript", "React", "Angular",
			"Vue", "Spring Boot", "Node.js", "Django", "Flask", "Express", "SQL", "MySQL", "PostgreSQL", "MongoDB",
			"Redis", "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "Git", "CI/CD", "Agile", "Scrum", "Jira",
			"Machine Learning", "AI", "Deep Learning", "NLP", "Data Science", "Graphic Design", "Adobe Illustrator",
			"Adobe Creative Suite", "C++", "C#", ".NET", "PHP", "Ruby", "Go",
			"Rust", "HTML", "CSS", "SASS", "Bootstrap", "Tailwind", "GraphQL", "REST API", "Microservices", "Kafka",
			"RabbitMQ" };

	/**
	 * ✅ Get string value from JsonNode field Method name: getString (capital 'S')
	 */
	public String getString(JsonNode node, String field) {
		if (node == null || field == null) {
			return null;
		}
		return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
	}

	/**
	 * ✅ Extract experience requirement from description Method name:
	 * extractExperience
	 */
	public String extractExperience(String description) {
		if (description == null || description.isEmpty()) {
			return null;
		}

		String[] patterns = { "(\\d+\\+?\\s*(?:-\\s*\\d+)?)\\s*(?:years?|yrs?)",
				"(\\d+)\\s*(?:-|to)\\s*(\\d+)\\s*(?:years?|yrs?)", "(\\d+)\\s*\\+\\s*(?:years?|yrs?)",
				"(\\d+)\\s*(?:years?|yrs?)\\s*(?:of)?\\s*experience",
				"experience\\s*(?:of)?\\s*(\\d+)\\s*\\+?\\s*(?:years?|yrs?)" };

		for (String patternStr : patterns) {
			Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(description);
			if (matcher.find()) {
				return matcher.group();
			}
		}
		return null;
	}

	/**
	 * ✅ Extract skills from description Method name: extractSkills (capital 'S')
	 */
	public String extractSkills(String description) {
		if (description == null || description.isEmpty()) {
			return null;
		}

		List<String> foundSkills = new ArrayList<>();
		String lowerDesc = description.toLowerCase();

		for (String skill : COMMON_SKILLS) {
			if (lowerDesc.contains(skill.toLowerCase())) {
				foundSkills.add(skill);
			}
		}

		return foundSkills.isEmpty() ? null : String.join(", ", foundSkills);
	}
}