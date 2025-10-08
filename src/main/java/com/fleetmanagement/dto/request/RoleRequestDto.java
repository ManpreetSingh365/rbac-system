package com.fleetmanagement.dto.request;

import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.type.RoleScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for creating/updating roles
 */
@Data
public class RoleRequestDto {

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Active status is required")
    private Boolean active;

    private UUID tenantId;

    @NotNull(message = "Role Scope is required")
    private RoleScope roleScope;

    private Set<UUID> permissionIds;
}