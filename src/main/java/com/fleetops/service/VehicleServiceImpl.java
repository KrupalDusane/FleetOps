package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, AuditLogService auditLogService) {
        this.vehicleRepository = vehicleRepository;
        this.auditLogService = auditLogService;
    }



    @Override
    @org.springframework.transaction.annotation.Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByVehicleNumberAndDeletedFalse(vehicle.getVehicleNumber())) {
            throw new com.fleetops.exception.InvalidOperationException("Vehicle number must be unique.");
        }
        if (vehicle.getManufacturingYear() < 1900 || vehicle.getManufacturingYear() > java.time.Year.now().getValue()) {
            throw new com.fleetops.exception.InvalidOperationException("Invalid manufacturing year.");
        }
        if (vehicle.getCurrentOdometer() < 0) {
            throw new com.fleetops.exception.InvalidOperationException("Odometer reading cannot be negative.");
        }
        
        vehicle.setId(null);
        Vehicle saved = vehicleRepository.save(vehicle);
        auditLogService.logAction("Vehicle Created", "Vehicle", saved.getId(), null, saved, "Created new vehicle: " + saved.getVehicleNumber());
        return saved;
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAllByDeletedFalse();
    }

    @Override
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new com.fleetops.exception.ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        if (vehicleRepository.existsByVehicleNumberAndIdNotAndDeletedFalse(vehicleDetails.getVehicleNumber(), id)) {
            throw new com.fleetops.exception.InvalidOperationException("Vehicle number must be unique.");
        }
        if (vehicleDetails.getManufacturingYear() < 1900 || vehicleDetails.getManufacturingYear() > java.time.Year.now().getValue()) {
            throw new com.fleetops.exception.InvalidOperationException("Invalid manufacturing year.");
        }
        if (vehicleDetails.getCurrentOdometer() < 0) {
            throw new com.fleetops.exception.InvalidOperationException("Odometer reading cannot be negative.");
        }

        Vehicle existingVehicle = getVehicleById(id);
        
        // Deep copy old state for audit
        Vehicle oldState = Vehicle.builder()
            .id(existingVehicle.getId())
            .vehicleNumber(existingVehicle.getVehicleNumber())
            .brand(existingVehicle.getBrand())
            .model(existingVehicle.getModel())
            .manufacturingYear(existingVehicle.getManufacturingYear())
            .fuelType(existingVehicle.getFuelType())
            .currentOdometer(existingVehicle.getCurrentOdometer())
            .status(existingVehicle.getStatus())
            .build();

        existingVehicle.setVehicleNumber(vehicleDetails.getVehicleNumber());
        existingVehicle.setBrand(vehicleDetails.getBrand());
        existingVehicle.setModel(vehicleDetails.getModel());
        existingVehicle.setManufacturingYear(vehicleDetails.getManufacturingYear());
        existingVehicle.setFuelType(vehicleDetails.getFuelType());
        existingVehicle.setCurrentOdometer(vehicleDetails.getCurrentOdometer());
        existingVehicle.setStatus(vehicleDetails.getStatus());

        Vehicle saved = vehicleRepository.save(existingVehicle);
        
        String action = oldState.getStatus() != saved.getStatus() ? "Vehicle Status Changed" : "Vehicle Updated";
        auditLogService.logAction(action, "Vehicle", saved.getId(), oldState, saved, "Updated vehicle: " + saved.getVehicleNumber());
        
        return saved;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteVehicle(Long id) {
        Vehicle existingVehicle = getVehicleById(id);
        if (existingVehicle.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Vehicle is already deleted");
        }
        
        Vehicle oldState = Vehicle.builder().id(existingVehicle.getId()).vehicleNumber(existingVehicle.getVehicleNumber()).build();

        existingVehicle.setDeleted(true);
        existingVehicle.setDeletedAt(java.time.LocalDateTime.now());
        existingVehicle.setDeletedBy(com.fleetops.util.SecurityUtils.getCurrentUsername());
        
        vehicleRepository.save(existingVehicle);
        auditLogService.logAction("Vehicle Deleted", "Vehicle", id, oldState, null, "Deleted vehicle: " + existingVehicle.getVehicleNumber());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void restoreVehicle(Long id) {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new com.fleetops.exception.ResourceNotFoundException("Vehicle not found with id: " + id));
        if (!existingVehicle.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Vehicle is already active and cannot be restored");
        }
        
        existingVehicle.setDeleted(false);
        existingVehicle.setDeletedAt(null);
        existingVehicle.setDeletedBy(null);
        
        vehicleRepository.save(existingVehicle);
        auditLogService.logAction("Vehicle Restored", "Vehicle", id, null, existingVehicle, "Restored vehicle: " + existingVehicle.getVehicleNumber());
    }

    @Override
    public org.springframework.data.domain.Page<Vehicle> searchAndFilterVehicles(String search, com.fleetops.entity.VehicleStatus status, org.springframework.data.domain.Pageable pageable) {
        return vehicleRepository.searchByDeletedFalse(search, status, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Vehicle> getDeletedVehicles(org.springframework.data.domain.Pageable pageable) {
        return vehicleRepository.findAllByDeletedTrue(pageable);
    }
}
