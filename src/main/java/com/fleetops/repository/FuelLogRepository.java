package com.fleetops.repository;

import com.fleetops.entity.FuelLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {

    @Query("SELECT f FROM FuelLog f WHERE " +
           "(:vehicleId IS NULL OR f.vehicle.id = :vehicleId) AND " +
           "(:fuelDate IS NULL OR f.fuelDate = :fuelDate)")
    Page<FuelLog> searchAndFilterFuelLogs(@Param("vehicleId") Long vehicleId, @Param("fuelDate") LocalDate fuelDate, Pageable pageable);
}
