package com.fleetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for User entity
 * Updated: Changed email to username, made email optional
 * Excludes sensitive information like passwords
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String username;  // Changed from email to username
    private String email;     // Optional field
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private Boolean active;    
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID modifiedBy;

    private Set<UUID> roleIds;
    private Set<UUID> devicesIds;
    private Set<UUID> vehiclesIds;
}