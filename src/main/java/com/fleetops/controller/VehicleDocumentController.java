package com.fleetops.controller;

import com.fleetops.dto.VehicleDocumentUploadDTO;
import com.fleetops.entity.DocumentType;
import com.fleetops.entity.VehicleDocument;
import com.fleetops.service.VehicleDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Vehicle Documents", description = "Endpoints for vehicle document management")
public class VehicleDocumentController {

    private final VehicleDocumentService documentService;

    public VehicleDocumentController(VehicleDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload a vehicle document", description = "Uploads a new document and archives existing ones of the same type.")
    public ResponseEntity<VehicleDocument> uploadDocument(@Valid @ModelAttribute VehicleDocumentUploadDTO uploadDTO) throws IOException, NoSuchAlgorithmException {
        VehicleDocument document = documentService.uploadDocument(uploadDTO);
        return ResponseEntity.status(201).body(document);
    }

    @GetMapping
    @Operation(summary = "Search vehicle documents", description = "Search and filter active vehicle documents")
    public ResponseEntity<Page<VehicleDocument>> searchDocuments(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) String statusFilter,
            @PageableDefault(size = 10, sort = "uploadedAt") Pageable pageable) {
        Page<VehicleDocument> documents = documentService.searchDocuments(vehicleId, documentType, statusFilter, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document metadata by ID", description = "Returns metadata for a specific document.")
    public ResponseEntity<VehicleDocument> getDocumentById(@PathVariable Long id) {
        VehicleDocument document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document file", description = "Streams the actual file for a given document ID.")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws IOException {
        VehicleDocument document = documentService.getDocumentById(id);
        Resource resource = documentService.downloadDocument(id);

        String userFriendlyFilename = document.getVehicle().getVehicleNumber() + "_" + document.getDocumentType() + "_" + document.getOriginalFileName();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userFriendlyFilename + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a document", description = "Soft deletes a specific vehicle document.")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
