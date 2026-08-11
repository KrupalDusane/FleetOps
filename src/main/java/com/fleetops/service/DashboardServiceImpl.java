package com.fleetops.service;

import com.fleetops.dto.DashboardStats;
import com.fleetops.entity.VehicleStatus;
import com.fleetops.repository.DriverRepository;
import com.fleetops.repository.FuelLogRepository;
import com.fleetops.repository.MaintenanceRepository;
import com.fleetops.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final com.fleetops.repository.VehicleDocumentRepository vehicleDocumentRepository;
    private final FuelAnalyticsService fuelAnalyticsService;
    private final FleetHealthService fleetHealthService;

    public DashboardServiceImpl(VehicleRepository vehicleRepository,
                                DriverRepository driverRepository,
                                FuelLogRepository fuelLogRepository,
                                MaintenanceRepository maintenanceRepository,
                                com.fleetops.repository.VehicleDocumentRepository vehicleDocumentRepository,
                                FuelAnalyticsService fuelAnalyticsService,
                                FleetHealthService fleetHealthService) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.fuelLogRepository = fuelLogRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleDocumentRepository = vehicleDocumentRepository;
        this.fuelAnalyticsService = fuelAnalyticsService;
        this.fleetHealthService = fleetHealthService;
    }

    @Override
    public DashboardStats getDashboardStats() {
        long available = 0;
        long inService = 0;
        long maintenance = 0;
        long totalVehicles = 0;
        
        for (Object[] row : vehicleRepository.getVehicleStatusCounts()) {
            VehicleStatus status = (VehicleStatus) row[0];
            long count = ((Number) row[1]).longValue();
            totalVehicles += count;
            if (status == VehicleStatus.AVAILABLE) available = count;
            else if (status == VehicleStatus.IN_SERVICE) inService = count;
            else if (status == VehicleStatus.UNDER_MAINTENANCE) maintenance = count;
        }
        long totalDrivers = driverRepository.countByDeletedFalse();
        long totalFuelLogs = fuelLogRepository.countByDeletedFalse();
        long totalMaintenanceLogs = maintenanceRepository.countByDeletedFalse();

        // Enterprise Analytics
        com.fleetops.dto.FuelAnalyticsDTO analytics = fuelAnalyticsService.getGlobalFuelAnalytics();
        Double monthlyFuelCost = analytics.getMonthlyFuelCost().doubleValue();
        java.math.BigDecimal totalFuelCost = analytics.getTotalFuelCost();
        java.math.BigDecimal avgFuelCost = analytics.getAverageFuelCostPerVehicle();
        String topVehicle = analytics.getHighestFuelConsumingVehicle();
        long fuelRecordsThisMonth = fuelLogRepository.countFuelLogsForCurrentMonthAndDeletedFalse();

        java.time.LocalDate today = java.time.LocalDate.now();
        long totalDocs = vehicleDocumentRepository.countByDeletedFalseAndArchivedFalse();
        long expiredDocs = vehicleDocumentRepository.countExpiredDocuments(today);
        long expiringSoonDocs = vehicleDocumentRepository.countExpiringSoonDocuments(today, today.plusDays(31));
        
        long overdueMaintenance = maintenanceRepository.countOverdueMaintenance();

        com.fleetops.dto.FleetHealthDTO fleetHealth = fleetHealthService.calculateHealth(
                totalVehicles,
                available,
                maintenance,
                expiringSoonDocs,
                expiredDocs,
                overdueMaintenance,
                analytics
        );

        // Charts
        java.util.List<Double> fuelExpenseChart = new java.util.ArrayList<>(java.util.Collections.nCopies(12, 0.0));
        for (Object[] row : fuelLogRepository.getMonthlyFuelExpenseCurrentYearAndDeletedFalse()) {
            int month = (Integer) row[0];
            double cost = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            fuelExpenseChart.set(month - 1, cost);
        }

        java.util.List<Long> maintenanceChart = new java.util.ArrayList<>(java.util.Collections.nCopies(12, 0L));
        for (Object[] row : maintenanceRepository.getMonthlyMaintenanceCountCurrentYearAndDeletedFalse()) {
            int month = (Integer) row[0];
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            maintenanceChart.set(month - 1, count);
        }

        java.util.Map<String, Long> statusChart = new java.util.HashMap<>();
        statusChart.put("Available", available);
        statusChart.put("In Service", inService);
        statusChart.put("Under Maintenance", maintenance);

        return DashboardStats.builder()
                .totalVehicles(totalVehicles)
                .availableVehicles(available)
                .underMaintenance(maintenance)
                .inService(inService)
                .totalDrivers(totalDrivers)
                .totalFuelLogs(totalFuelLogs)
                .totalMaintenanceLogs(totalMaintenanceLogs)
                .monthlyFuelCost(monthlyFuelCost)
                .totalFuelCost(totalFuelCost)
                .averageFuelCostPerVehicle(avgFuelCost)
                .topFuelConsumingVehicle(topVehicle)
                .fuelRecordsThisMonth(fuelRecordsThisMonth)
                .totalDocuments(totalDocs)
                .documentsExpiringSoon(expiringSoonDocs)
                .expiredDocuments(expiredDocs)
                .fleetHealth(fleetHealth)
                .monthlyFuelExpenseChart(fuelExpenseChart)
                .maintenanceTrendChart(maintenanceChart)
                .vehicleStatusDistributionChart(statusChart)
                .build();
    }

    @Override
    public java.util.List<com.fleetops.entity.Vehicle> getRecentVehicles() {
        return vehicleRepository.findTop5ByDeletedFalseOrderByIdDesc();
    }
}
