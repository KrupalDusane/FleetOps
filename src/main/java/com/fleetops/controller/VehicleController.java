package com.fleetops.controller;

import com.fleetops.entity.Vehicle;
import com.fleetops.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for managing vehicles in the fleet")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Create a new vehicle", description = "Adds a new vehicle to the database")
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        Vehicle savedVehicle = vehicleService.createVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Retrieves a list of all vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter vehicles", description = "Search vehicles by number, brand, or model, filter by status, with pagination and sorting.")
    public ResponseEntity<org.springframework.data.domain.Page<Vehicle>> searchVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.fleetops.entity.VehicleStatus status,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "id") org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Vehicle> vehiclesPage = vehicleService.searchAndFilterVehicles(search, status, pageable);
        return ResponseEntity.ok(vehiclesPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a vehicle by ID", description = "Fetches details of a specific vehicle by its ID")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a vehicle", description = "Updates an existing vehicle's information")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicleDetails) {
        Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicleDetails);
        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle", description = "Removes a vehicle from the system by its ID")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
