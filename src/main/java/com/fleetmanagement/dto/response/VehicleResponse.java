package com.fleetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for Vehicle entity
 * Includes vehicle information with device and user assignments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    
    private UUID id;
    private String licensePlate;
    private String brand;        // Changed from make
    private String model;
    private Integer year;
    private String vin;
    private String vehicleType;
    private String status;
    private UUID tenantId;
    private UUID fleetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private DeviceSummaryResponse device;
    private Set<UserSummaryResponse> users;
    
    /**
     * Nested class for device summary in vehicle response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceSummaryResponse {
        private UUID id;
        private String imei;
        private String deviceModel;
        private String status;
        private Boolean registeredBySms;
        private LocalDateTime lastHeartbeat;
        private LocalDateTime expiryAt;
    }
    
    /**
     * Nested class for user summary in vehicle response
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