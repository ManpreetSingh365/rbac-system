package com.fleetmanagement.repository;

import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.type.RoleScope;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Role Repository with optimized queries for scalability
 * Includes custom queries for role management operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);

    /**
     * Find active roles by tenant with pagination
     */
    Page<Role> findByTenantIdAndActiveTrue(UUID tenantId, Pageable pageable);

    /**
     * Find all active roles with pagination
     */
    Page<Role> findByActiveTrue(Pageable pageable);

    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if role exists by name for tenant (excluding current role)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name AND r.tenantId = :tenantId AND r.id != :excludeId")
    boolean existsByNameAndTenantIdExcluding(@Param("name") String name, @Param("tenantId") UUID tenantId, @Param("excludeId") UUID excludeId);

    /**
     * Find roles with specific permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId AND r.active = true")
    Page<Role> findByPermissionId(@Param("permissionId") UUID permissionId, Pageable pageable);

    /**
     * Find role with permissions eagerly loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :roleId AND r.active = true")
    Optional<Role> findByIdWithPermissions(@Param("roleId") UUID roleId);

    /**
     * Count active roles by tenant
     */
    long countByTenantIdAndActiveTrue(UUID tenantId);

    /**
     * Find roles by Role Scope
     */
    Page<Role> findByRoleScopeAndActiveTrue(RoleScope roleScope, Pageable pageable);

    /**
     * Find roles assigned to specific user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId AND r.active = true")
    Page<Role> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find role by name and tenant ID
     */
    Optional<Role> findByNameAndTenantId(@Param("name") String name, @Param("tenantId") UUID tenantId);

    /**
     * Check if role exists by name and tenant ID
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name AND r.tenantId = :tenantId")
    boolean existsByNameAndTenantId(@Param("name") String name, @Param("tenantId") UUID tenantId);

    /**
     * Find roles by tenant ID and Role Scope with active status
     */
    Page<Role> findByTenantIdAndRoleScopeAndActiveTrue(@Param("tenantId") UUID tenantId, @Param("roleScope") RoleScope roleScope, Pageable pageable);
}