package com.fleetops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelAnalyticsDTO {
    private BigDecimal totalFuelCost;
    private BigDecimal monthlyFuelCost;
    private BigDecimal yearlyFuelCost;
    private BigDecimal averageFuelCostPerVehicle;
    private Double averageFuelQuantityPerVehicle;
    
    private String highestFuelConsumingVehicle;
    private String lowestFuelConsumingVehicle;
    
    private Double overallFuelEfficiency; // km per liter
    private BigDecimal monthOverMonthTrend; // percentage increase/decrease
}
