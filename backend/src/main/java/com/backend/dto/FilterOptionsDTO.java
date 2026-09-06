package com.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterOptionsDTO {
    private List<String> locations;
    private List<String> companies;
    private List<String> experienceLevels;
}