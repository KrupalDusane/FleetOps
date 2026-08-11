package com.fleetops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderDTO {
    
    // A synthetic unique ID composed of Type + Reference ID (e.g. "MAINTENANCE-12")
    private String id;
    
    private ReminderType type;
    private Long referenceId;
    
    private Long vehicleId;
    private String vehicleNumber;
    
    private String title;
    private String description;
    
    private ReminderPriority priority;
    
    private LocalDate dueDate;
}
