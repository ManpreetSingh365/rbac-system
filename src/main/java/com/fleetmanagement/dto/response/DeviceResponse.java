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
    private LocalDateTime lastHeartbeat;
    private LocalDateTime installedAt;  // Changed from createdAt
    private LocalDateTime expiryAt;     // Added expiry field
    private LocalDateTime updatedAt;
    
    private VehicleSummaryResponse vehicle;
    private Set<UserSummaryResponse> users;
    
    /**
     * Nested class for vehicle summary in device response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleSummaryResponse {
        private UUID id;
        private String licensePlate;
        private String brand;
        private String model;
        private Integer year;
        private String vehicleType;
        private String status;
    }
    
    /**
     * Nested class for user summary in device response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummaryResponse {
        private UUID id;
        private String username;
        private String email;
        private String fullName;
        private Boolean active;
    }
}