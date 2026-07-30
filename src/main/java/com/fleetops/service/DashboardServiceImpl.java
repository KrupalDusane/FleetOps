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

    public DashboardServiceImpl(VehicleRepository vehicleRepository,
                                DriverRepository driverRepository,
                                FuelLogRepository fuelLogRepository,
                                MaintenanceRepository maintenanceRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.fuelLogRepository = fuelLogRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalVehicles(vehicleRepository.count())
                .availableVehicles(vehicleRepository.countByStatus(VehicleStatus.AVAILABLE))
                .underMaintenance(vehicleRepository.countByStatus(VehicleStatus.UNDER_MAINTENANCE))
                .inService(vehicleRepository.countByStatus(VehicleStatus.IN_SERVICE))
                .totalDrivers(driverRepository.count())
                .totalFuelLogs(fuelLogRepository.count())
                .totalMaintenanceLogs(maintenanceRepository.count())
                .build();
    }

    @Override
    public java.util.List<com.fleetops.entity.Vehicle> getRecentVehicles() {
        return vehicleRepository.findTop5ByOrderByIdDesc();
    }
}
