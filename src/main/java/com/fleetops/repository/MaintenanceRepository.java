package com.fleetops.repository;

import com.fleetops.entity.Maintenance;
import com.fleetops.entity.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT m FROM Maintenance m WHERE m.deleted = false AND " +
           "(:vehicleId IS NULL OR m.vehicle.id = :vehicleId) AND " +
           "(cast(:garage as string) IS NULL OR LOWER(m.garage) LIKE LOWER(CONCAT('%', cast(:garage as string), '%'))) AND " +
           "(cast(:status as string) IS NULL OR m.status = :status)")
    Page<Maintenance> searchByDeletedFalse(
            @Param("vehicleId") Long vehicleId, 
            @Param("garage") String garage, 
            @Param("status") MaintenanceStatus status, 
            Pageable pageable);

    @Query("SELECT MONTH(m.serviceDate), COUNT(m) FROM Maintenance m WHERE m.deleted = false AND YEAR(m.serviceDate) = YEAR(CURRENT_DATE) GROUP BY MONTH(m.serviceDate) ORDER BY MONTH(m.serviceDate)")
    java.util.List<Object[]> getMonthlyMaintenanceCountCurrentYearAndDeletedFalse();

    long countByDeletedFalse();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    java.util.Optional<Maintenance> findByIdAndDeletedFalse(Long id);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    java.util.List<Maintenance> findAllByDeletedFalse();
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    Page<Maintenance> findAllByDeletedTrue(Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT m FROM Maintenance m WHERE m.deleted = false AND m.status != 'COMPLETED' AND m.nextServiceDate <= :thresholdDate")
    java.util.List<Maintenance> findUpcomingOrOverdueMaintenance(@Param("thresholdDate") java.time.LocalDate thresholdDate);
    
    @Query("SELECT COUNT(m) FROM Maintenance m WHERE m.deleted = false AND m.status != 'COMPLETED' AND m.nextServiceDate < CURRENT_DATE")
    long countOverdueMaintenance();
}
