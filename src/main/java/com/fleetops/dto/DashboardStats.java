package com.fleetops.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {
    private long totalVehicles;
    private long availableVehicles;
    private long underMaintenance;
    private long inService;
    private long totalDrivers;
    private long totalFuelLogs;
    private long totalMaintenanceLogs;
}
