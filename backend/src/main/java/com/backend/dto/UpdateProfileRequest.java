package com.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
	
    private String primarySkills;
    private Integer yearsOfExperience;
    private String preferredLocation;
    
}