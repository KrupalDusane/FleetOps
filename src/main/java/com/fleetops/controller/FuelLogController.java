package com.fleetops.controller;

import com.fleetops.entity.FuelLog;
import com.fleetops.service.FuelLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fuel-logs")
@Tag(name = "Fuel Logs Management", description = "Endpoints for tracking fleet fueling expenses")
public class FuelLogController {

    private final FuelLogService fuelLogService;

    public FuelLogController(FuelLogService fuelLogService) {
        this.fuelLogService = fuelLogService;
    }

    @PostMapping
    @Operation(summary = "Create a new fuel log", description = "Records a new fueling event and auto-calculates total cost")
    public ResponseEntity<FuelLog> createFuelLog(@Valid @RequestBody FuelLog fuelLog) {
        FuelLog savedLog = fuelLogService.createFuelLog(fuelLog);
        return new ResponseEntity<>(savedLog, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all fuel logs", description = "Retrieves a list of all fueling events")
    public ResponseEntity<List<FuelLog>> getAllFuelLogs() {
        List<FuelLog> logs = fuelLogService.getAllFuelLogs();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter fuel logs", description = "Filter by Vehicle ID, Date, Cost, and Quantity ranges with pagination and sorting")
    public ResponseEntity<Page<FuelLog>> searchFuelLogs(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fuelDate,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost,
            @RequestParam(required = false) Double minQty,
            @RequestParam(required = false) Double maxQty,
            @PageableDefault(size = 10, sort = "fuelDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        
        // Use advanced filters to satisfy the analytics criteria
        Page<FuelLog> logsPage = fuelLogService.searchWithAdvancedFilters(vehicleId, fuelDate, minCost, maxCost, minQty, maxQty, pageable);
        return new ResponseEntity<>(logsPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fuel log by ID", description = "Fetches details of a specific fueling event")
    public ResponseEntity<FuelLog> getFuelLogById(@PathVariable Long id) {
        FuelLog log = fuelLogService.getFuelLogById(id);
        return new ResponseEntity<>(log, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fuel log", description = "Updates an existing fueling event and recalculates total cost")
    public ResponseEntity<FuelLog> updateFuelLog(@PathVariable Long id, @Valid @RequestBody FuelLog fuelLogDetails) {
        FuelLog updatedLog = fuelLogService.updateFuelLog(id, fuelLogDetails);
        return new ResponseEntity<>(updatedLog, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete fuel log", description = "Removes a fueling event record from the system")
    public ResponseEntity<Void> deleteFuelLog(@PathVariable Long id) {
        fuelLogService.deleteFuelLog(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
