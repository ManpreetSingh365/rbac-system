package com.fleetmanagement.dto.response;


import com.fleetmanagement.entity.Device;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for device data
 */
@Data
public class DeviceResponseDto {

    private UUID id;

    private String imei;

    private String deviceModel;

    private String firmwareVersion;

    private String simNumber;

    private Device.DeviceStatus status;

    private UUID tenantId;

    private Boolean registeredBySms;

    private String installerPhone;

    private LocalDateTime lastHeartbeat;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime installedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private UUID createdBy;

    private Set<UUID> userIds;

    private UUID vehicleId;
}