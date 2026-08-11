package com.fleetops.service;

import com.fleetops.dto.FleetHealthDTO;
import com.fleetops.dto.FleetHealthGrade;
import com.fleetops.dto.FuelAnalyticsDTO;
import com.fleetops.dto.RecommendationDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FleetHealthServiceImpl implements FleetHealthService {

    private static final double WEIGHT_AVAILABILITY = 0.40;
    private static final double WEIGHT_MAINTENANCE = 0.25;
    private static final double WEIGHT_DOCUMENTS = 0.20;
    private static final double WEIGHT_FUEL = 0.15;

    @Override
    public FleetHealthDTO calculateHealth(
            long totalVehicles,
            long available,
            long maintenance,
            long expiringDocs,
            long expiredDocs,
            long overdueMaintenance,
            FuelAnalyticsDTO fuelAnalytics) {

        if (totalVehicles == 0) {
            return buildEmptyHealth();
        }

        List<String> strengths = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<RecommendationDTO> recommendations = new ArrayList<>();

        int availabilityScore = calculateAvailability(totalVehicles, available, maintenance, strengths, risks, recommendations);
        int maintenanceScore = calculateMaintenance(totalVehicles, overdueMaintenance, strengths, risks, recommendations);
        int documentScore = calculateDocuments(totalVehicles, expiredDocs, expiringDocs, strengths, risks, recommendations);
        int fuelScore = calculateFuel(fuelAnalytics, strengths, risks, recommendations);

        int totalScore = (int) Math.round(
                (availabilityScore * WEIGHT_AVAILABILITY) +
                (maintenanceScore * WEIGHT_MAINTENANCE) +
                (documentScore * WEIGHT_DOCUMENTS) +
                (fuelScore * WEIGHT_FUEL)
        );

        totalScore = Math.max(0, Math.min(100, totalScore));
        FleetHealthGrade grade = determineGrade(totalScore);

        String summary = generateSummary(grade);

        return FleetHealthDTO.builder()
                .score(totalScore)
                .grade(grade)
                .summary(summary)
                .availabilityScore(availabilityScore)
                .maintenanceScore(maintenanceScore)
                .documentScore(documentScore)
                .fuelScore(fuelScore)
                .strengths(strengths)
                .risks(risks)
                .recommendations(recommendations)
                .build();
    }

    private int calculateAvailability(long total, long available, long maintenance, List<String> strengths, List<String> risks, List<RecommendationDTO> recommendations) {
        double ratio = (double) available / total;
        int score = (int) (ratio * 100);

        if (score >= 90) {
            strengths.add("Excellent vehicle availability.");
        } else if (score < 70) {
            risks.add((total - available) + " vehicles are currently unavailable.");
            recommendations.add(new RecommendationDTO("Improve Availability", "HIGH", "VEHICLE", "Investigate high number of unavailable vehicles to restore fleet capacity."));
        }
        return score;
    }

    private int calculateMaintenance(long total, long overdueMaintenance, List<String> strengths, List<String> risks, List<RecommendationDTO> recommendations) {
        if (overdueMaintenance == 0) {
            strengths.add("No overdue maintenance tasks.");
            return 100;
        }

        double overdueRatio = (double) overdueMaintenance / total;
        int score = 100 - (int) (overdueRatio * 100 * 2); // Penalize heavily
        score = Math.max(0, score);

        risks.add(overdueMaintenance + " Vehicles overdue for service.");
        recommendations.add(new RecommendationDTO("Overdue Maintenance", "CRITICAL", "MAINTENANCE", "Schedule maintenance immediately for " + overdueMaintenance + " vehicles."));

        return score;
    }

    private int calculateDocuments(long total, long expired, long expiringSoon, List<String> strengths, List<String> risks, List<RecommendationDTO> recommendations) {
        if (expired == 0 && expiringSoon == 0) {
            strengths.add("All vehicle documents are up to date.");
            return 100;
        }

        int score = 100;
        if (expired > 0) {
            score -= Math.min(100, (expired * 20)); // -20 per expired
            risks.add(expired + " documents are currently expired.");
            recommendations.add(new RecommendationDTO("Expired Documents", "CRITICAL", "DOCUMENT", "Renew " + expired + " expired documents immediately to ensure legal compliance."));
        }
        
        if (expiringSoon > 0) {
            score -= Math.min(score, (expiringSoon * 5)); // -5 per expiring soon
            risks.add(expiringSoon + " documents expiring soon.");
            recommendations.add(new RecommendationDTO("Upcoming Expirations", "MEDIUM", "DOCUMENT", "Prepare renewal for " + expiringSoon + " documents expiring within 30 days."));
        }

        return Math.max(0, score);
    }

    private int calculateFuel(FuelAnalyticsDTO fuelAnalytics, List<String> strengths, List<String> risks, List<RecommendationDTO> recommendations) {
        if (fuelAnalytics == null || fuelAnalytics.getMonthOverMonthTrend() == null) {
            return 80; // Default baseline if no data
        }

        int score = 80; // Start at baseline
        BigDecimal momTrend = fuelAnalytics.getMonthOverMonthTrend();
        
        if (momTrend.compareTo(BigDecimal.ZERO) > 0) {
            // Expenses increased
            double increase = momTrend.doubleValue();
            score -= (int) increase;
            if (increase > 10) {
                risks.add(String.format("Fuel cost increased by %.1f%% this month.", increase));
                recommendations.add(new RecommendationDTO("Fuel Cost Spike", "HIGH", "FUEL", "Inspect vehicle efficiency and driver behavior due to rising fuel costs."));
            }
        } else if (momTrend.compareTo(BigDecimal.ZERO) < 0) {
            // Expenses decreased
            double decrease = Math.abs(momTrend.doubleValue());
            score += (int) (decrease / 2);
            if (decrease > 5) {
                strengths.add(String.format("Fuel costs decreased by %.1f%%.", decrease));
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    private FleetHealthGrade determineGrade(int score) {
        if (score >= 90) return FleetHealthGrade.A;
        if (score >= 80) return FleetHealthGrade.B;
        if (score >= 70) return FleetHealthGrade.C;
        if (score >= 60) return FleetHealthGrade.D;
        return FleetHealthGrade.F;
    }

    private String generateSummary(FleetHealthGrade grade) {
        switch (grade) {
            case A: return "Fleet is operating in optimal condition with minimal risks.";
            case B: return "Fleet is healthy, but minor improvements can be made.";
            case C: return "Fleet operations are acceptable, but several risks require attention.";
            case D: return "Fleet health is poor. Immediate action is required to prevent operational failures.";
            case F: return "Critical failure in fleet operations. Severe risks detected.";
            default: return "Unknown state.";
        }
    }

    private FleetHealthDTO buildEmptyHealth() {
        return FleetHealthDTO.builder()
                .score(0)
                .grade(FleetHealthGrade.F)
                .summary("No vehicles found in the fleet.")
                .availabilityScore(0)
                .maintenanceScore(0)
                .documentScore(0)
                .fuelScore(0)
                .strengths(new ArrayList<>())
                .risks(List.of("No vehicles exist."))
                .recommendations(List.of(new RecommendationDTO("Add Vehicles", "HIGH", "VEHICLE", "Onboard vehicles to the fleet to begin tracking health.")))
                .build();
    }
}
