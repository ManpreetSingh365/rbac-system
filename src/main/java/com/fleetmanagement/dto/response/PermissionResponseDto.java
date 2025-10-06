package com.fleetmanagement.dto.response;


import com.fleetmanagement.entity.Permission;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for permission data
 */
@Data
public class PermissionResponseDto {

    private UUID id;

    private String code;

    private String name;

    private String description;

    private Permission.PermissionCategory category;

    private Boolean active;

    private Boolean requiresScope;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Set<UUID> roleIds;
}