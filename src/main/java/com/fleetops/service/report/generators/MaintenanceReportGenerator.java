package com.fleetops.service.report.generators;

import com.fleetops.dto.report.ReportFilterDTO;
import com.fleetops.entity.Maintenance;
import com.fleetops.repository.MaintenanceRepository;
import com.fleetops.service.report.AbstractReportGenerator;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class MaintenanceReportGenerator extends AbstractReportGenerator<Maintenance, ReportFilterDTO> {

    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceReportGenerator(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public boolean supports(String reportType) {
        return "MAINTENANCE".equalsIgnoreCase(reportType);
    }

    @Override
    protected String getReportTitle() {
        return "Maintenance Logs & Service Report";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Vehicle", "Service Date", "Description", "Garage", "Cost", "Status", "Next Service Date"};
    }

    @Override
    protected List<Maintenance> fetchData(ReportFilterDTO filters) {
        List<Maintenance> logs = maintenanceRepository.findAllByDeletedFalse();
        
        return logs.stream().filter(m -> {
            boolean matches = true;
            if (filters.getVehicleId() != null && m.getVehicle() != null) {
                matches = matches && m.getVehicle().getId().equals(filters.getVehicleId());
            }
            if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
                matches = matches && m.getStatus().name().equalsIgnoreCase(filters.getStatus());
            }
            if (filters.getStartDate() != null && m.getServiceDate() != null) {
                matches = matches && !m.getServiceDate().isBefore(filters.getStartDate());
            }
            if (filters.getEndDate() != null && m.getServiceDate() != null) {
                matches = matches && !m.getServiceDate().isAfter(filters.getEndDate());
            }
            if (filters.getMinCost() != null && m.getCost() != null) {
                matches = matches && m.getCost() >= filters.getMinCost();
            }
            if (filters.getMaxCost() != null && m.getCost() != null) {
                matches = matches && m.getCost() <= filters.getMaxCost();
            }
            return matches;
        }).toList();
    }

    @Override
    protected String[] getRowData(Maintenance m) {
        return new String[]{
                m.getVehicle() != null ? m.getVehicle().getVehicleNumber() : "Unknown",
                m.getServiceDate() != null ? m.getServiceDate().format(DateTimeFormatter.ISO_DATE) : "",
                "N/A", // description not available
                m.getGarage(),
                m.getCost() != null ? String.format("%.2f", m.getCost()) : "0.00",
                m.getStatus() != null ? m.getStatus().name() : "",
                m.getNextServiceDate() != null ? m.getNextServiceDate().format(DateTimeFormatter.ISO_DATE) : ""
        };
    }

    @Override
    protected String getAppliedFiltersString(ReportFilterDTO filters) {
        List<String> activeFilters = new ArrayList<>();
        if (filters.getVehicleId() != null) activeFilters.add("Vehicle ID: " + filters.getVehicleId());
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) activeFilters.add("Status: " + filters.getStatus());
        if (filters.getStartDate() != null) activeFilters.add("Start Date: " + filters.getStartDate());
        if (filters.getEndDate() != null) activeFilters.add("End Date: " + filters.getEndDate());
        if (filters.getMinCost() != null) activeFilters.add("Min Cost: " + filters.getMinCost());
        if (filters.getMaxCost() != null) activeFilters.add("Max Cost: " + filters.getMaxCost());
        return activeFilters.isEmpty() ? "None" : String.join("; ", activeFilters);
    }
}
