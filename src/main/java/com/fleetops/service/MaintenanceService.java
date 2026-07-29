package com.fleetops.service;

import com.fleetops.entity.Maintenance;
import com.fleetops.entity.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MaintenanceService {

    Maintenance createMaintenance(Maintenance maintenance);

    List<Maintenance> getAllMaintenanceLogs();

    Maintenance getMaintenanceById(Long id);

    Maintenance updateMaintenance(Long id, Maintenance maintenance);

    void deleteMaintenance(Long id);

    Page<Maintenance> searchAndFilterMaintenance(Long vehicleId, String garage, MaintenanceStatus status, Pageable pageable);
}
