package com.fleetmanagement.dto.request;

import com.fleetmanagement.entity.Device;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating and updating devices
 */
@Data
public class DeviceRequestDto {

    @NotBlank(message = "IMEI is required")
    @Size(max = 20, message = "IMEI cannot exceed 20 characters")
    private String imei;

    @NotBlank(message = "Device model is required")
    @Size(max = 100, message = "Device model cannot exceed 100 characters")
    private String deviceModel;

    @Size(max = 100, message = "Firmware version cannot exceed 100 characters")
    private String firmwareVersion;

    @Size(max = 20, message = "SIM number cannot exceed 20 characters")
    private String simNumber;

    @NotNull(message = "Status is required")
    private Device.DeviceStatus status;

    private UUID tenantId;

    @NotNull(message = "Registered by SMS is required")
    private Boolean registeredBySms;

    @Size(max = 20, message = "Installer phone cannot exceed 20 characters")
    private String installerPhone;

    private LocalDateTime expiryAt;

    private UUID createdBy;

    private UUID modifiedBy;

    private Set<UUID> userIds;
}