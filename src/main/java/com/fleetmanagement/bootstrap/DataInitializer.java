package com.fleetmanagement.bootstrap;

import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.entity.type.RoleScope;
import com.fleetmanagement.repository.PermissionRepository;
import com.fleetmanagement.repository.RoleRepository;
import com.fleetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Modern RBAC Data Initializer - Spring Boot 3.x Compatible
 * 
 * Features:
 * - Complete error handling and validation
 * - Modern Java 21 features and best practices
 * - Configuration-driven approach
 * - Comprehensive logging and monitoring
 * - Proper transaction management
 * - Idempotent operations
 * - Structured permission categories
 * 
 * @author Fleet Management System
 * @version 2.0
 * @since Spring Boot 3.x
 */
@Component
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    // Repositories
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Configuration Properties
    @Value("${app.default-tenant-id:#{T(java.util.UUID).randomUUID().toString()}}")
    private String defaultTenantId;

    @Value("${app.superadmin.username:superadmin}")
    private String superAdminUsername;

    @Value("${app.superadmin.password:SuperAdmin@123}")
    private String superAdminPassword;

    @Value("${app.superadmin.email:superadmin@fleetmanagement.com}")
    private String superAdminEmail;

    // Constants for better maintainability
//     private static final String SYSTEM_USER = "SYSTEM";
    private static final UUID SYSTEM_USER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Map<String, String> ROLE_DESCRIPTIONS = Map.of(
        "SUPER_ADMIN", "Super Administrator with complete system access across all tenants",
        "TENANT_ADMIN", "Tenant Administrator with full access within tenant scope",
        "FLEET_MANAGER", "Fleet Manager with vehicle and tracking management capabilities",
        "DISPATCHER", "Dispatcher with live tracking and communication access",
        "INSTALLER", "Device Installer with registration and activation permissions",
        "VIEWER", "Read-only access to tracking data and basic reports"
    );



    @Override
    @Transactional
    public void run(String... args) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🚀 Starting RBAC Data Initialization Process");
            
            // Validate prerequisites
            validatePrerequisites();
            
            // Check if initialization is needed
            if (isInitializationRequired()) {
                performInitialization();
            } else {
                log.info("📊 RBAC data already exists - skipping initialization");
                logCurrentStats();
            }
            
        } catch (Exception e) {
            log.error("❌ Fatal error during RBAC initialization", e);
            throw new RuntimeException("RBAC initialization failed", e);
        } finally {
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("⏱️ Total initialization time: {}ms", totalTime);
        }
    }

    /**
     * Validate system prerequisites before initialization
     */
    private void validatePrerequisites() {
        log.debug("🔍 Validating system prerequisites");
        
        try {
            // Test database connectivity
            permissionRepository.count();
            roleRepository.count();
            userRepository.count();
            
            // Validate configuration
            Objects.requireNonNull(defaultTenantId, "Default tenant ID cannot be null");
            Objects.requireNonNull(superAdminUsername, "Super admin username cannot be null");
            Objects.requireNonNull(passwordEncoder, "Password encoder cannot be null");
            
            log.debug("✅ All prerequisites validated successfully");
        } catch (Exception e) {
            log.error("❌ Prerequisites validation failed", e);
            throw new RuntimeException("System prerequisites not met", e);
        }
    }

    /**
     * Check if initialization is required
     */
    private boolean isInitializationRequired() {
        return permissionRepository.count() == 0L;
    }

    /**
     * Perform the complete initialization process
     */
    private void performInitialization() {
        log.info("🏗️ Performing fresh RBAC initialization");

        // Step 1: Create Permissions
        long permStart = System.currentTimeMillis();
        var permissions = createPermissions();
        log.info("✅ Created {} permissions ({}ms)", permissions.size(), System.currentTimeMillis() - permStart);

        // Step 2: Create Roles
        long roleStart = System.currentTimeMillis();
        var roles = createRoles(permissions);
        log.info("✅ Created {} roles ({}ms)", roles.size(), System.currentTimeMillis() - roleStart);

        // Step 3: Create Super Admin
        long userStart = System.currentTimeMillis();
        var superAdmin = createSuperAdmin(roles);
        log.info("✅ Created SuperAdmin user: {} ({}ms)", superAdmin.getUsername(), System.currentTimeMillis() - userStart);

        // Step 4: Log final statistics
        logInitializationComplete();
    }

    /**
     * Create comprehensive permission system with modern enum approach
     */
    private List<Permission> createPermissions() {
        log.info("🔑 Creating comprehensive permission system");

        var permissionDefinitions = getPermissionDefinitions();
        var createdPermissions = new ArrayList<Permission>();

        permissionDefinitions.forEach((category, permissions) -> {
                log.debug("Creating {} permissions for category: {}", permissions.size(), category);

                permissions.forEach(permDef -> {
                try {
                        // Map initializer enum to entity enum
                        var entityCategory = Permission.PermissionCategory.valueOf(category.name());
                        var permission = createSinglePermission(permDef, entityCategory);
                        createdPermissions.add(permission);
                } catch (Exception e) {
                        log.warn("⚠️ Failed to create permission: {} - {}", permDef.code(), e.getMessage());
                }
                });
        });

        log.info("📊 Permission creation summary: {} total permissions across {} categories",
                createdPermissions.size(), permissionDefinitions.size());

        return createdPermissions;
}

    /**
     * Modern permission definitions using records for better structure
     */
    private Map<Permission.PermissionCategory, List<PermissionDefinition>> getPermissionDefinitions() {
        return Map.of(
            // System Administration
            Permission.PermissionCategory.SYSTEM_ADMINISTRATION, List.of(
                new PermissionDefinition("SUPER_ADMIN", "Super Administrator Access", 
                    "Complete system access with all permissions", false),
                new PermissionDefinition("SYSTEM_MAINTENANCE", "System Maintenance", 
                    "Core system operations and maintenance tasks", true),
                new PermissionDefinition("MULTI_TENANT_MANAGE", "Multi-Tenant Management", 
                    "Cross-tenant operations and global management", true),
                new PermissionDefinition("SYSTEM_CONFIG", "System Configuration", 
                    "Modify global system settings and parameters", true)
            ),
            
            // User Management
            Permission.PermissionCategory.USER_MANAGEMENT, List.of(
                new PermissionDefinition("USER_CREATE", "Create User", "Create new user accounts", true),
                new PermissionDefinition("USER_READ", "View Users", "View user information and profiles", true),
                new PermissionDefinition("USER_UPDATE", "Update User", "Modify user information and settings", true),
                new PermissionDefinition("USER_DELETE", "Delete User", "Remove user accounts from system", true),
                new PermissionDefinition("USER_RESET_PASSWORD", "Reset Password", "Reset user passwords", true),
                new PermissionDefinition("USER_ACTIVATE", "Activate/Deactivate Users", "Enable or disable user accounts", true)
            ),
            
            // Role Management
            Permission.PermissionCategory.ROLE_MANAGEMENT, List.of(
                new PermissionDefinition("ROLE_CREATE", "Create Role", "Create new roles and permissions", true),
                new PermissionDefinition("ROLE_READ", "View Roles", "View role definitions and permissions", true),
                new PermissionDefinition("ROLE_UPDATE", "Update Role", "Modify role permissions and settings", true),
                new PermissionDefinition("ROLE_DELETE", "Delete Role", "Remove roles from system", true),
                new PermissionDefinition("ROLE_ASSIGN", "Assign Roles", "Assign roles to users", true)
            ),
            
            // Device Management
            Permission.PermissionCategory.DEVICE_MANAGEMENT, List.of(
                new PermissionDefinition("DEVICE_READ", "View Devices", "View device information and status", true),
                new PermissionDefinition("DEVICE_REGISTER", "Register Device", "Add new tracking devices", true),
                new PermissionDefinition("DEVICE_UPDATE", "Update Device", "Modify device settings and configuration", true),
                new PermissionDefinition("DEVICE_DELETE", "Delete Device", "Remove devices from system", true),
                new PermissionDefinition("DEVICE_ASSIGN", "Assign Device", "Assign devices to vehicles or users", true),
                new PermissionDefinition("DEVICE_ACTIVATE", "Activate Device", "Enable or disable device functionality", true),
                new PermissionDefinition("DEVICE_REMOTE_CONFIG", "Remote Configuration", "Push configuration updates to devices", true),
                new PermissionDefinition("DEVICE_BULK_OPERATIONS", "Bulk Device Operations", "Perform mass operations on multiple devices", true)
            ),
            
            // Vehicle Management
            Permission.PermissionCategory.VEHICLE_MANAGEMENT, List.of(
                new PermissionDefinition("VEHICLE_READ", "View Vehicles", "View vehicle details and information", true),
                new PermissionDefinition("VEHICLE_CREATE", "Create Vehicle", "Add new vehicles to fleet", true),
                new PermissionDefinition("VEHICLE_UPDATE", "Update Vehicle", "Modify vehicle information and settings", true),
                new PermissionDefinition("VEHICLE_DELETE", "Delete Vehicle", "Remove vehicles from fleet", true),
                new PermissionDefinition("VEHICLE_ASSIGN_DEVICE", "Assign Device to Vehicle", "Connect tracking devices to vehicles", true),
                new PermissionDefinition("FLEET_MANAGE", "Fleet Management", "Organize and manage vehicle groups", true),
                new PermissionDefinition("VEHICLE_MAINTENANCE", "Vehicle Maintenance", "Track and schedule vehicle maintenance", true)
            ),
            
            // Location & Tracking
            Permission.PermissionCategory.LOCATION_TRACKING, List.of(
                new PermissionDefinition("VIEW_LOCATION_LIVE", "Live Location Tracking", "View real-time vehicle locations", true),
                new PermissionDefinition("VIEW_LOCATION_HISTORY", "Location History", "Access historical tracking data", true),
                new PermissionDefinition("EXPORT_LOCATION", "Export Location Data", "Download and export location information", true),
                new PermissionDefinition("GEOFENCE_MANAGE", "Geofence Management", "Create and manage geographic boundaries", true),
                new PermissionDefinition("ROUTE_PLANNING", "Route Planning", "Create and optimize vehicle routes", true),
                new PermissionDefinition("PLAYBACK_HISTORY", "Route Playback", "Replay historical vehicle movements", true)
            ),
            
            // Alerts & Notifications
            Permission.PermissionCategory.ALERTS_NOTIFICATIONS, List.of(
                new PermissionDefinition("ALERT_READ", "View Alerts", "View system alerts and notifications", true),
                new PermissionDefinition("ALERT_MANAGE", "Manage Alerts", "Create and modify alert rules", true),
                new PermissionDefinition("ALERT_ACKNOWLEDGE", "Acknowledge Alerts", "Mark alerts as acknowledged", true),
                new PermissionDefinition("NOTIFICATION_SEND", "Send Notifications", "Send messages and notifications", true),
                new PermissionDefinition("EMERGENCY_ALERT", "Emergency Alerts", "Handle emergency situations and panic buttons", true)
            ),
            
            // Reports & Analytics
            Permission.PermissionCategory.REPORTS_ANALYTICS, List.of(
                new PermissionDefinition("REPORT_VIEW", "View Reports", "Access standard system reports", true),
                new PermissionDefinition("REPORT_GENERATE", "Generate Reports", "Create custom reports and analytics", true),
                new PermissionDefinition("REPORT_SCHEDULE", "Schedule Reports", "Set up automated report generation", true),
                new PermissionDefinition("ANALYTICS_ACCESS", "Analytics Dashboard", "Access advanced analytics and KPIs", true),
                new PermissionDefinition("DATA_EXPORT", "Data Export", "Export data in various formats", true)
            ),
            
            // Security & Compliance
            Permission.PermissionCategory.SECURITY_COMPLIANCE, List.of(
                new PermissionDefinition("AUDIT_READ", "View Audit Logs", "Access system audit trails", true),
                new PermissionDefinition("SECURITY_CONFIG", "Security Configuration", "Modify security settings", true),
                new PermissionDefinition("COMPLIANCE_MANAGE", "Compliance Management", "Handle regulatory compliance", true),
                new PermissionDefinition("BACKUP_RESTORE", "Backup & Restore", "Manage data backup and recovery", true),
                new PermissionDefinition("API_ACCESS", "API Access", "Access to system APIs", true)
            )
        );
    }

    /**
     * Create individual permission with proper validation
     */
//     private Permission createSinglePermission(PermissionDefinition definition, PermissionCategory category) {
//         // Check if permission already exists
//         var existingPermission = permissionRepository.findByCode(definition.code());
//         if (existingPermission.isPresent()) {
//             log.debug("Permission {} already exists, skipping", definition.code());
//             return existingPermission.get();
//         }

//         var permission = Permission.builder()
//             .code(definition.code())
//             .name(definition.name())
//             .description(definition.description())
//             .category(category.name()) // Store as string for JPA compatibility
//             .active(true)
//             .requiresScope(definition.requiresScope())
//             .createdBy(SYSTEM_USER)
//             .createdAt(LocalDateTime.now())
//             .build();

//         return permissionRepository.save(permission);
//     }


private Permission createSinglePermission(PermissionDefinition definition, Permission.PermissionCategory category) {
    // Check if permission already exists
    var existingPermission = permissionRepository.findByCode(definition.code());
    if (existingPermission.isPresent()) {
        log.debug("Permission {} already exists, skipping", definition.code());
        return existingPermission.get();
    }

    // Build new permission
    var permission = Permission.builder()
            .code(definition.code())
            .name(definition.name())
            .description(definition.description())
            .category(category) // Entity enum
            .active(true)
            .requiresScope(definition.requiresScope())
            .createdBy(SYSTEM_USER)
            .createdAt(LocalDateTime.now())
            .build();

    return permissionRepository.save(permission);
}
    /**
     * Create role hierarchy with proper permission assignments
     */
    private List<Role> createRoles(List<Permission> permissions) {
        log.info("👥 Creating role hierarchy with permission assignments");

        var permissionMap = permissions.stream()
            .collect(Collectors.toMap(Permission::getCode, p -> p));

        var roles = new ArrayList<Role>();

        // Create roles in hierarchy order
        var roleHierarchy = List.of("VIEWER", "INSTALLER", "DISPATCHER", "FLEET_MANAGER", "TENANT_ADMIN", "SUPER_ADMIN");
        
        roleHierarchy.forEach(roleName -> {
            try {
                var role = createRoleWithPermissions(roleName, permissionMap);
                roles.add(role);
                log.debug("✅ Created role: {} with {} permissions", roleName, role.getPermissions().size());
            } catch (Exception e) {
                log.error("❌ Failed to create role: {}", roleName, e);
            }
        });

        return roles;
    }

    /**
     * Create individual role with specific permissions
     */
    private Role createRoleWithPermissions(String roleName, Map<String, Permission> permissionMap) {
        // Check if role already exists
        var existingRole = roleRepository.findByName(roleName);
        if (existingRole.isPresent()) {
            log.debug("Role {} already exists, skipping", roleName);
            return existingRole.get();
        }

        var role = Role.builder()
            .name(roleName)
            .description(ROLE_DESCRIPTIONS.get(roleName))
            .active(true)
            .tenantId(UUID.fromString(defaultTenantId))
            .roleScope(roleName.equals("SUPER_ADMIN") ? RoleScope.GLOBAL : RoleScope.TENANT)
            .createdBy(SYSTEM_USER)
            .createdAt(LocalDateTime.now())
            .build();

        // Assign permissions based on role
        var assignedPermissions = getPermissionsForRole(roleName, permissionMap);
        role.setPermissions(new HashSet<>(assignedPermissions));

        return roleRepository.save(role);
    }

    /**
     * Define permission assignments for each role
     */
    private List<Permission> getPermissionsForRole(String roleName, Map<String, Permission> permissionMap) {
        var rolePermissions = switch (roleName) {
            case "SUPER_ADMIN" -> permissionMap.values().stream().toList(); // All permissions
            
            case "TENANT_ADMIN" -> List.of(
                // User & Role Management
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE", "USER_RESET_PASSWORD", "USER_ACTIVATE",
                "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE", "ROLE_ASSIGN",
                // Device & Vehicle Management
                "DEVICE_READ", "DEVICE_REGISTER", "DEVICE_UPDATE", "DEVICE_ASSIGN", "DEVICE_ACTIVATE", "DEVICE_REMOTE_CONFIG", "DEVICE_BULK_OPERATIONS",
                "VEHICLE_READ", "VEHICLE_CREATE", "VEHICLE_UPDATE", "VEHICLE_DELETE", "VEHICLE_ASSIGN_DEVICE", "FLEET_MANAGE", "VEHICLE_MAINTENANCE",
                // Location & Tracking
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "EXPORT_LOCATION", "GEOFENCE_MANAGE", "ROUTE_PLANNING", "PLAYBACK_HISTORY",
                // Alerts & Reports
                "ALERT_READ", "ALERT_MANAGE", "ALERT_ACKNOWLEDGE", "NOTIFICATION_SEND",
                "REPORT_VIEW", "REPORT_GENERATE", "REPORT_SCHEDULE", "ANALYTICS_ACCESS", "DATA_EXPORT",
                // Security
                "AUDIT_READ", "API_ACCESS"
            ).stream().map(permissionMap::get).filter(Objects::nonNull).toList();
            
            case "FLEET_MANAGER" -> List.of(
                "VEHICLE_READ", "VEHICLE_UPDATE", "VEHICLE_ASSIGN_DEVICE", "FLEET_MANAGE", "VEHICLE_MAINTENANCE",
                "DEVICE_READ", "DEVICE_ASSIGN", "DEVICE_ACTIVATE",
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "EXPORT_LOCATION", "GEOFENCE_MANAGE", "ROUTE_PLANNING", "PLAYBACK_HISTORY",
                "ALERT_READ", "ALERT_MANAGE", "ALERT_ACKNOWLEDGE",
                "REPORT_VIEW", "REPORT_GENERATE", "ANALYTICS_ACCESS", "DATA_EXPORT"
            ).stream().map(permissionMap::get).filter(Objects::nonNull).toList();
            
            case "DISPATCHER" -> List.of(
                "VEHICLE_READ", "VIEW_LOCATION_LIVE", "ALERT_READ", "ALERT_ACKNOWLEDGE", "NOTIFICATION_SEND", 
                "EMERGENCY_ALERT", "ROUTE_PLANNING", "REPORT_VIEW"
            ).stream().map(permissionMap::get).filter(Objects::nonNull).toList();
            
            case "INSTALLER" -> List.of(
                "DEVICE_REGISTER", "DEVICE_ASSIGN", "DEVICE_ACTIVATE", "DEVICE_UPDATE", "VEHICLE_READ", "VEHICLE_ASSIGN_DEVICE"
            ).stream().map(permissionMap::get).filter(Objects::nonNull).toList();
            
            case "VIEWER" -> List.of(
                "VEHICLE_READ", "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "ALERT_READ", "REPORT_VIEW"
            ).stream().map(permissionMap::get).filter(Objects::nonNull).toList();
            
            default -> List.<Permission>of();
        };

        log.debug("Assigned {} permissions to role {}", rolePermissions.size(), roleName);
        return rolePermissions;
    }

    /**
     * Create SuperAdmin user with enhanced security
     */
    private User createSuperAdmin(List<Role> roles) {
        log.info("👤 Creating SuperAdmin user account");

        // Check if SuperAdmin already exists
        var existingSuperAdmin = userRepository.findByUsernameAndActiveTrue(superAdminUsername);
        if (existingSuperAdmin.isPresent()) {
            log.info("SuperAdmin user already exists: {}", superAdminUsername);
            return existingSuperAdmin.get();
        }

        var superAdminRole = roles.stream()
            .filter(role -> "SUPER_ADMIN".equals(role.getName()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("SuperAdmin role not found"));

        var superAdmin = User.builder()
            .username(superAdminUsername)
            .email(superAdminEmail)
            .firstName("Super")
            .lastName("Admin")
            .password(passwordEncoder.encode(superAdminPassword))
            .phoneNumber("+1234567890")
            .tenantId(UUID.fromString(defaultTenantId))
            .active(true)
            .roles(Set.of(superAdminRole))
            .createdBy(SYSTEM_USER)
            .createdAt(LocalDateTime.now())
            .build();

        var savedUser = userRepository.save(superAdmin);
        
        log.info("✅ SuperAdmin created successfully:");
        log.info("   Username: {}", superAdminUsername);
        log.info("   Email: {}", superAdminEmail);
        log.info("   Default Password: {} (Please change on first login)", superAdminPassword);
        
        return savedUser;
    }

    /**
     * Log current system statistics
     */
    private void logCurrentStats() {
        var permissionCount = permissionRepository.count();
        var roleCount = roleRepository.count();
        var userCount = userRepository.count();
        
        log.info("📊 Current RBAC Statistics:");
        log.info("   Permissions: {}", permissionCount);
        log.info("   Roles: {}", roleCount);
        log.info("   Users: {}", userCount);
    }

    /**
     * Log initialization completion with summary
     */
    private void logInitializationComplete() {
        log.info("🎉 RBAC Data Initialization Completed Successfully!");
        // log.info("=" * 60);
        logCurrentStats();
        // log.info("=" * 60);
        log.info("🔐 Default SuperAdmin Credentials:");
        log.info("   Username: {}", superAdminUsername);
        log.info("   Password: {}", superAdminPassword);
        log.info("   ⚠️  Please change the default password after first login!");
        // log.info("=" * 60);
    }

    /**
     * Modern record for permission definition
     */
    public record PermissionDefinition(
        String code,
        String name,
        String description,
        boolean requiresScope
    ) {}
}