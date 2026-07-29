package com.fleetops.controller;

import com.fleetops.entity.Maintenance;
import com.fleetops.entity.MaintenanceStatus;
import com.fleetops.service.MaintenanceService;
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
@RequestMapping("/api/maintenance")
@Tag(name = "Maintenance Management", description = "Endpoints for scheduling and tracking vehicle maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    @Operation(summary = "Create a maintenance record", description = "Logs a new maintenance event for a vehicle")
    public ResponseEntity<Maintenance> createMaintenance(@Valid @RequestBody Maintenance maintenance) {
        Maintenance savedLog = maintenanceService.createMaintenance(maintenance);
        return new ResponseEntity<>(savedLog, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all maintenance records", description = "Retrieves a list of all maintenance events")
    public ResponseEntity<List<Maintenance>> getAllMaintenanceLogs() {
        List<Maintenance> logs = maintenanceService.getAllMaintenanceLogs();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter maintenance records", description = "Filter by Vehicle, Garage, or Status with pagination")
    public ResponseEntity<Page<Maintenance>> searchMaintenanceLogs(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String garage,
            @RequestParam(required = false) MaintenanceStatus status,
            @PageableDefault(size = 10, sort = "serviceDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<Maintenance> logsPage = maintenanceService.searchAndFilterMaintenance(vehicleId, garage, status, pageable);
        return new ResponseEntity<>(logsPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance record by ID", description = "Fetches details of a specific maintenance event")
    public ResponseEntity<Maintenance> getMaintenanceById(@PathVariable Long id) {
        Maintenance log = maintenanceService.getMaintenanceById(id);
        return new ResponseEntity<>(log, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update maintenance record", description = "Updates an existing maintenance event")
    public ResponseEntity<Maintenance> updateMaintenance(@PathVariable Long id, @Valid @RequestBody Maintenance maintenanceDetails) {
        Maintenance updatedLog = maintenanceService.updateMaintenance(id, maintenanceDetails);
        return new ResponseEntity<>(updatedLog, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete maintenance record", description = "Removes a maintenance event from the system")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
