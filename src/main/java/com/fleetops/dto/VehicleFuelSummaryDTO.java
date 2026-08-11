package com.fleetops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFuelSummaryDTO {
    private String vehicleNumber;
    private BigDecimal totalFuelCost;
    private Double totalFuelQuantity;
    private Double averageFuelQuantity;
    private BigDecimal averageCostPerFill;
    private LocalDate lastFuelDate;
    private Long totalNumberOfFuelLogs;
    private Double fuelEfficiency; // km per liter
}
