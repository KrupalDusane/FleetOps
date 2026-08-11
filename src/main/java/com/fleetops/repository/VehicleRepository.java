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

    @Query("SELECT v FROM Vehicle v WHERE v.deleted = false AND " +
           "(cast(:search as string) IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', cast(:search as string), '%'))) AND " +
           "(cast(:status as string) IS NULL OR v.status = :status)")
    Page<Vehicle> searchByDeletedFalse(@Param("search") String search, @Param("status") VehicleStatus status, Pageable pageable);

    @Query("SELECT v.status, COUNT(v) FROM Vehicle v WHERE v.deleted = false GROUP BY v.status")
    java.util.List<Object[]> getVehicleStatusCounts();
    
    long countByDeletedFalse();

    java.util.List<Vehicle> findTop5ByDeletedFalseOrderByIdDesc();

    java.util.Optional<Vehicle> findByIdAndDeletedFalse(Long id);
    
    boolean existsByVehicleNumberAndDeletedFalse(String vehicleNumber);

    boolean existsByVehicleNumberAndIdNotAndDeletedFalse(String vehicleNumber, Long id);

    java.util.List<Vehicle> findAllByDeletedFalse();
    
    Page<Vehicle> findAllByDeletedTrue(Pageable pageable);
}
