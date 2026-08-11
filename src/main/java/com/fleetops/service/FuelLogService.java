package com.fleetops.service;

import com.fleetops.entity.FuelLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface FuelLogService {

    FuelLog createFuelLog(FuelLog fuelLog);

    List<FuelLog> getAllFuelLogs();

    FuelLog getFuelLogById(Long id);

    FuelLog updateFuelLog(Long id, FuelLog fuelLog);

    void deleteFuelLog(Long id);
    void restoreFuelLog(Long id);

    Page<FuelLog> searchAndFilterFuelLogs(Long vehicleId, LocalDate fuelDate, Pageable pageable);
    
    Page<FuelLog> searchWithAdvancedFilters(Long vehicleId, LocalDate fuelDate, Double minCost, Double maxCost, Double minQty, Double maxQty, Pageable pageable);
    
    Page<FuelLog> getDeletedFuelLogs(Pageable pageable);
}
