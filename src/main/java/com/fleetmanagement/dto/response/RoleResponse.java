package com.fleetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for Role entity
 * Includes role information with associated permissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    
    private UUID id;
    private String name;
    private String description;
    private Boolean active;
    private UUID tenantId;
    private String scopeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Set<PermissionResponse> permissions;
    private Set<UserSummaryResponse> users;
    
    /**
     * Nested class for user summary in role response
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