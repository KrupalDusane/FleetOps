package com.fleetops.repository;

import com.fleetops.entity.Driver;
import com.fleetops.entity.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"currentVehicle"})
    @Query("SELECT d FROM Driver d WHERE d.deleted = false AND " +
           "(cast(:search as string) IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', cast(:search as string), '%'))) AND " +
           "(cast(:status as string) IS NULL OR d.status = :status)")
    Page<Driver> searchByDeletedFalse(@Param("search") String search, @Param("status") DriverStatus status, Pageable pageable);

    long countByDeletedFalse();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"currentVehicle"})
    java.util.Optional<Driver> findByIdAndDeletedFalse(Long id);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"currentVehicle"})
    java.util.List<Driver> findAllByDeletedFalse();
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"currentVehicle"})
    Page<Driver> findAllByDeletedTrue(Pageable pageable);

    long countByCurrentVehicleIdAndDeletedFalse(Long vehicleId);

    boolean existsByLicenseNumberAndDeletedFalse(String licenseNumber);

    boolean existsByLicenseNumberAndIdNotAndDeletedFalse(String licenseNumber, Long id);
}
