package com.fleetops.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_logs", indexes = {
    @Index(name = "idx_maint_deleted", columnList = "is_deleted"),
    @Index(name = "idx_maint_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Garage name cannot be blank")
    @Size(max = 100, message = "Garage name must not exceed 100 characters")
    @Column(nullable = false)
    private String garage;

    @NotNull(message = "Service cost cannot be null")
    @Min(value = 0, message = "Cost cannot be negative")
    @Column(nullable = false)
    private Double cost;

    @NotNull(message = "Service date cannot be null")
    @Column(nullable = false)
    private LocalDate serviceDate;

    @NotNull(message = "Next service date cannot be null")
    @Column(nullable = false)
    private LocalDate nextServiceDate;

    @NotNull(message = "Maintenance status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;

    @NotNull(message = "Vehicle cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;
}
