package com.fleetops.controller;

import com.fleetops.dto.FuelAnalyticsDTO;
import com.fleetops.dto.MonthlyFuelExpenseDTO;
import com.fleetops.dto.TopFuelExpenseDTO;
import com.fleetops.dto.VehicleFuelSummaryDTO;
import com.fleetops.service.FuelAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fuel/analytics")
@Tag(name = "Fuel Analytics", description = "Endpoints for fuel analytics and cost intelligence")
public class FuelAnalyticsController {

    private final FuelAnalyticsService fuelAnalyticsService;

    public FuelAnalyticsController(FuelAnalyticsService fuelAnalyticsService) {
        this.fuelAnalyticsService = fuelAnalyticsService;
    }

    @GetMapping("/global")
    @Operation(summary = "Get global fuel analytics", description = "Returns system-wide aggregated fuel analytics.")
    public ResponseEntity<FuelAnalyticsDTO> getGlobalAnalytics() {
        return ResponseEntity.ok(fuelAnalyticsService.getGlobalFuelAnalytics());
    }

    @GetMapping("/vehicle-summary")
    @Operation(summary = "Get vehicle fuel summaries", description = "Returns fuel summary grouped by each vehicle.")
    public ResponseEntity<List<VehicleFuelSummaryDTO>> getVehicleSummaries() {
        return ResponseEntity.ok(fuelAnalyticsService.getVehicleFuelSummaries());
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly fuel trends", description = "Returns aggregated fuel expenses grouped by month and year.")
    public ResponseEntity<List<MonthlyFuelExpenseDTO>> getMonthlyTrends() {
        return ResponseEntity.ok(fuelAnalyticsService.getMonthlyFuelExpenses());
    }

    @GetMapping("/top-expenses")
    @Operation(summary = "Get top fuel expenses", description = "Returns the highest single fuel log expenses paginated.")
    public ResponseEntity<Page<TopFuelExpenseDTO>> getTopExpenses(
            @PageableDefault(size = 5) Pageable pageable) {
        return ResponseEntity.ok(fuelAnalyticsService.getTopFuelExpenses(pageable));
    }
}
