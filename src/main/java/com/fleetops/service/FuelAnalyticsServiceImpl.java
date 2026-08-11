package com.fleetops.service;

import com.fleetops.dto.FuelAnalyticsDTO;
import com.fleetops.dto.MonthlyFuelExpenseDTO;
import com.fleetops.dto.TopFuelExpenseDTO;
import com.fleetops.dto.VehicleFuelSummaryDTO;
import com.fleetops.repository.FuelLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FuelAnalyticsServiceImpl implements FuelAnalyticsService {

    private final FuelLogRepository fuelLogRepository;

    public FuelAnalyticsServiceImpl(FuelLogRepository fuelLogRepository) {
        this.fuelLogRepository = fuelLogRepository;
    }

    @Override
    public FuelAnalyticsDTO getGlobalFuelAnalytics() {
        Double total = fuelLogRepository.getTotalFuelCost();
        Double yearly = fuelLogRepository.getYearlyFuelCost();
        Double monthly = fuelLogRepository.sumTotalCostForCurrentMonthAndDeletedFalse();
        
        BigDecimal totalFuelCost = total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
        BigDecimal yearlyFuelCost = yearly != null ? BigDecimal.valueOf(yearly) : BigDecimal.ZERO;
        BigDecimal monthlyFuelCost = monthly != null ? BigDecimal.valueOf(monthly) : BigDecimal.ZERO;

        List<VehicleFuelSummaryDTO> vehicleSummaries = fuelLogRepository.getVehicleFuelSummaries();
        
        BigDecimal avgCostPerVehicle = BigDecimal.ZERO;
        Double avgQtyPerVehicle = 0.0;
        String highestVehicle = "N/A";
        String lowestVehicle = "N/A";
        Double overallEfficiency = 0.0;

        if (!vehicleSummaries.isEmpty()) {
            avgCostPerVehicle = totalFuelCost.divide(BigDecimal.valueOf(vehicleSummaries.size()), 2, RoundingMode.HALF_UP);
            
            Double totalQty = vehicleSummaries.stream().mapToDouble(VehicleFuelSummaryDTO::getTotalFuelQuantity).sum();
            avgQtyPerVehicle = totalQty / vehicleSummaries.size();
            
            // Due to ordering by SUM(totalCost) in query? The query groups by vehicleNumber but isn't ordered.
            // Let's sort manually to find highest/lowest
            vehicleSummaries.sort((a, b) -> a.getTotalFuelCost().compareTo(b.getTotalFuelCost()));
            lowestVehicle = vehicleSummaries.get(0).getVehicleNumber();
            highestVehicle = vehicleSummaries.get(vehicleSummaries.size() - 1).getVehicleNumber();

            // Calculate overall efficiency by averaging non-null vehicle efficiencies
            double efficiencySum = 0;
            int efficiencyCount = 0;
            for (VehicleFuelSummaryDTO v : vehicleSummaries) {
                if (v.getFuelEfficiency() != null) {
                    efficiencySum += v.getFuelEfficiency();
                    efficiencyCount++;
                }
            }
            if (efficiencyCount > 0) {
                overallEfficiency = efficiencySum / efficiencyCount;
            }
        }

        // Calculate Month-over-Month trend
        List<MonthlyFuelExpenseDTO> monthlyExpenses = fuelLogRepository.getMonthlyFuelExpenses();
        BigDecimal momTrend = BigDecimal.ZERO;
        if (monthlyExpenses.size() >= 2) {
            BigDecimal currentMonth = monthlyExpenses.get(monthlyExpenses.size() - 1).getTotalExpense();
            BigDecimal previousMonth = monthlyExpenses.get(monthlyExpenses.size() - 2).getTotalExpense();
            
            if (previousMonth != null && previousMonth.compareTo(BigDecimal.ZERO) > 0) {
                momTrend = currentMonth.subtract(previousMonth)
                        .divide(previousMonth, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            } else if (currentMonth != null && currentMonth.compareTo(BigDecimal.ZERO) > 0) {
                momTrend = BigDecimal.valueOf(100); // from 0 to something is 100% increase logic
            }
        }

        return FuelAnalyticsDTO.builder()
                .totalFuelCost(totalFuelCost)
                .monthlyFuelCost(monthlyFuelCost)
                .yearlyFuelCost(yearlyFuelCost)
                .averageFuelCostPerVehicle(avgCostPerVehicle)
                .averageFuelQuantityPerVehicle(avgQtyPerVehicle)
                .highestFuelConsumingVehicle(highestVehicle)
                .lowestFuelConsumingVehicle(lowestVehicle)
                .overallFuelEfficiency(overallEfficiency)
                .monthOverMonthTrend(momTrend)
                .build();
    }

    @Override
    public List<VehicleFuelSummaryDTO> getVehicleFuelSummaries() {
        return fuelLogRepository.getVehicleFuelSummaries();
    }

    @Override
    public List<MonthlyFuelExpenseDTO> getMonthlyFuelExpenses() {
        return fuelLogRepository.getMonthlyFuelExpenses();
    }

    @Override
    public Page<TopFuelExpenseDTO> getTopFuelExpenses(Pageable pageable) {
        return fuelLogRepository.getTopFuelExpenses(pageable);
    }
}
