package com.fleetops.repository;

import com.fleetops.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fleetops.entity.VehicleStatus;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:search IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR v.status = :status)")
    Page<Vehicle> searchAndFilterVehicles(@Param("search") String search, @Param("status") VehicleStatus status, Pageable pageable);

    long countByStatus(VehicleStatus status);

    java.util.List<Vehicle> findTop5ByOrderByIdDesc();
}
