package com.fleetops.service;

import com.fleetops.dto.FleetHealthDTO;
import com.fleetops.dto.FuelAnalyticsDTO;

public interface FleetHealthService {
    FleetHealthDTO calculateHealth(
            long totalVehicles, 
            long available, 
            long maintenance, 
            long expiringDocs, 
            long expiredDocs,
            long overdueMaintenance,
            FuelAnalyticsDTO fuelAnalytics
    );
}
