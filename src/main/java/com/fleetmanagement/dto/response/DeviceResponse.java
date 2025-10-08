package com.fleetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for Device entity
 * Includes device information with installation and expiry details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponse {

    private UUID id;
    private String imei;
    private String deviceModel;
    private String firmwareVersion;
    private String simNumber;
    private String status;
    private UUID tenantId;
    private Boolean registeredBySms;
    private String installerPhone;
    private UUID createdBy;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime installedAt;
    private LocalDateTime expiryAt;
    private LocalDateTime updatedAt;
    private Set<VehicleResponseDto> vehicles;
    private Set<UserResponse> users;
}