package com.fleetops.service.report.generators;

import com.fleetops.dto.report.ReportFilterDTO;
import com.fleetops.entity.Vehicle;
import com.fleetops.repository.VehicleRepository;
import com.fleetops.service.report.AbstractReportGenerator;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class VehicleReportGenerator extends AbstractReportGenerator<Vehicle, ReportFilterDTO> {

    private final VehicleRepository vehicleRepository;

    public VehicleReportGenerator(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public boolean supports(String reportType) {
        return "VEHICLE".equalsIgnoreCase(reportType);
    }

    @Override
    protected String getReportTitle() {
        return "Vehicle Inventory & Status Report";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Vehicle Number", "Brand", "Model", "Year", "VIN", "Status", "Plate Number", "Registration Date"};
    }

    @Override
    protected List<Vehicle> fetchData(ReportFilterDTO filters) {
        // Ideally we should use a criteria query for this, but for simplicity we fetch all non-deleted and filter in memory if needed.
        // In a real enterprise system, we would create a specific Specification for filtering.
        List<Vehicle> allVehicles = vehicleRepository.findAllByDeletedFalse();
        
        return allVehicles.stream().filter(v -> {
            boolean matches = true;
            if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
                matches = matches && v.getStatus().name().equalsIgnoreCase(filters.getStatus());
            }
            if (filters.getVehicleId() != null) {
                matches = matches && v.getId().equals(filters.getVehicleId());
            }
            return matches;
        }).toList();
    }

    @Override
    protected String[] getRowData(Vehicle v) {
        return new String[]{
                v.getVehicleNumber(),
                v.getBrand(),
                v.getModel(),
                v.getManufacturingYear() != null ? String.valueOf(v.getManufacturingYear()) : "",
                "N/A", // vin not available
                v.getStatus().name(),
                "N/A", // licensePlate not available
                "N/A" // registrationDate not available
        };
    }

    @Override
    protected String getAppliedFiltersString(ReportFilterDTO filters) {
        List<String> activeFilters = new ArrayList<>();
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
            activeFilters.add("Status: " + filters.getStatus());
        }
        if (filters.getVehicleId() != null) {
            activeFilters.add("Vehicle ID: " + filters.getVehicleId());
        }
        return activeFilters.isEmpty() ? "None" : String.join("; ", activeFilters);
    }
}
