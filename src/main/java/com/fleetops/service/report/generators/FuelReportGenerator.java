package com.fleetops.service.report.generators;

import com.fleetops.dto.report.ReportFilterDTO;
import com.fleetops.entity.FuelLog;
import com.fleetops.repository.FuelLogRepository;
import com.fleetops.service.report.AbstractReportGenerator;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class FuelReportGenerator extends AbstractReportGenerator<FuelLog, ReportFilterDTO> {

    private final FuelLogRepository fuelLogRepository;

    public FuelReportGenerator(FuelLogRepository fuelLogRepository) {
        this.fuelLogRepository = fuelLogRepository;
    }

    @Override
    public boolean supports(String reportType) {
        return "FUEL".equalsIgnoreCase(reportType);
    }

    @Override
    protected String getReportTitle() {
        return "Fuel Expense & Consumption Report";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Date", "Vehicle", "Odometer", "Quantity (L)", "Price/Litre", "Total Cost"};
    }

    @Override
    protected List<FuelLog> fetchData(ReportFilterDTO filters) {
        List<FuelLog> logs = fuelLogRepository.findAllByDeletedFalse();
        
        return logs.stream().filter(f -> {
            boolean matches = true;
            if (filters.getVehicleId() != null && f.getVehicle() != null) {
                matches = matches && f.getVehicle().getId().equals(filters.getVehicleId());
            }
            if (filters.getStartDate() != null && f.getFuelDate() != null) {
                matches = matches && !f.getFuelDate().isBefore(filters.getStartDate());
            }
            if (filters.getEndDate() != null && f.getFuelDate() != null) {
                matches = matches && !f.getFuelDate().isAfter(filters.getEndDate());
            }
            if (filters.getMinCost() != null && f.getTotalCost() != null) {
                matches = matches && f.getTotalCost() >= filters.getMinCost();
            }
            if (filters.getMaxCost() != null && f.getTotalCost() != null) {
                matches = matches && f.getTotalCost() <= filters.getMaxCost();
            }
            return matches;
        }).toList();
    }

    @Override
    protected String[] getRowData(FuelLog f) {
        return new String[]{
                f.getFuelDate() != null ? f.getFuelDate().format(DateTimeFormatter.ISO_DATE) : "",
                f.getVehicle() != null ? f.getVehicle().getVehicleNumber() : "Unknown",
                f.getOdometerAtFueling() != null ? String.valueOf(f.getOdometerAtFueling()) : "0",
                f.getFuelQuantity() != null ? String.format("%.2f", f.getFuelQuantity()) : "0.00",
                f.getPricePerLitre() != null ? String.format("%.2f", f.getPricePerLitre()) : "0.00",
                f.getTotalCost() != null ? String.format("%.2f", f.getTotalCost()) : "0.00"
        };
    }

    @Override
    protected String getAppliedFiltersString(ReportFilterDTO filters) {
        List<String> activeFilters = new ArrayList<>();
        if (filters.getVehicleId() != null) activeFilters.add("Vehicle ID: " + filters.getVehicleId());
        if (filters.getStartDate() != null) activeFilters.add("Start Date: " + filters.getStartDate());
        if (filters.getEndDate() != null) activeFilters.add("End Date: " + filters.getEndDate());
        if (filters.getMinCost() != null) activeFilters.add("Min Cost: " + filters.getMinCost());
        if (filters.getMaxCost() != null) activeFilters.add("Max Cost: " + filters.getMaxCost());
        return activeFilters.isEmpty() ? "None" : String.join("; ", activeFilters);
    }
}
