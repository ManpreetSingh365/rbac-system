package com.fleetmanagement.repository;

import com.fleetmanagement.entity.User;
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
 * User Repository with optimized queries for scalability
 * Updated: Changed email to username authentication
 * Includes custom queries for performance optimization
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username for authentication (changed from email)
     */
    Optional<User> findByUsernameAndActiveTrue(String username);

    /**
     * Find user by email (optional field now)
     */
    Optional<User> findByEmailAndActiveTrue(String email);
    
    
    Optional<User> findByEmailIgnoreCase(String email);

    
    /**
     * Check if user exists by username
     */
    boolean existsByUsernameAndActiveTrue(String username);


    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.roles
    LEFT JOIN FETCH u.devices
    LEFT JOIN FETCH u.vehicles
    WHERE u.tenantId = :tenantId AND u.active = true
    """)
    Page<User> findAllWithRelations(@Param("tenantId") UUID tenantId,Pageable pageable);



    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles
    LEFT JOIN FETCH u.devices
    LEFT JOIN FETCH u.vehicles
    WHERE u.id = :userId
""")
    Optional<User> findByIdWithRelations(UUID userId);


    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find active users by tenant with pagination
     */
    Page<User> findByTenantIdAndActiveTrue(UUID tenantId, Pageable pageable);

    /**
     * Find all active users with pagination
     */
    Page<User> findByActiveTrue(Pageable pageable);

    /**
     * Find users with specific role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId AND u.active = true")
    Page<User> findByRoleId(@Param("roleId") UUID roleId, Pageable pageable);

    /**
     * Find users assigned to specific device
     */
    @Query("SELECT u FROM User u JOIN u.devices d WHERE d.id = :deviceId AND u.active = true")
    Page<User> findByDeviceId(@Param("deviceId") UUID deviceId, Pageable pageable);

    /**
     * Find users assigned to specific vehicle
     */
    @Query("SELECT u FROM User u JOIN u.vehicles v WHERE v.id = :vehicleId AND u.active = true")
    Page<User> findByVehicleId(@Param("vehicleId") UUID vehicleId, Pageable pageable);

    /**
     * Count active users by tenant
     */
    long countByTenantIdAndActiveTrue(UUID tenantId);

    /**
     * Find user with roles eagerly loaded for permission checking
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :userId AND u.active = true")
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") UUID userId);

    /**
     * Find users by phone number
     */
    Optional<User> findByPhoneNumberAndActiveTrue(String phoneNumber);

    /**
     * Find users by partial username (search functionality)
     */
    Page<User> findByUsernameContainingIgnoreCaseAndActiveTrue(String username, Pageable pageable);

    /**
     * Find users by partial email (search functionality)
     */
    Page<User> findByEmailContainingIgnoreCaseAndActiveTrue(String email, Pageable pageable);

    /**
     * Find users by name (first name or last name)
     */
    @Query("SELECT u FROM User u WHERE (u.firstName LIKE %:name% OR u.lastName LIKE %:name%) AND u.active = true")
    Page<User> findByNameContaining(@Param("name") String name, Pageable pageable);

    Optional<User> findByIdAndActiveTrue(UUID id);

    Set<User> findAllByIdIn(Set<UUID> userIds);
}