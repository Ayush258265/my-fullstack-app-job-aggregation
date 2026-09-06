package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSetupRequest {
	
	private String topic ;
	private Integer durationMinutes ;
	private String mode;
	
}
