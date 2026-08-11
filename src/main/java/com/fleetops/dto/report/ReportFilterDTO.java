package com.fleetops.dto.report;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportFilterDTO {
    private String reportType;
    private String format;
    private Long vehicleId;
    private Long driverId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double minCost;
    private Double maxCost;
}
