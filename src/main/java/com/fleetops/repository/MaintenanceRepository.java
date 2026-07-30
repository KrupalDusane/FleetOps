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

    @Query("SELECT m FROM Maintenance m WHERE " +
           "(:vehicleId IS NULL OR m.vehicle.id = :vehicleId) AND " +
           "(cast(:garage as string) IS NULL OR LOWER(m.garage) LIKE LOWER(CONCAT('%', cast(:garage as string), '%'))) AND " +
           "(cast(:status as string) IS NULL OR m.status = :status)")
    Page<Maintenance> searchAndFilterMaintenance(
            @Param("vehicleId") Long vehicleId, 
            @Param("garage") String garage, 
            @Param("status") MaintenanceStatus status, 
            Pageable pageable);
}
