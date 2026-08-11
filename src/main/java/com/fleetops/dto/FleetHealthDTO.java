package com.fleetops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetHealthDTO {
    private int score;
    private FleetHealthGrade grade;
    private String summary;
    
    // Component Scores
    private int availabilityScore;
    private int maintenanceScore;
    private int fuelScore;
    private int documentScore;

    private List<String> strengths;
    private List<String> risks;
    private List<RecommendationDTO> recommendations;
}
