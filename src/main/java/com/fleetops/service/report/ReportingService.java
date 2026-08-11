package com.fleetops.service.report;

public interface ReportingService {
    byte[] generateReport(String reportType, String format, Object filterCriteria);
    String generateFilename(String reportType, String format);
}
