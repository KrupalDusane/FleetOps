package com.fleetops.controller;

import com.fleetops.entity.Driver;
import com.fleetops.entity.FuelLog;
import com.fleetops.entity.Maintenance;
import com.fleetops.entity.Vehicle;
import com.fleetops.service.DriverService;
import com.fleetops.service.FuelLogService;
import com.fleetops.service.MaintenanceService;
import com.fleetops.service.VehicleDocumentService;
import com.fleetops.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDeletedRecordsController {

    private final VehicleService vehicleService;
    private final DriverService driverService;
    private final FuelLogService fuelLogService;
    private final MaintenanceService maintenanceService;
    private final VehicleDocumentService documentService;

    public AdminDeletedRecordsController(VehicleService vehicleService,
                                         DriverService driverService,
                                         FuelLogService fuelLogService,
                                         MaintenanceService maintenanceService,
                                         VehicleDocumentService documentService) {
        this.vehicleService = vehicleService;
        this.driverService = driverService;
        this.fuelLogService = fuelLogService;
        this.maintenanceService = maintenanceService;
        this.documentService = documentService;
    }

    // --- GET Deleted Records ---
    
    @GetMapping("/deleted/vehicles")
    public ResponseEntity<Page<Vehicle>> getDeletedVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(vehicleService.getDeletedVehicles(pageable));
    }

    @GetMapping("/deleted/drivers")
    public ResponseEntity<Page<Driver>> getDeletedDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(driverService.getDeletedDrivers(pageable));
    }

    @GetMapping("/deleted/fuel-logs")
    public ResponseEntity<Page<FuelLog>> getDeletedFuelLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(fuelLogService.getDeletedFuelLogs(pageable));
    }

    @GetMapping("/deleted/maintenance")
    public ResponseEntity<Page<Maintenance>> getDeletedMaintenanceLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(maintenanceService.getDeletedMaintenance(pageable));
    }

    // --- Restore Endpoints ---

    @PostMapping("/restore/vehicle/{id}")
    public ResponseEntity<Void> restoreVehicle(@PathVariable Long id) {
        vehicleService.restoreVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/driver/{id}")
    public ResponseEntity<Void> restoreDriver(@PathVariable Long id) {
        driverService.restoreDriver(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/fuel-log/{id}")
    public ResponseEntity<Void> restoreFuelLog(@PathVariable Long id) {
        fuelLogService.restoreFuelLog(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/maintenance/{id}")
    @Operation(summary = "Restore deleted maintenance log", description = "Restores a soft-deleted maintenance log")
    public ResponseEntity<Void> restoreMaintenance(@PathVariable Long id) {
        maintenanceService.restoreMaintenance(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents")
    @Operation(summary = "Get all deleted documents", description = "Returns a paginated list of all soft-deleted vehicle documents")
    public ResponseEntity<Page<com.fleetops.entity.VehicleDocument>> getDeletedDocuments(
            @org.springframework.data.web.PageableDefault(size = 10, sort = "deletedAt") Pageable pageable) {
        Page<com.fleetops.entity.VehicleDocument> documents = documentService.getDeletedDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/restore/documents/{id}")
    @Operation(summary = "Restore deleted document", description = "Restores a soft-deleted vehicle document")
    public ResponseEntity<Void> restoreDocument(@PathVariable Long id) {
        documentService.restoreDocument(id);
        return ResponseEntity.ok().build();
    }
}
