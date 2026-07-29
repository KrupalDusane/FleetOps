package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new com.fleetops.exception.ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    @Override
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle existingVehicle = getVehicleById(id);

        existingVehicle.setVehicleNumber(vehicleDetails.getVehicleNumber());
        existingVehicle.setBrand(vehicleDetails.getBrand());
        existingVehicle.setModel(vehicleDetails.getModel());
        existingVehicle.setManufacturingYear(vehicleDetails.getManufacturingYear());
        existingVehicle.setFuelType(vehicleDetails.getFuelType());
        existingVehicle.setCurrentOdometer(vehicleDetails.getCurrentOdometer());
        existingVehicle.setStatus(vehicleDetails.getStatus());

        return vehicleRepository.save(existingVehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle existingVehicle = getVehicleById(id);
        vehicleRepository.delete(existingVehicle);
    }

    @Override
    public org.springframework.data.domain.Page<Vehicle> searchAndFilterVehicles(String search, com.fleetops.entity.VehicleStatus status, org.springframework.data.domain.Pageable pageable) {
        return vehicleRepository.searchAndFilterVehicles(search, status, pageable);
    }
}
