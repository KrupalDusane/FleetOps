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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT f FROM FuelLog f WHERE f.deleted = false AND " +
           "(:vehicleId IS NULL OR f.vehicle.id = :vehicleId) AND " +
           "(cast(:fuelDate as date) IS NULL OR f.fuelDate = :fuelDate)")
    Page<FuelLog> searchByDeletedFalse(@Param("vehicleId") Long vehicleId, @Param("fuelDate") LocalDate fuelDate, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT f FROM FuelLog f WHERE f.deleted = false AND " +
           "(:vehicleId IS NULL OR f.vehicle.id = :vehicleId) AND " +
           "(cast(:fuelDate as date) IS NULL OR f.fuelDate = :fuelDate) AND " +
           "(:minCost IS NULL OR f.totalCost >= :minCost) AND " +
           "(:maxCost IS NULL OR f.totalCost <= :maxCost) AND " +
           "(:minQty IS NULL OR f.fuelQuantity >= :minQty) AND " +
           "(:maxQty IS NULL OR f.fuelQuantity <= :maxQty)")
    Page<FuelLog> searchWithAdvancedFilters(
            @Param("vehicleId") Long vehicleId, 
            @Param("fuelDate") LocalDate fuelDate,
            @Param("minCost") Double minCost,
            @Param("maxCost") Double maxCost,
            @Param("minQty") Double minQty,
            @Param("maxQty") Double maxQty,
            Pageable pageable);

    @Query("SELECT SUM(f.totalCost) FROM FuelLog f WHERE f.deleted = false AND MONTH(f.fuelDate) = MONTH(CURRENT_DATE) AND YEAR(f.fuelDate) = YEAR(CURRENT_DATE)")
    Double sumTotalCostForCurrentMonthAndDeletedFalse();

    @Query("SELECT COUNT(f) FROM FuelLog f WHERE f.deleted = false AND MONTH(f.fuelDate) = MONTH(CURRENT_DATE) AND YEAR(f.fuelDate) = YEAR(CURRENT_DATE)")
    long countFuelLogsForCurrentMonthAndDeletedFalse();

    @Query("SELECT MONTH(f.fuelDate), SUM(f.totalCost) FROM FuelLog f WHERE f.deleted = false AND YEAR(f.fuelDate) = YEAR(CURRENT_DATE) GROUP BY MONTH(f.fuelDate) ORDER BY MONTH(f.fuelDate)")
    java.util.List<Object[]> getMonthlyFuelExpenseCurrentYearAndDeletedFalse();

    long countByDeletedFalse();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    java.util.Optional<FuelLog> findByIdAndDeletedFalse(Long id);
    
    java.util.Optional<FuelLog> findFirstByVehicleIdAndDeletedFalseOrderByOdometerAtFuelingDesc(Long vehicleId);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    java.util.List<FuelLog> findAllByDeletedFalse();
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    Page<FuelLog> findAllByDeletedTrue(Pageable pageable);

    @Query("SELECT new com.fleetops.dto.MonthlyFuelExpenseDTO(MONTH(f.fuelDate), YEAR(f.fuelDate), CAST(SUM(f.totalCost) AS big_decimal)) " +
           "FROM FuelLog f WHERE f.deleted = false GROUP BY YEAR(f.fuelDate), MONTH(f.fuelDate) ORDER BY YEAR(f.fuelDate), MONTH(f.fuelDate)")
    java.util.List<com.fleetops.dto.MonthlyFuelExpenseDTO> getMonthlyFuelExpenses();

    @Query("SELECT new com.fleetops.dto.TopFuelExpenseDTO(f.vehicle.vehicleNumber, f.fuelDate, f.fuelQuantity, CAST(f.totalCost AS big_decimal)) " +
           "FROM FuelLog f WHERE f.deleted = false ORDER BY f.totalCost DESC")
    Page<com.fleetops.dto.TopFuelExpenseDTO> getTopFuelExpenses(Pageable pageable);

    @Query("SELECT new com.fleetops.dto.VehicleFuelSummaryDTO(" +
           "v.vehicleNumber, " +
           "CAST(SUM(f.totalCost) AS big_decimal), " +
           "SUM(f.fuelQuantity), " +
           "AVG(f.fuelQuantity), " +
           "CAST(AVG(f.totalCost) AS big_decimal), " +
           "MAX(f.fuelDate), " +
           "COUNT(f), " +
           "(MAX(f.odometerAtFueling) - MIN(f.odometerAtFueling)) / NULLIF(SUM(f.fuelQuantity), 0)) " +
           "FROM FuelLog f JOIN f.vehicle v WHERE f.deleted = false GROUP BY v.vehicleNumber")
    java.util.List<com.fleetops.dto.VehicleFuelSummaryDTO> getVehicleFuelSummaries();

    @Query("SELECT SUM(f.totalCost) FROM FuelLog f WHERE f.deleted = false")
    Double getTotalFuelCost();
    
    @Query("SELECT SUM(f.totalCost) FROM FuelLog f WHERE f.deleted = false AND YEAR(f.fuelDate) = YEAR(CURRENT_DATE)")
    Double getYearlyFuelCost();
}
