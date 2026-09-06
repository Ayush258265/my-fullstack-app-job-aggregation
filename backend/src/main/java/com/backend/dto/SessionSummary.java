package com.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionSummary {

	private Long sessionId;
	private String topic;
	private String mode;
	private Integer durationMinutes;
	private Integer totalQuestions;
	private Integer answeredQuestions;
	private Double overallScore;
	private List<String> strengths;
	private List<String> weaknesses;
	private String commonMistakes;
	private String message;
	private String completedAt;

}
