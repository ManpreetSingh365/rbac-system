package com.fleetmanagement.dto.request;


import java.util.Set;
import java.util.UUID;

import com.fleetmanagement.entity.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating and updating permissions
 */
@Data
public class PermissionRequestDto {



    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code cannot exceed 100 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Category is required")
    private Permission.PermissionCategory category;

    @NotNull(message = "Active status is required")
    private Boolean active;

    @NotNull(message = "Requires scope is required")
    private Boolean requiresScope;

    private Set<UUID> roleIds;
}