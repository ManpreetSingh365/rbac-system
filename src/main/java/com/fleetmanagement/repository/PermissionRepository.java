package com.fleetmanagement.repository;

import com.fleetmanagement.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Permission Repository with optimized queries for scalability
 * Includes custom queries for permission management operations
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    /**
     * Find permission by code
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * Find active permissions with pagination
     */
    Page<Permission> findByActiveTrue(Pageable pageable);
    
    /**
     * Find permissions by category
     */
    Page<Permission> findByCategoryAndActiveTrue(Permission.PermissionCategory category, Pageable pageable);
    
    /**
     * Find permissions by codes
     */
    List<Permission> findByCodeIn(Set<String> codes);
    
    /**
     * Check if permission exists by code
     */
    boolean existsByCode(String code);
    
    /**
     * Find permissions that require scope
     */
    Page<Permission> findByRequiresScopeAndActiveTrue(Boolean requiresScope, Pageable pageable);
    
    /**
     * Find permissions assigned to specific role
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId AND p.active = true")
    Page<Permission> findByRoleId(@Param("roleId") UUID roleId, Pageable pageable);
    
    /**
     * Count permissions by category
     */
    long countByCategoryAndActiveTrue(Permission.PermissionCategory category);
    
    /**
     * Find permissions by name containing (search)
     */
    Page<Permission> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    
    /**
     * Find all active permissions (for role assignment)
     */
    List<Permission> findByActiveTrueOrderByCategory();

    Set<Permission> findAllByIdIn(Set<UUID> ids);
}