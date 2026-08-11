package com.fleetops.repository;

import com.fleetops.entity.DocumentType;
import com.fleetops.entity.VehicleDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {

    @EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT d FROM VehicleDocument d WHERE d.deleted = false AND d.archived = false AND " +
           "(:vehicleId IS NULL OR d.vehicle.id = :vehicleId) AND " +
           "(cast(:documentType as string) IS NULL OR d.documentType = :documentType) AND " +
           "(:statusFilter IS NULL OR " +
           "   (:statusFilter = 'VALID' AND (d.expiryDate IS NULL OR d.expiryDate >= :validThreshold)) OR " +
           "   (:statusFilter = 'EXPIRING_SOON' AND d.expiryDate >= :currentDate AND d.expiryDate < :validThreshold) OR " +
           "   (:statusFilter = 'EXPIRED' AND d.expiryDate < :currentDate) " +
           ")")
    Page<VehicleDocument> searchByDeletedFalse(
            @Param("vehicleId") Long vehicleId,
            @Param("documentType") DocumentType documentType,
            @Param("statusFilter") String statusFilter,
            @Param("currentDate") LocalDate currentDate,
            @Param("validThreshold") LocalDate validThreshold,
            Pageable pageable);

    @EntityGraph(attributePaths = {"vehicle"})
    Optional<VehicleDocument> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"vehicle"})
    Page<VehicleDocument> findAllByDeletedTrue(Pageable pageable);

    Optional<VehicleDocument> findByVehicleIdAndDocumentTypeAndArchivedFalseAndDeletedFalse(Long vehicleId, DocumentType documentType);

    long countByDeletedFalseAndArchivedFalse();

    @Query("SELECT COUNT(d) FROM VehicleDocument d WHERE d.deleted = false AND d.archived = false AND d.expiryDate < :currentDate")
    long countExpiredDocuments(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(d) FROM VehicleDocument d WHERE d.deleted = false AND d.archived = false AND d.expiryDate >= :currentDate AND d.expiryDate < :validThreshold")
    long countExpiringSoonDocuments(@Param("currentDate") LocalDate currentDate, @Param("validThreshold") LocalDate validThreshold);

    @EntityGraph(attributePaths = {"vehicle"})
    List<VehicleDocument> findTop5ByDeletedFalseAndArchivedFalseOrderByUploadedAtDesc();

    @EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT d FROM VehicleDocument d WHERE d.deleted = false AND d.archived = false AND d.expiryDate <= :thresholdDate")
    List<VehicleDocument> findExpiringOrExpiredDocuments(@Param("thresholdDate") LocalDate thresholdDate);
}
