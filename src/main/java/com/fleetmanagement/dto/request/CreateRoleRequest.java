package com.fleetmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating new roles with permissions
 * Supports permission assignment and scope configuration
 */
@Data
public class CreateRoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private UUID tenantId;
    
    private String scopeType = "TENANT";
    
    @NotEmpty(message = "At least one permission is required")
    private Set<UUID> permissionIds;
}