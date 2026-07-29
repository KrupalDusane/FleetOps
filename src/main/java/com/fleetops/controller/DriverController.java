package com.fleetops.controller;

import com.fleetops.entity.Driver;
import com.fleetops.entity.DriverStatus;
import com.fleetops.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Management", description = "Endpoints for managing drivers in the fleet")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @Operation(summary = "Create a new driver", description = "Adds a new driver to the system")
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody Driver driver) {
        Driver savedDriver = driverService.createDriver(driver);
        return new ResponseEntity<>(savedDriver, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all drivers", description = "Retrieves a list of all drivers")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter drivers", description = "Search drivers by name or license number, filter by status, with pagination and sorting.")
    public ResponseEntity<Page<Driver>> searchDrivers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DriverStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Driver> driversPage = driverService.searchAndFilterDrivers(search, status, pageable);
        return new ResponseEntity<>(driversPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a driver by ID", description = "Fetches details of a specific driver by their ID")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        Driver driver = driverService.getDriverById(id);
        return new ResponseEntity<>(driver, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a driver", description = "Updates an existing driver's information and vehicle assignment")
    public ResponseEntity<Driver> updateDriver(@PathVariable Long id, @Valid @RequestBody Driver driverDetails) {
        Driver updatedDriver = driverService.updateDriver(id, driverDetails);
        return new ResponseEntity<>(updatedDriver, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a driver", description = "Removes a driver from the system by their ID")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
