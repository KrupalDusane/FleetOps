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

    public DriverServiceImpl(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Driver createDriver(Driver driver) {
        driver.setId(null);
        if (driver.getCurrentVehicle() != null && driver.getCurrentVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(driver.getCurrentVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + driver.getCurrentVehicle().getId()));
            driver.setCurrentVehicle(vehicle);
        }
        return driverRepository.save(driver);
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Override
    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
    }

    @Override
    public Driver updateDriver(Long id, Driver driverDetails) {
        Driver existingDriver = getDriverById(id);

        existingDriver.setName(driverDetails.getName());
        existingDriver.setLicenseNumber(driverDetails.getLicenseNumber());
        existingDriver.setPhone(driverDetails.getPhone());
        existingDriver.setStatus(driverDetails.getStatus());

        if (driverDetails.getCurrentVehicle() != null && driverDetails.getCurrentVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(driverDetails.getCurrentVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + driverDetails.getCurrentVehicle().getId()));
            existingDriver.setCurrentVehicle(vehicle);
        } else {
            existingDriver.setCurrentVehicle(null);
        }

        return driverRepository.save(existingDriver);
    }

    @Override
    public void deleteDriver(Long id) {
        Driver existingDriver = getDriverById(id);
        driverRepository.delete(existingDriver);
    }

    @Override
    public Page<Driver> searchAndFilterDrivers(String search, DriverStatus status, Pageable pageable) {
        return driverRepository.searchAndFilterDrivers(search, status, pageable);
    }
}
