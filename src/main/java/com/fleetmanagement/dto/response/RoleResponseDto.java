package com.fleetmanagement.dto.response;

import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.type.RoleScope;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for role response
 */
@Data
public class RoleResponseDto {

    private UUID id;
    private String name;
    private String description;
    private Boolean active;
    private UUID tenantId;
    private RoleScope roleScope;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private Set<UUID> permissionIds;
}