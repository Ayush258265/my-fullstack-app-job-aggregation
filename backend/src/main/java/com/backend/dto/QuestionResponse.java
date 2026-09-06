package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
	
	private Long sessionId;
	private Integer questionNumber;
	private Integer totalQuestions;
	private String question;
	private Integer timeLimitSeconds;
	private String topic;
	private Boolean isLastQuestion;
	private String mode;
	
}
