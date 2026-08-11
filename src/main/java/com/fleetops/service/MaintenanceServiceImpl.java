package com.fleetops.service;

import com.fleetops.entity.Maintenance;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.ResourceNotFoundException;
import com.fleetops.repository.MaintenanceRepository;
import com.fleetops.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public MaintenanceServiceImpl(MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository, AuditLogService auditLogService) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditLogService = auditLogService;
    }



    private void validateRules(Maintenance maintenance) {
        if (maintenance.getServiceDate() != null && maintenance.getNextServiceDate() != null) {
            if (maintenance.getNextServiceDate().isBefore(maintenance.getServiceDate())) {
                throw new com.fleetops.exception.InvalidOperationException("Next service date must be after the service date");
            }
        }
        if (maintenance.getCost() != null && maintenance.getCost() < 0) {
            throw new com.fleetops.exception.InvalidOperationException("Service cost cannot be negative.");
        }
    }

    private void validateAndSetVehicle(Maintenance maintenance) {
        if (maintenance.getVehicle() != null && maintenance.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(maintenance.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active Vehicle not found with id: " + maintenance.getVehicle().getId()));
            maintenance.setVehicle(vehicle);
        } else {
            throw new com.fleetops.exception.InvalidOperationException("Maintenance log must be associated with a valid vehicle.");
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Maintenance createMaintenance(Maintenance maintenance) {
        maintenance.setId(null);
        validateAndSetVehicle(maintenance);
        validateRules(maintenance);
        Maintenance saved = maintenanceRepository.save(maintenance);
        auditLogService.logAction("Maintenance Scheduled", "Maintenance", saved.getId(), null, saved, "Scheduled maintenance for vehicle ID: " + saved.getVehicle().getId());
        return saved;
    }

    @Override
    public List<Maintenance> getAllMaintenanceLogs() {
        return maintenanceRepository.findAllByDeletedFalse();
    }

    @Override
    public Maintenance getMaintenanceById(Long id) {
        return maintenanceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance log not found with id: " + id));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Maintenance updateMaintenance(Long id, Maintenance maintenanceDetails) {
        Maintenance existingMaintenance = getMaintenanceById(id);
        
        Maintenance oldState = Maintenance.builder()
            .id(existingMaintenance.getId())
            .garage(existingMaintenance.getGarage())
            .cost(existingMaintenance.getCost())
            .serviceDate(existingMaintenance.getServiceDate())
            .nextServiceDate(existingMaintenance.getNextServiceDate())
            .status(existingMaintenance.getStatus())
            .vehicle(existingMaintenance.getVehicle())
            .build();

        existingMaintenance.setGarage(maintenanceDetails.getGarage());
        existingMaintenance.setCost(maintenanceDetails.getCost());
        existingMaintenance.setServiceDate(maintenanceDetails.getServiceDate());
        existingMaintenance.setNextServiceDate(maintenanceDetails.getNextServiceDate());
        existingMaintenance.setStatus(maintenanceDetails.getStatus());

        if (maintenanceDetails.getVehicle() != null && maintenanceDetails.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(maintenanceDetails.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active Vehicle not found with id: " + maintenanceDetails.getVehicle().getId()));
            existingMaintenance.setVehicle(vehicle);
        }

        validateRules(existingMaintenance);
        Maintenance saved = maintenanceRepository.save(existingMaintenance);
        
        String action = oldState.getStatus() != saved.getStatus() ? "Maintenance Status Changed" : "Maintenance Updated";
        auditLogService.logAction(action, "Maintenance", saved.getId(), oldState, saved, "Updated maintenance log ID: " + saved.getId());
        
        return saved;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteMaintenance(Long id) {
        Maintenance existingMaintenance = getMaintenanceById(id);
        if (existingMaintenance.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Maintenance log is already deleted");
        }
        
        Maintenance oldState = Maintenance.builder().id(existingMaintenance.getId()).garage(existingMaintenance.getGarage()).vehicle(existingMaintenance.getVehicle()).build();

        existingMaintenance.setDeleted(true);
        existingMaintenance.setDeletedAt(java.time.LocalDateTime.now());
        existingMaintenance.setDeletedBy(com.fleetops.util.SecurityUtils.getCurrentUsername());
        
        maintenanceRepository.save(existingMaintenance);
        auditLogService.logAction("Maintenance Deleted", "Maintenance", id, oldState, null, "Deleted maintenance log ID: " + id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void restoreMaintenance(Long id) {
        Maintenance existingMaintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance log not found with id: " + id));
        if (!existingMaintenance.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Maintenance log is already active and cannot be restored");
        }
        
        existingMaintenance.setDeleted(false);
        existingMaintenance.setDeletedAt(null);
        existingMaintenance.setDeletedBy(null);
        
        maintenanceRepository.save(existingMaintenance);
        auditLogService.logAction("Maintenance Restored", "Maintenance", id, null, existingMaintenance, "Restored maintenance log ID: " + id);
    }

    @Override
    public Page<Maintenance> searchAndFilterMaintenance(Long vehicleId, String garage,
            com.fleetops.entity.MaintenanceStatus status, Pageable pageable) {
        return maintenanceRepository.searchByDeletedFalse(vehicleId, garage, status, pageable);
    }

    @Override
    public Page<Maintenance> getDeletedMaintenance(Pageable pageable) {
        return maintenanceRepository.findAllByDeletedTrue(pageable);
    }
}
