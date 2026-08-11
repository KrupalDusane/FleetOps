package com.fleetops.service.report;

import com.fleetops.exception.InvalidOperationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final Map<String, ReportGenerator> generators;

    public ReportingServiceImpl(List<ReportGenerator> reportGenerators) {
        // Build a map of type -> generator. Assuming one generator per type for simplicity.
        // We'll match them dynamically later or hardcode the registration.
        this.generators = reportGenerators.stream()
            .collect(Collectors.toMap(
                g -> getSupportedType(g),
                g -> g
            ));
    }
    
    private String getSupportedType(ReportGenerator g) {
        if (g.supports("VEHICLE")) return "VEHICLE";
        if (g.supports("DRIVER")) return "DRIVER";
        if (g.supports("FUEL")) return "FUEL";
        if (g.supports("MAINTENANCE")) return "MAINTENANCE";
        if (g.supports("FLEET_HEALTH")) return "FLEET_HEALTH";
        if (g.supports("DASHBOARD_SUMMARY")) return "DASHBOARD_SUMMARY";
        return "UNKNOWN";
    }

    @Override
    public byte[] generateReport(String reportType, String format, Object filterCriteria) {
        ReportGenerator generator = generators.get(reportType.toUpperCase());
        if (generator == null) {
            throw new InvalidOperationException("No report generator found for type: " + reportType);
        }

        switch (format.toLowerCase()) {
            case "pdf":
                return generator.generatePdf(filterCriteria);
            case "excel":
            case "xlsx":
                return generator.generateExcel(filterCriteria);
            case "csv":
                return generator.generateCsv(filterCriteria);
            default:
                throw new InvalidOperationException("Unsupported export format: " + format);
        }
    }

    @Override
    public String generateFilename(String reportType, String format) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String ext = format.toLowerCase().equals("excel") ? "xlsx" : format.toLowerCase();
        return String.format("fleetops_%s_report_%s.%s", reportType.toLowerCase(), dateStr, ext);
    }
}
