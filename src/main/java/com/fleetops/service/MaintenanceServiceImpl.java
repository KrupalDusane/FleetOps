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

    public MaintenanceServiceImpl(MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    private void validateDates(Maintenance maintenance) {
        if (maintenance.getServiceDate() != null && maintenance.getNextServiceDate() != null) {
            if (maintenance.getNextServiceDate().isBefore(maintenance.getServiceDate())) {
                throw new IllegalArgumentException("Next service date cannot be before the actual service date.");
            }
        }
    }

    private void validateAndSetVehicle(Maintenance maintenance) {
        if (maintenance.getVehicle() != null && maintenance.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(maintenance.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + maintenance.getVehicle().getId()));
            maintenance.setVehicle(vehicle);
        } else {
            throw new IllegalArgumentException("Maintenance log must be associated with a valid vehicle.");
        }
    }

    @Override
    public Maintenance createMaintenance(Maintenance maintenance) {
        validateAndSetVehicle(maintenance);
        validateDates(maintenance);
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public List<Maintenance> getAllMaintenanceLogs() {
        return maintenanceRepository.findAll();
    }

    @Override
    public Maintenance getMaintenanceById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance log not found with id: " + id));
    }

    @Override
    public Maintenance updateMaintenance(Long id, Maintenance maintenanceDetails) {
        Maintenance existingMaintenance = getMaintenanceById(id);

        existingMaintenance.setGarage(maintenanceDetails.getGarage());
        existingMaintenance.setCost(maintenanceDetails.getCost());
        existingMaintenance.setServiceDate(maintenanceDetails.getServiceDate());
        existingMaintenance.setNextServiceDate(maintenanceDetails.getNextServiceDate());
        existingMaintenance.setStatus(maintenanceDetails.getStatus());

        if (maintenanceDetails.getVehicle() != null && maintenanceDetails.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(maintenanceDetails.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + maintenanceDetails.getVehicle().getId()));
            existingMaintenance.setVehicle(vehicle);
        }

        validateDates(existingMaintenance);
        return maintenanceRepository.save(existingMaintenance);
    }

    @Override
    public void deleteMaintenance(Long id) {
        Maintenance existingMaintenance = getMaintenanceById(id);
        maintenanceRepository.delete(existingMaintenance);
    }

    @Override
    public Page<Maintenance> searchAndFilterMaintenance(Long vehicleId, String garage, com.fleetops.entity.MaintenanceStatus status, Pageable pageable) {
        return maintenanceRepository.searchAndFilterMaintenance(vehicleId, garage, status, pageable);
    }
}
