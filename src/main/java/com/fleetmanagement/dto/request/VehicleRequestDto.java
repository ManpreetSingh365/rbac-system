package com.fleetmanagement.dto.request;

import com.fleetmanagement.entity.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating and updating vehicles
 */
@Data
public class VehicleRequestDto {

    @NotBlank(message = "License plate is required")
    @Size(max = 15, message = "License plate cannot exceed 15 characters")
    private String licensePlate;

    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be 1900 or later")
    private Integer year;

    @Size(max = 20, message = "VIN cannot exceed 20 characters")
    private String vin;

    @NotNull(message = "Vehicle type is required")
    private Vehicle.VehicleType vehicleType;

    @NotNull(message = "Vehicle status is required")
    private Vehicle.VehicleStatus status;

    private UUID fleetId;

    private UUID deviceId;

    private Set<UUID> userIds;
}