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
public class TopFuelExpenseDTO {
    private String vehicleNumber;
    private LocalDate fuelDate;
    private Double fuelQuantity;
    private BigDecimal totalCost;
}
