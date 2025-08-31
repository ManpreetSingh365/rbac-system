package com.fleetmanagement.service;

import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.repository.PermissionRepository;
import com.fleetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Permission Service - Handles permission checking and validation
 * Updated: Proper business logic implementation without caching
 * Implements comprehensive permission validation with scope checking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {
    
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * Core permission checking method with comprehensive business logic
     * Supports scoped permissions for multi-tenant architecture
     * 
     * @param userId User requesting permission
     * @param permissionCode Permission code to check
     * @param scopeId Scope context (tenantId, fleetId, etc.)
     * @return true if user has permission within scope
     */
    public boolean hasPermission(UUID userId, String permissionCode, UUID scopeId) {
        log.debug("Checking permission: {} for user: {} with scope: {}", permissionCode, userId, scopeId);
        
        // Step 1: Validate input parameters
        if (userId == null || permissionCode == null || permissionCode.trim().isEmpty()) {
            log.warn("Invalid permission check parameters: userId={}, permissionCode={}", userId, permissionCode);
            return false;
        }
        
        // Step 2: Load user with roles and permissions (single query optimization)
        User user = getUserWithPermissions(userId);
        if (user == null) {
            log.debug("User not found or inactive: {}", userId);
            return false;
        }
        
        // Step 3: Check for SUPER_ADMIN (bypass all other checks)
        if (hasSuperAdminPermission(user)) {
            log.debug("User {} has SUPER_ADMIN permission - access granted", userId);
            return true;
        }
        
        // Step 4: Extract user permissions from roles
        Set<String> userPermissions = extractUserPermissions(user);
        
        // Step 5: Check if user has the specific permission
        if (!userPermissions.contains(permissionCode)) {
            log.debug("User {} does not have permission: {}", userId, permissionCode);
            return false;
        }
        
        // Step 6: Validate scope if required
        if (scopeId != null && !validatePermissionScope(user, permissionCode, scopeId)) {
            log.debug("Scope validation failed for user: {} with scope: {}", userId, scopeId);
            return false;
        }
        
        log.debug("Permission granted: {} for user: {} within scope: {}", permissionCode, userId, scopeId);
        return true;
    }
    
    /**
     * Get all permissions for a user through their roles
     * Used for bulk permission checks and authorization
     */
    public Set<String> getAllUserPermissions(UUID userId) {
        log.debug("Fetching all permissions for user: {}", userId);
        
        if (userId == null) {
            return Collections.emptySet();
        }
        
        User user = getUserWithPermissions(userId);
        if (user == null) {
            return Collections.emptySet();
        }
        
        Set<String> permissions = extractUserPermissions(user);
        log.debug("User {} has {} permissions", userId, permissions.size());
        
        return permissions;
    }
    
    /**
     * Check if user has any of the specified permissions (OR logic)
     * Optimized for multiple permission checks
     */
    public boolean hasAnyPermission(UUID userId, Set<String> permissionCodes, UUID scopeId) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return false;
        }
        
        log.debug("Checking if user {} has any of {} permissions", userId, permissionCodes.size());
        
        // Optimization: Get user permissions once
        Set<String> userPermissions = getAllUserPermissions(userId);
        
        // Check if user has SUPER_ADMIN (bypass all checks)
        if (userPermissions.contains("SUPER_ADMIN")) {
            return true;
        }
        
        // Check for any matching permission with scope validation
        for (String permissionCode : permissionCodes) {
            if (userPermissions.contains(permissionCode)) {
                if (scopeId == null || validatePermissionScopeForUser(userId, permissionCode, scopeId)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if user has all specified permissions (AND logic)
     * Used for operations requiring multiple permissions
     */
    public boolean hasAllPermissions(UUID userId, Set<String> permissionCodes, UUID scopeId) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return true; // Empty set means no requirements
        }
        
        log.debug("Checking if user {} has all {} permissions", userId, permissionCodes.size());
        
        return permissionCodes.stream()
            .allMatch(permission -> hasPermission(userId, permission, scopeId));
    }
    
    /**
     * Validate permission hierarchy - prevents privilege escalation
     * Business logic to ensure users cannot assign permissions higher than their own
     */
    public boolean canGrantPermission(UUID grantorUserId, String permissionCode, UUID targetTenantId) {
        log.debug("Checking if user {} can grant permission: {} to tenant: {}", 
            grantorUserId, permissionCode, targetTenantId);
        
        // SuperAdmin can grant any permission
        if (hasPermission(grantorUserId, "SUPER_ADMIN", null)) {
            return true;
        }
        
        // User must have the permission to grant it
        if (!hasPermission(grantorUserId, permissionCode, targetTenantId)) {
            log.debug("User {} cannot grant permission they don't have: {}", grantorUserId, permissionCode);
            return false;
        }
        
        // Business rule: Cannot grant SUPER_ADMIN unless you are SUPER_ADMIN
        if ("SUPER_ADMIN".equals(permissionCode)) {
            return hasPermission(grantorUserId, "SUPER_ADMIN", null);
        }
        
        // Additional business rules can be added here
        return true;
    }
    
    /**
     * Check if user can access a specific tenant
     * Core business logic for multi-tenant security
     */
    public boolean canAccessTenant(UUID userId, UUID tenantId) {
        log.debug("Checking if user {} can access tenant: {}", userId, tenantId);
        
        if (userId == null || tenantId == null) {
            return false;
        }
        
        // SuperAdmin can access all tenants
        if (hasPermission(userId, "SUPER_ADMIN", null)) {
            return true;
        }
        
        User user = getUserWithPermissions(userId);
        if (user == null) {
            return false;
        }
        
        // User can access their own tenant
        return tenantId.equals(user.getTenantId());
    }
    
    /**
     * Refresh user permissions - for real-time permission changes
     * Since we use direct DB queries, this ensures fresh data
     */
    @Transactional
    public void refreshUserPermissions(UUID userId) {
        log.info("Refreshing permissions for user: {}", userId);
        
        // With direct database queries, permissions are always fresh
        // This method can be used for audit logging or future enhancements
        
        User user = getUserWithPermissions(userId);
        if (user != null) {
            Set<String> permissions = extractUserPermissions(user);
            log.info("User {} permissions refreshed - has {} permissions", userId, permissions.size());
        }
    }
    
    // =================== PRIVATE HELPER METHODS ===================
    
    /**
     * Load user with roles and permissions in single query
     * Optimization to avoid N+1 queries
     */
    private User getUserWithPermissions(UUID userId) {
        return userRepository.findByIdWithRolesAndPermissions(userId).orElse(null);
    }
    
    /**
     * Check if user has SUPER_ADMIN permission
     * Business logic for administrative bypass
     */
    private boolean hasSuperAdminPermission(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        
        return user.getRoles().stream()
            .filter(Role::getActive)
            .flatMap(role -> role.getPermissions().stream())
            .filter(Permission::getActive)
            .anyMatch(permission -> "SUPER_ADMIN".equals(permission.getCode()));
    }
    
    /**
     * Extract all permission codes from user's active roles
     * Core business logic for permission inheritance
     */
    private Set<String> extractUserPermissions(User user) {
        if (user == null || user.getRoles() == null) {
            return Collections.emptySet();
        }
        
        return user.getRoles().stream()
            .filter(Role::getActive) // Only active roles
            .flatMap(role -> {
                if (role.getPermissions() == null) {
                    return java.util.stream.Stream.empty();
                }
                return role.getPermissions().stream();
            })
            .filter(Permission::getActive) // Only active permissions
            .map(Permission::getCode)
            .collect(Collectors.toSet());
    }
    
    /**
     * Validate permission scope with comprehensive business logic
     * Implements hierarchical scope validation
     */
    private boolean validatePermissionScope(User user, String permissionCode, UUID scopeId) {
        // Global users (no tenant) can access all scopes
        if (user.getTenantId() == null) {
            log.debug("Global user - scope validation passed");
            return true;
        }
        
        // Check if permission requires scope validation
        Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
        if (permission == null || !permission.getRequiresScope()) {
            log.debug("Permission {} does not require scope validation", permissionCode);
            return true;
        }
        
        // Tenant-level scope validation
        if (user.getTenantId().equals(scopeId)) {
            log.debug("Tenant scope validation passed for user: {}", user.getId());
            return true;
        }
        
        // Additional hierarchical scope validation can be added here
        // For example: Fleet > Vehicle hierarchy
        
        log.debug("Scope validation failed - user tenant: {}, required scope: {}", 
            user.getTenantId(), scopeId);
        return false;
    }
    
    /**
     * Validate permission scope for a specific user (helper method)
     */
    private boolean validatePermissionScopeForUser(UUID userId, String permissionCode, UUID scopeId) {
        User user = getUserWithPermissions(userId);
        if (user == null) {
            return false;
        }
        return validatePermissionScope(user, permissionCode, scopeId);
    }
    
    /**
     * Business logic for role-based permission validation
     * Ensures users can only work within their organizational boundaries
     */
    public boolean canManageUser(UUID managerId, UUID targetUserId) {
        log.debug("Checking if user {} can manage user: {}", managerId, targetUserId);
        
        // SuperAdmin can manage anyone
        if (hasPermission(managerId, "SUPER_ADMIN", null)) {
            return true;
        }
        
        // Cannot manage yourself for certain operations
        if (managerId.equals(targetUserId)) {
            return false;
        }
        
        // Must have user management permission
        if (!hasPermission(managerId, "USER_UPDATE", null)) {
            return false;
        }
        
        // Both users must be in same tenant
        User manager = getUserWithPermissions(managerId);
        User target = getUserWithPermissions(targetUserId);
        
        if (manager == null || target == null) {
            return false;
        }
        
        return manager.getTenantId() != null && 
               manager.getTenantId().equals(target.getTenantId());
    }
}