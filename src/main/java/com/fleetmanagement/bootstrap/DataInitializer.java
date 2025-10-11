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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Initialization Component - COMPLETELY FIXED
 * Fixes:
 * 1. Added ROLE_MANAGEMENT to Permission enum
 * 2. Fixed role.setScopeType() instead of setRoleScope()  
 * 3. Fixed UUID handling for createdBy fields
 * 4. Proper role creation and SuperAdmin assignment
 */
@Component
@ConditionalOnProperty(name="app.data-init.enabled", havingValue="true", matchIfMissing=true)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final UUID tenantId = UUID.fromString("b0accb3b-37e2-4a80-ba00-583eefa8b664");
    private final UUID systemUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("🚀 Starting RBAC data initialization...");
            
            if (permissionRepository.count() == 0) {
                log.info("🏗️ Performing fresh initialization");
                createPermissions();
                createRoles();
                createSuperAdmin();
                log.info("✅ Data initialization completed successfully");
            } else {
                log.info("📊 Data already initialized, skipping...");
            }
        } catch (Exception e) {
            log.error("❌ Fatal error during initialization", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    /**
     * Create essential permissions - FIXED enum usage
     */
    private void createPermissions() {
        log.info("🔑 Creating essential permissions...");

        // System Administration
        createPermission("SUPER_ADMIN", "Super Administrator Access",
            "Complete system access with all permissions", Permission.PermissionCategory.SYSTEM_ADMINISTRATION, false);
        createPermission("SYSTEM_MAINTENANCE", "System Maintenance",
            "Core system operations and maintenance", Permission.PermissionCategory.SYSTEM_ADMINISTRATION, true);
        createPermission("MULTI_TENANT_MANAGE", "Multi-Tenant Management",
            "Cross-tenant operations", Permission.PermissionCategory.SYSTEM_ADMINISTRATION, true);

        // User Management
        createPermission("USER_CREATE", "Create User",
            "Create new users", Permission.PermissionCategory.USER_MANAGEMENT, true);
        createPermission("USER_READ", "Read User",
            "View user information", Permission.PermissionCategory.USER_MANAGEMENT, true);
        createPermission("USER_UPDATE", "Update User",
            "Modify user information", Permission.PermissionCategory.USER_MANAGEMENT, true);
        createPermission("USER_DELETE", "Delete User",
            "Delete users", Permission.PermissionCategory.USER_MANAGEMENT, true);
        createPermission("USER_RESET_PASSWORD", "Reset Password",
            "Reset user passwords", Permission.PermissionCategory.USER_MANAGEMENT, true);

        // Role Management - FIXED: Now using correct enum
        createPermission("ROLE_CREATE", "Create Role",
            "Create new roles", Permission.PermissionCategory.ROLE_MANAGEMENT, true);
        createPermission("ROLE_READ", "Read Roles",
            "View role definitions", Permission.PermissionCategory.ROLE_MANAGEMENT, true);
        createPermission("ROLE_UPDATE", "Update Role",
            "Modify role permissions", Permission.PermissionCategory.ROLE_MANAGEMENT, true);
        createPermission("ROLE_DELETE", "Delete Role",
            "Delete existing roles", Permission.PermissionCategory.ROLE_MANAGEMENT, true);
        createPermission("ROLE_ASSIGN", "Assign Roles",
            "Assign roles to users", Permission.PermissionCategory.ROLE_MANAGEMENT, true);

        // Device Management
        createPermission("DEVICE_READ", "Read Device",
            "View device information", Permission.PermissionCategory.DEVICE_MANAGEMENT, true);
        createPermission("DEVICE_REGISTER", "Register Device",
            "Add new devices", Permission.PermissionCategory.DEVICE_MANAGEMENT, true);
        createPermission("DEVICE_ASSIGN", "Assign Device",
            "Assign devices to vehicles/users", Permission.PermissionCategory.DEVICE_MANAGEMENT, true);
        createPermission("DEVICE_ACTIVATE", "Activate Device",
            "Enable/disable devices", Permission.PermissionCategory.DEVICE_MANAGEMENT, true);
        createPermission("DEVICE_REMOTE_CONFIG", "Remote Configuration",
            "Push configuration updates", Permission.PermissionCategory.DEVICE_MANAGEMENT, true);
        createPermission("DEVICE_BULK_OPERATIONS", "Bulk Operations",
            "Mass device operations", Permission.PermissionCategory.DEVICE_MANAGEMENT, true);

        // Vehicle Management
        createPermission("VEHICLE_READ", "Read Vehicle",
            "View vehicle details", Permission.PermissionCategory.VEHICLE_MANAGEMENT, true);
        createPermission("VEHICLE_CREATE", "Create Vehicle",
            "Add vehicles", Permission.PermissionCategory.VEHICLE_MANAGEMENT, true);
        createPermission("VEHICLE_UPDATE", "Update Vehicle",
            "Modify vehicle information", Permission.PermissionCategory.VEHICLE_MANAGEMENT, true);
        createPermission("VEHICLE_ASSIGN_DEVICE", "Assign Device to Vehicle",
            "Connect devices to vehicles", Permission.PermissionCategory.VEHICLE_MANAGEMENT, true);
        createPermission("FLEET_MANAGE", "Fleet Management",
            "Organize vehicle groups", Permission.PermissionCategory.VEHICLE_MANAGEMENT, true);

        // Location & Tracking
        createPermission("VIEW_LOCATION_LIVE", "Live Location",
            "Real-time tracking", Permission.PermissionCategory.LOCATION_TRACKING, true);
        createPermission("VIEW_LOCATION_HISTORY", "Location History",
            "Historical routes", Permission.PermissionCategory.LOCATION_TRACKING, true);
        createPermission("EXPORT_LOCATION", "Export Location",
            "Download location data", Permission.PermissionCategory.LOCATION_TRACKING, true);
        createPermission("GEOFENCE_MANAGE", "Geofence Management",
            "Create/edit boundaries", Permission.PermissionCategory.LOCATION_TRACKING, true);

        // Alerts & Notifications  
        createPermission("ALERT_READ", "Read Alerts",
            "View alerts", Permission.PermissionCategory.ALERTS_NOTIFICATIONS, true);
        createPermission("ALERT_MANAGE", "Manage Alerts",
            "Create/modify alert rules", Permission.PermissionCategory.ALERTS_NOTIFICATIONS, true);
        createPermission("NOTIFICATION_SEND", "Send Notifications",
            "Send messages", Permission.PermissionCategory.ALERTS_NOTIFICATIONS, true);

        // Reports & Analytics
        createPermission("REPORT_VIEW", "View Reports",
            "Access reports", Permission.PermissionCategory.REPORTS_ANALYTICS, true);
        createPermission("REPORT_GENERATE", "Generate Reports",
            "Create custom reports", Permission.PermissionCategory.REPORTS_ANALYTICS, true);
        createPermission("ANALYTICS_ACCESS", "Analytics Access",
            "Performance metrics", Permission.PermissionCategory.REPORTS_ANALYTICS, true);

        // Security & Compliance
        createPermission("AUDIT_READ", "Read Audit Logs",
            "View access logs", Permission.PermissionCategory.SECURITY_COMPLIANCE, true);
        createPermission("SYSTEM_CONFIG_READ", "System Configuration",
            "View configuration", Permission.PermissionCategory.SECURITY_COMPLIANCE, true);
        createPermission("BILLING_VIEW", "Billing Access",
            "Access billing information", Permission.PermissionCategory.SECURITY_COMPLIANCE, true);

        log.info("✅ Created {} permissions", permissionRepository.count());
    }

    /**
     * Create essential roles - FIXED field names
     */
    private void createRoles() {
        log.info("👥 Creating essential roles...");

        try {
            // SuperAdmin Role - All permissions
            Role superAdminRole = createRole("SuperAdmin", "Super Administrator with complete system access", RoleScope.GLOBAL);
            superAdminRole.setPermissions(Set.copyOf(permissionRepository.findAll()));
            roleRepository.save(superAdminRole);

            // TenantAdmin Role - Comprehensive tenant management
            Role tenantAdminRole = createRole("TenantAdmin", "Tenant administrator with user and resource management", RoleScope.TENANT);
            tenantAdminRole.setPermissions(getPermissionsByCode(
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE", "USER_RESET_PASSWORD",
                "ROLE_ASSIGN", "ROLE_READ", "ROLE_CREATE", "ROLE_DELETE",
                "DEVICE_READ", "DEVICE_REGISTER", "DEVICE_ASSIGN", "DEVICE_ACTIVATE",
                "VEHICLE_READ", "VEHICLE_CREATE", "VEHICLE_UPDATE", "VEHICLE_ASSIGN_DEVICE", "FLEET_MANAGE",
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "EXPORT_LOCATION", "GEOFENCE_MANAGE",
                "ALERT_READ", "ALERT_MANAGE", "NOTIFICATION_SEND",
                "REPORT_VIEW", "REPORT_GENERATE", "ANALYTICS_ACCESS",
                "AUDIT_READ", "BILLING_VIEW"
            ));
            roleRepository.save(tenantAdminRole);

            // FleetManager Role - Fleet operations
            Role fleetManagerRole = createRole("FleetManager", "Fleet manager with vehicle and tracking access", RoleScope.TENANT);
            fleetManagerRole.setPermissions(getPermissionsByCode(
                "VEHICLE_READ", "VEHICLE_UPDATE", "VEHICLE_ASSIGN_DEVICE", "FLEET_MANAGE",
                "DEVICE_READ", "DEVICE_ASSIGN",
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "EXPORT_LOCATION", "GEOFENCE_MANAGE",
                "ALERT_READ", "ALERT_MANAGE",
                "REPORT_VIEW", "REPORT_GENERATE", "ANALYTICS_ACCESS"
            ));
            roleRepository.save(fleetManagerRole);

            // Dispatcher Role - Operations focused
            Role dispatcherRole = createRole("Dispatcher", "Dispatcher with live tracking and communication", RoleScope.TENANT);
            dispatcherRole.setPermissions(getPermissionsByCode(
                "VIEW_LOCATION_LIVE", "ALERT_READ", "NOTIFICATION_SEND", "VEHICLE_READ"
            ));
            roleRepository.save(dispatcherRole);

            // Installer Role - Device installation
            Role installerRole = createRole("Installer", "Device installer with registration permissions", RoleScope.TENANT);
            installerRole.setPermissions(getPermissionsByCode(
                "DEVICE_REGISTER", "DEVICE_ASSIGN", "DEVICE_ACTIVATE", "VEHICLE_ASSIGN_DEVICE"
            ));
            roleRepository.save(installerRole);

            // Viewer Role - Read-only access
            Role viewerRole = createRole("Viewer", "Read-only access to tracking and reports", RoleScope.TENANT);
            viewerRole.setPermissions(getPermissionsByCode(
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "VEHICLE_READ", "REPORT_VIEW"
            ));
            roleRepository.save(viewerRole);

            log.info("✅ Created {} roles", roleRepository.count());
            
        } catch (Exception e) {
            log.error("❌ Error creating roles", e);
            throw e;
        }
    }

    /**
     * Create SuperAdmin user - FIXED role lookup
     */
    private void createSuperAdmin() {
        log.info("👤 Creating SuperAdmin user...");

        try {
            if (userRepository.findByUsernameAndActiveTrue("superadmin").isEmpty()) {
                // FIXED: Use correct role name
                Role superAdminRole = roleRepository.findByName("SuperAdmin")
                    .orElseThrow(() -> new RuntimeException("SuperAdmin role not found"));

                User superAdmin = User.builder()
                    .username("superadmin")
                    .email("superadmin@fleetmanagement.com")
                    .firstName("Super")
                    .lastName("Admin")
                    .password(passwordEncoder.encode("SuperAdmin@123"))
                    .phoneNumber("+1234567890")
                    .tenantId(tenantId)
                    .active(true)
                    .roles(Set.of(superAdminRole))
                    .createdBy(systemUserId)
                    .createdAt(LocalDateTime.now())
                    .build();

                userRepository.save(superAdmin);
                log.info("✅ SuperAdmin user created successfully with username: superadmin");
            } else {
                log.info("SuperAdmin user already exists");
            }
        } catch (Exception e) {
            log.error("❌ Error creating SuperAdmin user", e);
            throw e;
        }
    }

    /**
     * Helper method to create permission - FIXED parameters
     */
    private void createPermission(String code, String name, String description, 
                                  Permission.PermissionCategory category, boolean requiresScope) {
        try {
            Permission permission = Permission.builder()
                .code(code)
                .name(name)
                .description(description)
                .category(category)
                .active(true)
                .requiresScope(requiresScope)
                .createdBy(systemUserId)
                .createdAt(LocalDateTime.now())
                .build();
            permissionRepository.save(permission);
        } catch (Exception e) {
            log.warn("⚠️ Failed to create permission: {} - {}", code, e.getMessage());
        }
    }

    /**
     * Helper method to create role - FIXED setScopeType
     */
    private Role createRole(String name, String description, RoleScope roleScope) {
        return Role.builder()
            .name(name)
            .description(description)
            .active(true)
            .tenantId(tenantId)
            .roleScope(roleScope)
            .createdBy(systemUserId)
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * Helper method to get permissions by code
     */
    private Set<Permission> getPermissionsByCode(String... codes) {
        return Set.copyOf(permissionRepository.findByCodeIn(Set.of(codes)));
    }
}