package com.fleetops.service;

import com.fleetops.dto.ReminderDTO;
import com.fleetops.dto.ReminderSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReminderService {
    
    Page<ReminderDTO> getReminders(Pageable pageable);
    
    Page<ReminderDTO> getCriticalReminders(Pageable pageable);
    
    ReminderSummaryDTO getReminderSummary();
}
