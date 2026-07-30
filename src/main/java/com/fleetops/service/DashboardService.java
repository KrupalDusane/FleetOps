package com.fleetops.service;

import com.fleetops.dto.DashboardStats;

public interface DashboardService {
    DashboardStats getDashboardStats();
    java.util.List<com.fleetops.entity.Vehicle> getRecentVehicles();
}
