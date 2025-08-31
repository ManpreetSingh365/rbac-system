package com.fleetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for Permission entity
 * Includes permission information with category details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String category;
    private Boolean active;
    private Boolean requiresScope;
    private LocalDateTime createdAt;
    
    private Set<RoleSummaryResponse> roles;
    
    /**
     * Nested class for role summary in permission response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleSummaryResponse {
        private UUID id;
        private String name;
        private String description;
        private Boolean active;
        private String scopeType;
    }
}