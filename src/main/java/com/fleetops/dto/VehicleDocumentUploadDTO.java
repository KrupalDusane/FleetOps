package com.fleetops.dto;

import com.fleetops.entity.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class VehicleDocumentUploadDTO {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    private String documentNumber;

    private LocalDate issueDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotNull(message = "File is required")
    private MultipartFile file;
}
