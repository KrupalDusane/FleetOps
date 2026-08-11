package com.fleetops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderSummaryDTO {
    private long totalActive;
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    
    private long maintenanceReminders;
    private long documentReminders;
}
