package com.fleetops.service.report.generators;

import com.fleetops.dto.report.ReportFilterDTO;
import com.fleetops.entity.Driver;
import com.fleetops.repository.DriverRepository;
import com.fleetops.service.report.AbstractReportGenerator;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DriverReportGenerator extends AbstractReportGenerator<Driver, ReportFilterDTO> {

    private final DriverRepository driverRepository;

    public DriverReportGenerator(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public boolean supports(String reportType) {
        return "DRIVER".equalsIgnoreCase(reportType);
    }

    @Override
    protected String getReportTitle() {
        return "Driver Roster & Status Report";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Name", "License Number", "License Expiry", "Phone", "Status"};
    }

    @Override
    protected List<Driver> fetchData(ReportFilterDTO filters) {
        List<Driver> drivers = driverRepository.findAllByDeletedFalse();
        
        return drivers.stream().filter(d -> {
            boolean matches = true;
            if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
                matches = matches && d.getStatus().name().equalsIgnoreCase(filters.getStatus());
            }
            if (filters.getDriverId() != null) {
                matches = matches && d.getId().equals(filters.getDriverId());
            }
            return matches;
        }).toList();
    }

    @Override
    protected String[] getRowData(Driver d) {
        return new String[]{
                d.getName(),
                d.getLicenseNumber(),
                "N/A", // license expiry not available
                d.getPhone(),
                d.getStatus().name()
        };
    }

    @Override
    protected String getAppliedFiltersString(ReportFilterDTO filters) {
        List<String> activeFilters = new ArrayList<>();
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
            activeFilters.add("Status: " + filters.getStatus());
        }
        if (filters.getDriverId() != null) {
            activeFilters.add("Driver ID: " + filters.getDriverId());
        }
        return activeFilters.isEmpty() ? "None" : String.join("; ", activeFilters);
    }
}
