package com.fleetops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private String title;
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW
    private String module; // MAINTENANCE, FUEL, DOCUMENT, VEHICLE
    private String description;
}
