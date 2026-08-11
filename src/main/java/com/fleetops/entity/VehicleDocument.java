package com.fleetops.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_documents", indexes = {
    @Index(name = "idx_vecdoc_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_vecdoc_type", columnList = "document_type"),
    @Index(name = "idx_vecdoc_expiry", columnList = "expiry_date"),
    @Index(name = "idx_vecdoc_archived", columnList = "is_archived"),
    @Index(name = "idx_vecdoc_deleted", columnList = "is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Vehicle cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull(message = "Document type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @NotBlank(message = "Original file name cannot be blank")
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotBlank(message = "Stored file name cannot be blank")
    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @NotBlank(message = "Storage path cannot be blank")
    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @NotBlank(message = "MIME type cannot be blank")
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "Checksum cannot be blank")
    @Column(name = "checksum", nullable = false)
    private String checksum;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @NotNull(message = "Expiry date cannot be null")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

    @Transient
    public DocumentStatus getStatus() {
        if (expiryDate == null) return DocumentStatus.VALID;
        LocalDate now = LocalDate.now();
        if (expiryDate.isBefore(now)) return DocumentStatus.EXPIRED;
        if (expiryDate.isBefore(now.plusDays(31))) return DocumentStatus.EXPIRING_SOON;
        return DocumentStatus.VALID;
    }
}
