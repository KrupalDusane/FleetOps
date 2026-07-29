package com.fleetops.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "fuel_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Fuel date cannot be null")
    @PastOrPresent(message = "Fuel date cannot be in the future")
    @Column(nullable = false)
    private LocalDate fuelDate;

    @NotNull(message = "Fuel quantity cannot be null")
    @Min(value = 1, message = "Fuel quantity must be greater than 0")
    @Column(nullable = false)
    private Double fuelQuantity;

    @NotNull(message = "Price per litre cannot be null")
    @Min(value = 0, message = "Price per litre cannot be negative")
    @Column(nullable = false)
    private Double pricePerLitre;

    @Column(nullable = false)
    private Double totalCost;

    @NotNull(message = "Odometer reading cannot be null")
    @Min(value = 0, message = "Odometer reading must be 0 or greater")
    @Column(nullable = false)
    private Integer odometerAtFueling;

    @NotNull(message = "Vehicle cannot be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
}
