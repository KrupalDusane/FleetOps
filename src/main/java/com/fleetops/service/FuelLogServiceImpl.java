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

    public FuelLogServiceImpl(FuelLogRepository fuelLogRepository, VehicleRepository vehicleRepository) {
        this.fuelLogRepository = fuelLogRepository;
        this.vehicleRepository = vehicleRepository;
    }

    private void calculateTotalCost(FuelLog fuelLog) {
        if (fuelLog.getFuelQuantity() != null && fuelLog.getPricePerLitre() != null) {
            fuelLog.setTotalCost(fuelLog.getFuelQuantity() * fuelLog.getPricePerLitre());
        }
    }

    private void validateAndSetVehicle(FuelLog fuelLog) {
        if (fuelLog.getVehicle() != null && fuelLog.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(fuelLog.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + fuelLog.getVehicle().getId()));
            fuelLog.setVehicle(vehicle);
        } else {
            throw new IllegalArgumentException("Fuel log must be associated with a valid vehicle.");
        }
    }

    @Override
    public FuelLog createFuelLog(FuelLog fuelLog) {
        fuelLog.setId(null);
        validateAndSetVehicle(fuelLog);
        calculateTotalCost(fuelLog);
        return fuelLogRepository.save(fuelLog);
    }

    @Override
    public List<FuelLog> getAllFuelLogs() {
        return fuelLogRepository.findAll();
    }

    @Override
    public FuelLog getFuelLogById(Long id) {
        return fuelLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel Log not found with id: " + id));
    }

    @Override
    public FuelLog updateFuelLog(Long id, FuelLog fuelLogDetails) {
        FuelLog existingLog = getFuelLogById(id);

        existingLog.setFuelDate(fuelLogDetails.getFuelDate());
        existingLog.setFuelQuantity(fuelLogDetails.getFuelQuantity());
        existingLog.setPricePerLitre(fuelLogDetails.getPricePerLitre());
        existingLog.setOdometerAtFueling(fuelLogDetails.getOdometerAtFueling());

        // Validate vehicle
        if (fuelLogDetails.getVehicle() != null && fuelLogDetails.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(fuelLogDetails.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + fuelLogDetails.getVehicle().getId()));
            existingLog.setVehicle(vehicle);
        }

        calculateTotalCost(existingLog);

        return fuelLogRepository.save(existingLog);
    }

    @Override
    public void deleteFuelLog(Long id) {
        FuelLog existingLog = getFuelLogById(id);
        fuelLogRepository.delete(existingLog);
    }

    @Override
    public Page<FuelLog> searchAndFilterFuelLogs(Long vehicleId, LocalDate fuelDate, Pageable pageable) {
        return fuelLogRepository.searchAndFilterFuelLogs(vehicleId, fuelDate, pageable);
    }
}
