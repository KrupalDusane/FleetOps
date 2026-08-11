package com.fleetops.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

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
    
    // Sprint 1 & 7 Enterprise Analytics
    private Double monthlyFuelCost;
    private java.math.BigDecimal totalFuelCost;
    private java.math.BigDecimal averageFuelCostPerVehicle;
    private String topFuelConsumingVehicle;
    
    private long fuelRecordsThisMonth;
    private FleetHealthDTO fleetHealth;
    // Chart Data
    
    // Sprint 5 Document Stats
    private long totalDocuments;
    private long documentsExpiringSoon;
    private long expiredDocuments;
    private List<Double> monthlyFuelExpenseChart;
    private Map<String, Long> vehicleStatusDistributionChart;
    private List<Long> maintenanceTrendChart;
}
