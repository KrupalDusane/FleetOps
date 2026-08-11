package com.fleetops.service;

import com.fleetops.dto.FuelAnalyticsDTO;
import com.fleetops.dto.MonthlyFuelExpenseDTO;
import com.fleetops.dto.TopFuelExpenseDTO;
import com.fleetops.dto.VehicleFuelSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FuelAnalyticsService {
    
    FuelAnalyticsDTO getGlobalFuelAnalytics();
    
    List<VehicleFuelSummaryDTO> getVehicleFuelSummaries();
    
    List<MonthlyFuelExpenseDTO> getMonthlyFuelExpenses();
    
    Page<TopFuelExpenseDTO> getTopFuelExpenses(Pageable pageable);
}
