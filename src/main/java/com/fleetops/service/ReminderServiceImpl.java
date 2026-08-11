package com.fleetops.service;

import com.fleetops.dto.ReminderDTO;
import com.fleetops.dto.ReminderPriority;
import com.fleetops.dto.ReminderSummaryDTO;
import com.fleetops.dto.ReminderType;
import com.fleetops.entity.Maintenance;
import com.fleetops.entity.VehicleDocument;
import com.fleetops.repository.MaintenanceRepository;
import com.fleetops.repository.VehicleDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final MaintenanceRepository maintenanceRepository;
    private final VehicleDocumentRepository documentRepository;

    public ReminderServiceImpl(MaintenanceRepository maintenanceRepository, VehicleDocumentRepository documentRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public Page<ReminderDTO> getReminders(Pageable pageable) {
        List<ReminderDTO> allReminders = generateAllReminders();
        
        // Sorting
        allReminders.sort(Comparator.comparing(ReminderDTO::getPriority).reversed()
                .thenComparing(ReminderDTO::getDueDate));

        // Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allReminders.size());
        
        List<ReminderDTO> pagedReminders = new ArrayList<>();
        if (start <= end) {
            pagedReminders = allReminders.subList(start, end);
        }

        return new PageImpl<>(pagedReminders, pageable, allReminders.size());
    }

    @Override
    public Page<ReminderDTO> getCriticalReminders(Pageable pageable) {
        List<ReminderDTO> criticalReminders = generateAllReminders().stream()
                .filter(r -> r.getPriority() == ReminderPriority.CRITICAL)
                .sorted(Comparator.comparing(ReminderDTO::getDueDate))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), criticalReminders.size());
        
        List<ReminderDTO> pagedReminders = new ArrayList<>();
        if (start <= end) {
            pagedReminders = criticalReminders.subList(start, end);
        }

        return new PageImpl<>(pagedReminders, pageable, criticalReminders.size());
    }

    @Override
    public ReminderSummaryDTO getReminderSummary() {
        List<ReminderDTO> allReminders = generateAllReminders();
        
        long critical = allReminders.stream().filter(r -> r.getPriority() == ReminderPriority.CRITICAL).count();
        long high = allReminders.stream().filter(r -> r.getPriority() == ReminderPriority.HIGH).count();
        long medium = allReminders.stream().filter(r -> r.getPriority() == ReminderPriority.MEDIUM).count();
        long low = allReminders.stream().filter(r -> r.getPriority() == ReminderPriority.LOW).count();
        
        long maintenanceCount = allReminders.stream().filter(r -> r.getType() == ReminderType.MAINTENANCE).count();
        long docCount = allReminders.stream().filter(r -> r.getType() == ReminderType.DOCUMENT).count();

        return ReminderSummaryDTO.builder()
                .totalActive(allReminders.size())
                .criticalCount(critical)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .maintenanceReminders(maintenanceCount)
                .documentReminders(docCount)
                .build();
    }

    private List<ReminderDTO> generateAllReminders() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);
        
        List<ReminderDTO> reminders = new ArrayList<>();
        
        // 1. Maintenance Reminders
        List<Maintenance> upcomingMaint = maintenanceRepository.findUpcomingOrOverdueMaintenance(threshold);
        for (Maintenance m : upcomingMaint) {
            ReminderDTO dto = ReminderDTO.builder()
                    .id("MAINTENANCE-" + m.getId())
                    .type(ReminderType.MAINTENANCE)
                    .referenceId(m.getId())
                    .vehicleId(m.getVehicle().getId())
                    .vehicleNumber(m.getVehicle().getVehicleNumber())
                    .dueDate(m.getNextServiceDate())
                    .build();
            
            long daysUntil = ChronoUnit.DAYS.between(today, m.getNextServiceDate());
            
            if (daysUntil < 0) {
                dto.setPriority(ReminderPriority.CRITICAL);
                dto.setTitle("Service Overdue");
                dto.setDescription("Service at " + m.getGarage() + " is overdue by " + Math.abs(daysUntil) + " days.");
            } else if (daysUntil <= 7) {
                dto.setPriority(ReminderPriority.HIGH);
                dto.setTitle("Service Due Soon");
                dto.setDescription("Service due at " + m.getGarage() + " in " + daysUntil + " days.");
            } else {
                dto.setPriority(ReminderPriority.MEDIUM);
                dto.setTitle("Upcoming Service");
                dto.setDescription("Service due at " + m.getGarage() + " in " + daysUntil + " days.");
            }
            reminders.add(dto);
        }
        
        // 2. Document Reminders
        List<VehicleDocument> expiringDocs = documentRepository.findExpiringOrExpiredDocuments(threshold);
        for (VehicleDocument d : expiringDocs) {
            ReminderDTO dto = ReminderDTO.builder()
                    .id("DOCUMENT-" + d.getId())
                    .type(ReminderType.DOCUMENT)
                    .referenceId(d.getId())
                    .vehicleId(d.getVehicle().getId())
                    .vehicleNumber(d.getVehicle().getVehicleNumber())
                    .dueDate(d.getExpiryDate())
                    .build();
            
            long daysUntil = ChronoUnit.DAYS.between(today, d.getExpiryDate());
            String docTypeName = d.getDocumentType().name().replace("_", " ");
            
            if (daysUntil < 0) {
                dto.setPriority(ReminderPriority.CRITICAL);
                dto.setTitle(docTypeName + " Expired");
                dto.setDescription(docTypeName + " expired " + Math.abs(daysUntil) + " days ago.");
            } else if (daysUntil <= 7) {
                dto.setPriority(ReminderPriority.CRITICAL);
                dto.setTitle(docTypeName + " Expiring Very Soon");
                dto.setDescription(docTypeName + " expires in " + daysUntil + " days.");
            } else if (daysUntil <= 15) {
                dto.setPriority(ReminderPriority.HIGH);
                dto.setTitle(docTypeName + " Expiring Soon");
                dto.setDescription(docTypeName + " expires in " + daysUntil + " days.");
            } else {
                dto.setPriority(ReminderPriority.MEDIUM);
                dto.setTitle("Upcoming " + docTypeName + " Expiry");
                dto.setDescription(docTypeName + " expires in " + daysUntil + " days.");
            }
            reminders.add(dto);
        }
        
        return reminders;
    }
}
