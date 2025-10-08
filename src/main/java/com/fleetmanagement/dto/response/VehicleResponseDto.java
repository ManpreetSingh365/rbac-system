package com.fleetmanagement.dto.response;

import com.fleetmanagement.entity.Vehicle;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for vehicle data
 */
@Data
public class VehicleResponseDto {

    private UUID id;

    private String licensePlate;

    private String brand;

    private String model;

    private Integer year;

    private String vin;

    private Vehicle.VehicleType vehicleType;

    private Vehicle.VehicleStatus status;

    private UUID tenantId;

    private UUID fleetId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private UUID deviceId;

    private Set<UUID> userIds;
}