package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.entity.DriverStatus;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.ResourceNotFoundException;
import com.fleetops.repository.DriverRepository;
import com.fleetops.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public DriverServiceImpl(DriverRepository driverRepository, VehicleRepository vehicleRepository, AuditLogService auditLogService) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditLogService = auditLogService;
    }



    @Override
    @org.springframework.transaction.annotation.Transactional
    public Driver createDriver(Driver driver) {
        if (driverRepository.existsByLicenseNumberAndDeletedFalse(driver.getLicenseNumber())) {
            throw new com.fleetops.exception.InvalidOperationException("License number must be unique.");
        }
        if (driver.getPhone() != null && !driver.getPhone().matches("^\\+?[0-9]{10,15}$")) {
            throw new com.fleetops.exception.InvalidOperationException("Invalid phone number format.");
        }

        driver.setId(null);
        if (driver.getCurrentVehicle() != null && driver.getCurrentVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(driver.getCurrentVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active Vehicle not found with id: " + driver.getCurrentVehicle().getId()));
            
            long activeDrivers = driverRepository.countByCurrentVehicleIdAndDeletedFalse(vehicle.getId());
            if (activeDrivers >= 2) {
                throw new com.fleetops.exception.InvalidOperationException("A maximum of two drivers can be assigned to a single vehicle.");
            }
            
            driver.setCurrentVehicle(vehicle);
        }
        Driver saved = driverRepository.save(driver);
        auditLogService.logAction("Driver Created", "Driver", saved.getId(), null, saved, "Created new driver: " + saved.getName());
        return saved;
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAllByDeletedFalse();
    }

    @Override
    public Driver getDriverById(Long id) {
        return driverRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Driver updateDriver(Long id, Driver driverDetails) {
        if (driverRepository.existsByLicenseNumberAndIdNotAndDeletedFalse(driverDetails.getLicenseNumber(), id)) {
            throw new com.fleetops.exception.InvalidOperationException("License number must be unique.");
        }
        if (driverDetails.getPhone() != null && !driverDetails.getPhone().matches("^\\+?[0-9]{10,15}$")) {
            throw new com.fleetops.exception.InvalidOperationException("Invalid phone number format.");
        }

        Driver existingDriver = getDriverById(id);
        
        Driver oldState = Driver.builder()
            .id(existingDriver.getId())
            .name(existingDriver.getName())
            .licenseNumber(existingDriver.getLicenseNumber())
            .phone(existingDriver.getPhone())
            .status(existingDriver.getStatus())
            .currentVehicle(existingDriver.getCurrentVehicle())
            .build();

        existingDriver.setName(driverDetails.getName());
        existingDriver.setLicenseNumber(driverDetails.getLicenseNumber());
        existingDriver.setPhone(driverDetails.getPhone());
        existingDriver.setStatus(driverDetails.getStatus());

        if (driverDetails.getCurrentVehicle() != null && driverDetails.getCurrentVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(driverDetails.getCurrentVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active Vehicle not found with id: " + driverDetails.getCurrentVehicle().getId()));
            
            if (oldState.getCurrentVehicle() == null || !oldState.getCurrentVehicle().getId().equals(vehicle.getId())) {
                long activeDrivers = driverRepository.countByCurrentVehicleIdAndDeletedFalse(vehicle.getId());
                if (activeDrivers >= 2) {
                    throw new com.fleetops.exception.InvalidOperationException("A maximum of two drivers can be assigned to a single vehicle.");
                }
            }
            
            existingDriver.setCurrentVehicle(vehicle);
        } else {
            existingDriver.setCurrentVehicle(null);
        }

        Driver saved = driverRepository.save(existingDriver);
        
        boolean vehicleChanged = (oldState.getCurrentVehicle() == null && saved.getCurrentVehicle() != null) || 
                                 (oldState.getCurrentVehicle() != null && saved.getCurrentVehicle() == null) ||
                                 (oldState.getCurrentVehicle() != null && saved.getCurrentVehicle() != null && !oldState.getCurrentVehicle().getId().equals(saved.getCurrentVehicle().getId()));
                                 
        String action = vehicleChanged ? "Driver Assigned" : "Driver Updated";
        auditLogService.logAction(action, "Driver", saved.getId(), oldState, saved, "Updated driver: " + saved.getName());
        
        return saved;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteDriver(Long id) {
        Driver existingDriver = getDriverById(id);
        if (existingDriver.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Driver is already deleted");
        }
        
        Driver oldState = Driver.builder().id(existingDriver.getId()).name(existingDriver.getName()).build();

        existingDriver.setDeleted(true);
        existingDriver.setDeletedAt(java.time.LocalDateTime.now());
        existingDriver.setDeletedBy(com.fleetops.util.SecurityUtils.getCurrentUsername());
        
        driverRepository.save(existingDriver);
        auditLogService.logAction("Driver Deleted", "Driver", id, oldState, null, "Deleted driver: " + existingDriver.getName());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void restoreDriver(Long id) {
        Driver existingDriver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        if (!existingDriver.isDeleted()) {
            throw new com.fleetops.exception.InvalidOperationException("Driver is already active and cannot be restored");
        }
        
        existingDriver.setDeleted(false);
        existingDriver.setDeletedAt(null);
        existingDriver.setDeletedBy(null);
        
        driverRepository.save(existingDriver);
        auditLogService.logAction("Driver Restored", "Driver", id, null, existingDriver, "Restored driver: " + existingDriver.getName());
    }

    @Override
    public Page<Driver> searchAndFilterDrivers(String search, DriverStatus status, Pageable pageable) {
        return driverRepository.searchByDeletedFalse(search, status, pageable);
    }

    @Override
    public Page<Driver> getDeletedDrivers(Pageable pageable) {
        return driverRepository.findAllByDeletedTrue(pageable);
    }
}
