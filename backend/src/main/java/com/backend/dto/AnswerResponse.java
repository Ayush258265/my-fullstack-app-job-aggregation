package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {

	private Integer questionNumber;
	private Integer score;
	private String grammarCorrection;
	private String fluencyFeedback;
	private String contentFeedback;
	private String strengths;
	private String weaknesses;
	private Boolean isComplete;
	private Boolean showNextQuestion;
	private String nextAction;
	private String message;
	private String mode;

}
