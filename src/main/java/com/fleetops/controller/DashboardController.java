package com.fleetops.controller;

import com.fleetops.dto.DashboardStats;
import com.fleetops.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard Statistics", description = "Endpoints for fetching dashboard statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieves live aggregates and counts for the dashboard")
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @GetMapping("/recent-vehicles")
    @Operation(summary = "Get recent vehicles", description = "Retrieves the latest 5 vehicles")
    public ResponseEntity<java.util.List<com.fleetops.entity.Vehicle>> getRecentVehicles() {
        java.util.List<com.fleetops.entity.Vehicle> vehicles = dashboardService.getRecentVehicles();
        return new ResponseEntity<>(vehicles, HttpStatus.OK);
    }
}
