package com.fleetops.controller;

import com.fleetops.dto.ReminderDTO;
import com.fleetops.dto.ReminderSummaryDTO;
import com.fleetops.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
@Tag(name = "Maintenance Reminders", description = "Endpoints for dynamic maintenance and document reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    @Operation(summary = "Get all active reminders", description = "Returns a paginated list of all active dynamic reminders.")
    public ResponseEntity<Page<ReminderDTO>> getReminders(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReminderDTO> reminders = reminderService.getReminders(pageable);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/critical")
    @Operation(summary = "Get critical reminders", description = "Returns a paginated list of only critical reminders.")
    public ResponseEntity<Page<ReminderDTO>> getCriticalReminders(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReminderDTO> reminders = reminderService.getCriticalReminders(pageable);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get reminder summary", description = "Returns statistical summary of current active reminders.")
    public ResponseEntity<ReminderSummaryDTO> getReminderSummary() {
        ReminderSummaryDTO summary = reminderService.getReminderSummary();
        return ResponseEntity.ok(summary);
    }
}
