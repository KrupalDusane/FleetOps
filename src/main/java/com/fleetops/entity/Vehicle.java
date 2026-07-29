package com.fleetops.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vehicle number cannot be blank")
    @Size(min = 5, max = 20, message = "Vehicle number must be between 5 and 20 characters")
    @Column(unique = true, nullable = false)
    private String vehicleNumber;

    @NotBlank(message = "Brand cannot be blank")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Model cannot be blank")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    @Column(nullable = false)
    private String model;

    @NotNull(message = "Manufacturing year cannot be null")
    @Min(value = 1990, message = "Manufacturing year must be at least 1990")
    @Max(value = 2027, message = "Manufacturing year cannot exceed 2027")
    @Column(nullable = false)
    private Integer manufacturingYear;

    @NotNull(message = "Fuel type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @NotNull(message = "Current odometer reading cannot be null")
    @Min(value = 0, message = "Odometer reading must be 0 or greater")
    @Column(nullable = false)
    private Integer currentOdometer;

    @NotNull(message = "Vehicle status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;
}
