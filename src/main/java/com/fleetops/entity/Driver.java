package com.fleetops.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "License number cannot be blank")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @NotBlank(message = "Phone cannot be blank")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(nullable = false)
    private String phone;

    @NotNull(message = "Driver status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle currentVehicle;
}
