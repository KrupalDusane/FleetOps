package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.entity.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DriverService {

    Driver createDriver(Driver driver);

    List<Driver> getAllDrivers();

    Driver getDriverById(Long id);

    Driver updateDriver(Long id, Driver driver);

    void deleteDriver(Long id);
    void restoreDriver(Long id);

    org.springframework.data.domain.Page<Driver> searchAndFilterDrivers(String search, com.fleetops.entity.DriverStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Driver> getDeletedDrivers(org.springframework.data.domain.Pageable pageable);
}
