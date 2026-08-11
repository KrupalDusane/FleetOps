package com.fleetops.service;

import com.fleetops.dto.VehicleDocumentUploadDTO;
import com.fleetops.entity.DocumentType;
import com.fleetops.entity.VehicleDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public interface VehicleDocumentService {

    VehicleDocument uploadDocument(VehicleDocumentUploadDTO uploadDTO) throws IOException, NoSuchAlgorithmException;

    VehicleDocument getDocumentById(Long id);

    org.springframework.core.io.Resource downloadDocument(Long id) throws IOException;

    void deleteDocument(Long id);

    void restoreDocument(Long id);

    Page<VehicleDocument> searchDocuments(Long vehicleId, DocumentType documentType, String statusFilter, Pageable pageable);

    Page<VehicleDocument> getDeletedDocuments(Pageable pageable);
}
