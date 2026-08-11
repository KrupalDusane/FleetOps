package com.fleetops.controller;

import com.fleetops.dto.report.ReportFilterDTO;
import com.fleetops.service.AuditLogService;
import com.fleetops.service.report.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Enterprise Reports & Export Center", description = "Endpoints for generating and downloading reports")
public class ReportingController {

    private final ReportingService reportingService;
    private final AuditLogService auditLogService;

    public ReportingController(ReportingService reportingService, AuditLogService auditLogService) {
        this.reportingService = reportingService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export Report", description = "Generates and downloads a report based on type and filters")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String reportType,
            @RequestParam String format,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost) {

        ReportFilterDTO filters = new ReportFilterDTO();
        filters.setReportType(reportType);
        filters.setFormat(format);
        filters.setVehicleId(vehicleId);
        filters.setDriverId(driverId);
        filters.setStatus(status);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setMinCost(minCost);
        filters.setMaxCost(maxCost);

        byte[] reportData = reportingService.generateReport(reportType, format, filters);
        String filename = reportingService.generateFilename(reportType, format);

        auditLogService.logAction("Report Generated", reportType, null, null, null, "Exported as " + format.toUpperCase());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        
        switch (format.toLowerCase()) {
            case "pdf":
                headers.setContentType(MediaType.APPLICATION_PDF);
                break;
            case "excel":
            case "xlsx":
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                break;
            case "csv":
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                break;
            default:
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
    }
}
