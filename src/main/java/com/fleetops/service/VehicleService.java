package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import java.util.List;

public interface VehicleService {
    Vehicle createVehicle(Vehicle vehicle);
    List<Vehicle> getAllVehicles();
    Vehicle getVehicleById(Long id);
    Vehicle updateVehicle(Long id, Vehicle vehicle);
    void deleteVehicle(Long id);
    void restoreVehicle(Long id);
    
    org.springframework.data.domain.Page<Vehicle> searchAndFilterVehicles(String search, com.fleetops.entity.VehicleStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Vehicle> getDeletedVehicles(org.springframework.data.domain.Pageable pageable);
}
