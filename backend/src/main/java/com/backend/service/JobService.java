package com.backend.service;

import com.backend.dto.FilterOptionsDTO;
import com.backend.repo.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public FilterOptionsDTO getFilterOptions() {
        FilterOptionsDTO options = new FilterOptionsDTO();
        options.setLocations(jobRepository.findAllLocations());
        options.setCompanies(jobRepository.findAllCompanies());
        options.setExperienceLevels(jobRepository.findAllExperienceLevels());
        return options;
        
    }
}