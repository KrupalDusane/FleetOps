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

    @Query("SELECT d FROM Driver d WHERE " +
           "(:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<Driver> searchAndFilterDrivers(@Param("search") String search, @Param("status") DriverStatus status, Pageable pageable);
}
