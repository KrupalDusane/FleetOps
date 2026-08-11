package com.fleetops.service;

import com.fleetops.dto.VehicleDocumentUploadDTO;
import com.fleetops.entity.DocumentType;
import com.fleetops.entity.Vehicle;
import com.fleetops.entity.VehicleDocument;
import com.fleetops.exception.InvalidOperationException;
import com.fleetops.exception.ResourceNotFoundException;
import com.fleetops.repository.VehicleDocumentRepository;
import com.fleetops.repository.VehicleRepository;
import com.fleetops.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VehicleDocumentServiceImpl implements VehicleDocumentService {

    private final VehicleDocumentRepository documentRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    @Value("${app.upload.dir:uploads/}")
    private String baseUploadDir;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf", "image/jpeg", "image/png"
    );
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "jpg", "jpeg", "png"
    );

    public VehicleDocumentServiceImpl(VehicleDocumentRepository documentRepository,
                                      VehicleRepository vehicleRepository,
                                      AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public VehicleDocument uploadDocument(VehicleDocumentUploadDTO uploadDTO) throws IOException, NoSuchAlgorithmException {
        // Validate dates
        if (uploadDTO.getIssueDate() != null && !uploadDTO.getIssueDate().isBefore(uploadDTO.getExpiryDate())) {
            throw new InvalidOperationException("Issue date must be before expiry date.");
        }

        MultipartFile file = uploadDTO.getFile();
        if (file.isEmpty()) {
            throw new InvalidOperationException("Cannot upload an empty file.");
        }

        // Validate MIME type
        String mimeType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new InvalidOperationException("Unsupported MIME type: " + mimeType);
        }

        // Validate Extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidOperationException("File must have an extension.");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidOperationException("Unsupported file extension: " + extension);
        }

        // Find Vehicle
        Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(uploadDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + uploadDTO.getVehicleId()));

        // Archive previous active document of same type
        Optional<VehicleDocument> existingActive = documentRepository.findByVehicleIdAndDocumentTypeAndArchivedFalseAndDeletedFalse(
                vehicle.getId(), uploadDTO.getDocumentType());
        if (existingActive.isPresent()) {
            VehicleDocument oldDoc = existingActive.get();
            oldDoc.setArchived(true);
            documentRepository.save(oldDoc);
            auditLogService.logAction("Document Archived", "VehicleDocument", oldDoc.getId(), null, null, "Archived old document type: " + oldDoc.getDocumentType());
        }

        // Generate Checksum
        String checksum = generateChecksum(file.getInputStream());

        // Create directory uploads/vehicles/{vehicleId}/{documentType}
        String relativePath = "vehicles/" + vehicle.getId() + "/" + uploadDTO.getDocumentType().name().toLowerCase();
        Path targetLocation = Paths.get(baseUploadDir).resolve(relativePath).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);

        // Generate Stored Filename
        String storedFilename = UUID.randomUUID().toString() + "." + extension;
        Path targetFile = targetLocation.resolve(storedFilename);

        // Save File
        file.transferTo(targetFile.toFile());

        // Create Entity
        VehicleDocument document = VehicleDocument.builder()
                .vehicle(vehicle)
                .documentType(uploadDTO.getDocumentType())
                .documentNumber(uploadDTO.getDocumentNumber())
                .originalFileName(originalFilename)
                .storedFileName(storedFilename)
                .storagePath(relativePath)
                .mimeType(mimeType)
                .fileSize(file.getSize())
                .checksum(checksum)
                .issueDate(uploadDTO.getIssueDate())
                .expiryDate(uploadDTO.getExpiryDate())
                .uploadedBy(SecurityUtils.getCurrentUsername())
                .build();

        VehicleDocument saved = documentRepository.save(document);
        auditLogService.logAction("Document Uploaded", "VehicleDocument", saved.getId(), null, saved, "Uploaded document type: " + saved.getDocumentType() + " for vehicle: " + vehicle.getVehicleNumber());

        return saved;
    }

    @Override
    public VehicleDocument getDocumentById(Long id) {
        return documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Document not found with ID: " + id));
    }

    @Override
    public Resource downloadDocument(Long id) throws IOException {
        VehicleDocument document = getDocumentById(id);
        Path filePath = Paths.get(baseUploadDir).resolve(document.getStoragePath()).resolve(document.getStoredFileName()).toAbsolutePath().normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            auditLogService.logAction("Document Downloaded", "VehicleDocument", id, null, null, "Downloaded document: " + document.getOriginalFileName());
            return resource;
        } else {
            throw new ResourceNotFoundException("File not found or not readable on the server.");
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        VehicleDocument document = getDocumentById(id);
        if (document.isDeleted()) {
            throw new InvalidOperationException("Document is already deleted.");
        }
        
        document.setDeleted(true);
        document.setDeletedAt(java.time.LocalDateTime.now());
        document.setDeletedBy(SecurityUtils.getCurrentUsername());
        
        documentRepository.save(document);
        auditLogService.logAction("Document Deleted", "VehicleDocument", id, null, null, "Deleted document: " + document.getOriginalFileName());
    }

    @Override
    @Transactional
    public void restoreDocument(Long id) {
        VehicleDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Document not found with id: " + id));
        if (!document.isDeleted()) {
            throw new InvalidOperationException("Document is already active and cannot be restored.");
        }
        
        document.setDeleted(false);
        document.setDeletedAt(null);
        document.setDeletedBy(null);
        
        documentRepository.save(document);
        auditLogService.logAction("Document Restored", "VehicleDocument", id, null, null, "Restored document: " + document.getOriginalFileName());
    }

    @Override
    public Page<VehicleDocument> searchDocuments(Long vehicleId, DocumentType documentType, String statusFilter, Pageable pageable) {
        LocalDate now = LocalDate.now();
        LocalDate threshold = now.plusDays(31);
        return documentRepository.searchByDeletedFalse(vehicleId, documentType, statusFilter, now, threshold, pageable);
    }

    @Override
    public Page<VehicleDocument> getDeletedDocuments(Pageable pageable) {
        return documentRepository.findAllByDeletedTrue(pageable);
    }

    private String generateChecksum(InputStream fis) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] byteArray = new byte[1024];
        int bytesCount;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
