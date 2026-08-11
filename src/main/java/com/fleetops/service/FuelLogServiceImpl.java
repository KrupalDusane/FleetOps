package com.fleetops.service;

import com.fleetops.entity.FuelLog;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.ResourceNotFoundException;
import com.fleetops.repository.FuelLogRepository;
import com.fleetops.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FuelLogServiceImpl implements FuelLogService {

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public FuelLogServiceImpl(FuelLogRepository fuelLogRepository, VehicleRepository vehicleRepository, AuditLogService auditLogService) {
        this.fuelLogRepository = fuelLogRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditLogService = auditLogService;
    }



    private void calculateTotalCost(FuelLog fuelLog) {
        if (fuelLog.getFuelQuantity() != null && fuelLog.getPricePerLitre() != null) {
            fuelLog.setTotalCost(fuelLog.getFuelQuantity() * fuelLog.getPricePerLitre());
        }
    }

    private void validateAndSetVehicle(FuelLog fuelLog) {
        if (fuelLog.getFuelQuantity() == null || fuelLog.getFuelQuantity() <= 0) {
            throw new com.fleetops.exception.InvalidOperationException("Fuel quantity must be greater than zero.");
        }
        if (fuelLog.getPricePerLitre() == null || fuelLog.getPricePerLitre() <= 0) {
            throw new com.fleetops.exception.InvalidOperationException("Price per litre must be greater than zero.");
        }

        if (fuelLog.getVehicle() != null && fuelLog.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(fuelLog.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active Vehicle not found with id: " + fuelLog.getVehicle().getId()));
            
            // Check odometer against latest fuel log
            fuelLogRepository.findFirstByVehicleIdAndDeletedFalseOrderByOdometerAtFuelingDesc(vehicle.getId())
                .ifPresent(latestLog -> {
                    if (fuelLog.getId() == null || !fuelLog.getId().equals(latestLog.getId())) {
                        if (fuelLog.getOdometerAtFueling() < latestLog.getOdometerAtFueling()) {
                            throw new com.fleetops.exception.InvalidOperationException("Odometer reading cannot be less than the previous entry (" + latestLog.getOdometerAtFueling() + ").");
                        }
                    }
                });
                
            fuelLog.setVehicle(vehicle);
        } else {
            throw new com.fleetops.exception.InvalidOperationException("Fuel log must be associated with a valid vehicle.");
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public FuelLog createFuelLog(FuelLog fuelLog) {
        fuelLog.setId(null);
        validateAndSetVehicle(fuelLog);
        calculateTotalCost(fuelLog);
        FuelLog saved = fuelLogRepository.save(fuelLog);
        auditLogService.logAction("Fuel Created", "FuelLog", saved.getId(), null, saved, "Created fuel log for vehicle ID: " + saved.getVehicle().getId());
        return saved;
    }

    @Override
    public List<FuelLog> getAllFuelLogs() {
        return fuelLogRepository.findAllByDeletedFalse();
    }

    @Override
    public FuelLog getFuelLogById(Long id) {
        return fuelLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel Log not found with id: " + id));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public FuelLog updateFuelLog(Long id, FuelLog fuelLogDetails) {
        FuelLog existingLog = getFuelLogById(id);
        
        FuelLog oldState = FuelLog.builder()
            .id(existingLog.getId())
            .fuelDate(existingLog.getFuelDate())
            .fuelQuantity(existingLog.getFuelQuantity())
            .pricePerLitre(existingLog.getPricePerLitre())
            .totalCost(existingLog.getTotalCost())
            .odometerAtFueling(existingLog.getOdometerAtFueling())
            .vehicle(existingLog.getVehicle())
            .build();

        existingLog.setFuelDate(fuelLogDetails.getFuelDate());
        existingLog.setFuelQuantity(fuelLogDetails.getFuelQuantity());
        existingLog.setPricePerLitre(fuelLogDetails.getPricePerLitre());
        existingLog.setOdometerAtFueling(fuelLogDetails.getOdometerAtFueling());

        // Validate vehicle and other rules
        if (fuelLogDetails.getVehicle() != null && fuelLogDetails.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(fuelLogDetails.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active Vehicle not found with id: " + fuelLogDetails.getVehicle().getId()));
            existingLog.setVehicle(vehicle);
        }

        validateAndSetVehicle(existingLog);
        calculateTotalCost(existingLog);

        FuelLog saved = fuelLogRepository.save(existingLog);
        auditLogService.logAction("Fuel Updated", "FuelLog", saved.getId(), oldState, saved, "Updated fuel log ID: " + saved.getId());
        return saved;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteFuelLog(Long id) {
        FuelLog existingLog = getFuelLogById(id);
        if (existingLog.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Fuel log is already deleted");
        }
        
        FuelLog oldState = FuelLog.builder().id(existingLog.getId()).fuelDate(existingLog.getFuelDate()).vehicle(existingLog.getVehicle()).build();

        existingLog.setDeleted(true);
        existingLog.setDeletedAt(java.time.LocalDateTime.now());
        existingLog.setDeletedBy(com.fleetops.util.SecurityUtils.getCurrentUsername());
        
        fuelLogRepository.save(existingLog);
        auditLogService.logAction("Fuel Deleted", "FuelLog", id, oldState, null, "Deleted fuel log ID: " + id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void restoreFuelLog(Long id) {
        FuelLog existingLog = fuelLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel Log not found with id: " + id));
        if (!existingLog.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Fuel log is already active and cannot be restored");
        }
        
        existingLog.setDeleted(false);
        existingLog.setDeletedAt(null);
        existingLog.setDeletedBy(null);
        
        fuelLogRepository.save(existingLog);
        auditLogService.logAction("Fuel Restored", "FuelLog", id, null, existingLog, "Restored fuel log ID: " + id);
    }

    @Override
    public Page<FuelLog> searchAndFilterFuelLogs(Long vehicleId, LocalDate fuelDate, Pageable pageable) {
        return fuelLogRepository.searchByDeletedFalse(vehicleId, fuelDate, pageable);
    }

    @Override
    public Page<FuelLog> searchWithAdvancedFilters(Long vehicleId, LocalDate fuelDate, Double minCost, Double maxCost, Double minQty, Double maxQty, Pageable pageable) {
        return fuelLogRepository.searchWithAdvancedFilters(vehicleId, fuelDate, minCost, maxCost, minQty, maxQty, pageable);
    }

    @Override
    public Page<FuelLog> getDeletedFuelLogs(Pageable pageable) {
        return fuelLogRepository.findAllByDeletedTrue(pageable);
    }
}
