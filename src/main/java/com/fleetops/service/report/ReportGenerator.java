package com.fleetops.service.report;

public interface ReportGenerator {
    byte[] generatePdf(Object filterCriteria);
    byte[] generateExcel(Object filterCriteria);
    byte[] generateCsv(Object filterCriteria);
    boolean supports(String reportType);
}
