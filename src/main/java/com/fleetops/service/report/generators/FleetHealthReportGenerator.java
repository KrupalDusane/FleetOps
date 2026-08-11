package com.fleetops.service.report.generators;

import com.fleetops.dto.FleetHealthDTO;
import com.fleetops.dto.RecommendationDTO;
import com.fleetops.dto.report.ReportFilterDTO;
import com.fleetops.service.DashboardService;
import com.fleetops.service.report.AbstractReportGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FleetHealthReportGenerator extends AbstractReportGenerator<String[], ReportFilterDTO> {

    private final DashboardService dashboardService;

    public FleetHealthReportGenerator(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public boolean supports(String reportType) {
        return "FLEET_HEALTH".equalsIgnoreCase(reportType);
    }

    @Override
    protected String getReportTitle() {
        return "Fleet Health Intelligence Report";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Category", "Detail", "Priority/Score"};
    }

    @Override
    protected List<String[]> fetchData(ReportFilterDTO filters) {
        // Since Fleet Health is a singleton report essentially, we will map its properties to rows
        FleetHealthDTO health = dashboardService.getDashboardStats().getFleetHealth();
        List<String[]> rows = new ArrayList<>();

        if (health == null) {
            rows.add(new String[]{"Overall", "No data available", "N/A"});
            return rows;
        }

        // Add Overview
        rows.add(new String[]{"OVERVIEW", "Overall Fleet Score", String.valueOf(health.getScore())});
        rows.add(new String[]{"OVERVIEW", "Overall Fleet Grade", health.getGrade().name()});
        rows.add(new String[]{"OVERVIEW", "Summary", health.getSummary()});
        
        // Add Components
        rows.add(new String[]{"COMPONENT", "Availability Score", String.valueOf(health.getAvailabilityScore())});
        rows.add(new String[]{"COMPONENT", "Maintenance Score", String.valueOf(health.getMaintenanceScore())});
        rows.add(new String[]{"COMPONENT", "Document Score", String.valueOf(health.getDocumentScore())});
        rows.add(new String[]{"COMPONENT", "Fuel Trend Score", String.valueOf(health.getFuelScore())});

        // Add Strengths
        if (health.getStrengths() != null) {
            for (String strength : health.getStrengths()) {
                rows.add(new String[]{"STRENGTH", strength, "POSITIVE"});
            }
        }

        // Add Risks
        if (health.getRisks() != null) {
            for (String risk : health.getRisks()) {
                rows.add(new String[]{"RISK", risk, "WARNING"});
            }
        }

        // Add Recommendations
        if (health.getRecommendations() != null) {
            for (RecommendationDTO rec : health.getRecommendations()) {
                rows.add(new String[]{
                        "RECOMMENDATION (" + rec.getModule() + ")", 
                        rec.getTitle() + ": " + rec.getDescription(), 
                        rec.getPriority()
                });
            }
        }

        return rows;
    }

    @Override
    protected String[] getRowData(String[] item) {
        return item; // Item is already a String[]
    }

    @Override
    protected String getAppliedFiltersString(ReportFilterDTO filters) {
        return "None (Global Snapshot)";
    }
}
