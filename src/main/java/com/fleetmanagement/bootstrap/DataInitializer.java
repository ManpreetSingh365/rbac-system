package com.fleetmanagement.bootstrap;

import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.User;
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

import java.util.Set;
import java.util.UUID;

/**
 * Data Initialization Component - FIXED
 * Creates essential permissions, roles, and SuperAdmin user on startup
 * FIXED: Username field was null in SuperAdmin creation
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

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data initialization...");

        if (permissionRepository.count() == 0) {
            createPermissions();
            createRoles();
            createSuperAdmin();
            log.info("Data initialization completed successfully");
        } else {
            log.info("Data already initialized, skipping...");
        }
    }

    /**
     * Create essential permissions based on streamlined RBAC model
     */
    private void createPermissions() {
        log.info("Creating essential permissions...");

        // System Administration
        createPermission("SUPER_ADMIN", "Super Administrator Access",
                "Complete system access with all permissions", Permission.PermissionCategory.SYSTEM_ADMINISTRATION);
        createPermission("SYSTEM_MAINTENANCE", "System Maintenance",
                "Core system operations and maintenance", Permission.PermissionCategory.SYSTEM_ADMINISTRATION);
        createPermission("MULTI_TENANT_MANAGE", "Multi-Tenant Management",
                "Cross-tenant operations", Permission.PermissionCategory.SYSTEM_ADMINISTRATION);

        // User Management
        createPermission("USER_CREATE", "Create User",
                "Create new users", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("USER_READ", "Read User",
                "View user information", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("USER_UPDATE", "Update User",
                "Modify user information", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("USER_DELETE", "Delete User",
                "Delete users", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("USER_RESET_PASSWORD", "Reset Password",
                "Reset user passwords", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("ROLE_ASSIGN", "Assign Roles",
                "Assign roles to users", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("ROLE_READ", "Read Roles",
                "View role definitions", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("ROLE_CREATE", "Create Role",
                "Create new roles", Permission.PermissionCategory.USER_MANAGEMENT);
        createPermission("ROLE_DELETE", "Delete Role",
                "Delete existing roles", Permission.PermissionCategory.USER_MANAGEMENT);

        // Device Management
        createPermission("DEVICE_READ", "Read Device",
                "View device information", Permission.PermissionCategory.DEVICE_MANAGEMENT);
        createPermission("DEVICE_REGISTER", "Register Device",
                "Add new devices", Permission.PermissionCategory.DEVICE_MANAGEMENT);
        createPermission("DEVICE_ASSIGN", "Assign Device",
                "Assign devices to vehicles/users", Permission.PermissionCategory.DEVICE_MANAGEMENT);
        createPermission("DEVICE_ACTIVATE", "Activate Device",
                "Enable/disable devices", Permission.PermissionCategory.DEVICE_MANAGEMENT);
        createPermission("DEVICE_REMOTE_CONFIG", "Remote Configuration",
                "Push configuration updates", Permission.PermissionCategory.DEVICE_MANAGEMENT);
        createPermission("DEVICE_BULK_OPERATIONS", "Bulk Operations",
                "Mass device operations", Permission.PermissionCategory.DEVICE_MANAGEMENT);

        // Vehicle Management
        createPermission("VEHICLE_READ", "Read Vehicle",
                "View vehicle details", Permission.PermissionCategory.VEHICLE_MANAGEMENT);
        createPermission("VEHICLE_CREATE", "Create Vehicle",
                "Add vehicles", Permission.PermissionCategory.VEHICLE_MANAGEMENT);
        createPermission("VEHICLE_UPDATE", "Update Vehicle",
                "Modify vehicle information", Permission.PermissionCategory.VEHICLE_MANAGEMENT);
        createPermission("VEHICLE_ASSIGN_DEVICE", "Assign Device to Vehicle",
                "Connect devices to vehicles", Permission.PermissionCategory.VEHICLE_MANAGEMENT);
        createPermission("FLEET_MANAGE", "Fleet Management",
                "Organize vehicle groups", Permission.PermissionCategory.VEHICLE_MANAGEMENT);

        // Location & Tracking
        createPermission("VIEW_LOCATION_LIVE", "Live Location",
                "Real-time tracking", Permission.PermissionCategory.LOCATION_TRACKING);
        createPermission("VIEW_LOCATION_HISTORY", "Location History",
                "Historical routes", Permission.PermissionCategory.LOCATION_TRACKING);
        createPermission("EXPORT_LOCATION", "Export Location",
                "Download location data", Permission.PermissionCategory.LOCATION_TRACKING);
        createPermission("GEOFENCE_MANAGE", "Geofence Management",
                "Create/edit boundaries", Permission.PermissionCategory.LOCATION_TRACKING);

        // Alerts & Notifications
        createPermission("ALERT_READ", "Read Alerts",
                "View alerts", Permission.PermissionCategory.ALERTS_NOTIFICATIONS);
        createPermission("ALERT_MANAGE", "Manage Alerts",
                "Create/modify alert rules", Permission.PermissionCategory.ALERTS_NOTIFICATIONS);
        createPermission("NOTIFICATION_SEND", "Send Notifications",
                "Send messages", Permission.PermissionCategory.ALERTS_NOTIFICATIONS);

        // Reports & Analytics
        createPermission("REPORT_VIEW", "View Reports",
                "Access reports", Permission.PermissionCategory.REPORTS_ANALYTICS);
        createPermission("REPORT_GENERATE", "Generate Reports",
                "Create custom reports", Permission.PermissionCategory.REPORTS_ANALYTICS);
        createPermission("ANALYTICS_ACCESS", "Analytics Access",
                "Performance metrics", Permission.PermissionCategory.REPORTS_ANALYTICS);

        // Security & Compliance
        createPermission("AUDIT_READ", "Read Audit Logs",
                "View access logs", Permission.PermissionCategory.SECURITY_COMPLIANCE);
        createPermission("SYSTEM_CONFIG_READ", "System Configuration",
                "View configuration", Permission.PermissionCategory.SECURITY_COMPLIANCE);
        createPermission("BILLING_VIEW", "Billing Access",
                "Access billing information", Permission.PermissionCategory.SECURITY_COMPLIANCE);

        log.info("Created {} permissions", permissionRepository.count());
    }

    /**
     * Create essential roles with optimized permissions
     */
    private void createRoles() {
        log.info("Creating essential roles...");

        // SuperAdmin Role - All permissions
        Role superAdminRole = createRole("SuperAdmin", "Super Administrator with complete system access");
        superAdminRole.setPermissions(Set.copyOf(permissionRepository.findAll()));
        roleRepository.save(superAdminRole);

        // TenantAdmin Role - Comprehensive tenant management
        Role tenantAdminRole = createRole("TenantAdmin", "Tenant administrator with user and resource management");
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
        Role fleetManagerRole = createRole("FleetManager", "Fleet manager with vehicle and tracking access");
        fleetManagerRole.setPermissions(getPermissionsByCode(
                "VEHICLE_READ", "VEHICLE_UPDATE", "VEHICLE_ASSIGN_DEVICE", "FLEET_MANAGE",
                "DEVICE_READ", "DEVICE_ASSIGN",
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "EXPORT_LOCATION", "GEOFENCE_MANAGE",
                "ALERT_READ", "ALERT_MANAGE",
                "REPORT_VIEW", "REPORT_GENERATE", "ANALYTICS_ACCESS"
        ));
        roleRepository.save(fleetManagerRole);

        // Dispatcher Role - Operations focused
        Role dispatcherRole = createRole("Dispatcher", "Dispatcher with live tracking and communication");
        dispatcherRole.setPermissions(getPermissionsByCode(
                "VIEW_LOCATION_LIVE", "ALERT_READ", "NOTIFICATION_SEND", "VEHICLE_READ"
        ));
        roleRepository.save(dispatcherRole);

        // Installer Role - Device installation
        Role installerRole = createRole("Installer", "Device installer with registration permissions");
        installerRole.setPermissions(getPermissionsByCode(
                "DEVICE_REGISTER", "DEVICE_ASSIGN", "DEVICE_ACTIVATE", "VEHICLE_ASSIGN_DEVICE"
        ));
        roleRepository.save(installerRole);

        // Viewer Role - Read-only access
        Role viewerRole = createRole("Viewer", "Read-only access to tracking and reports");
        viewerRole.setPermissions(getPermissionsByCode(
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY", "VEHICLE_READ", "REPORT_VIEW"
        ));
        roleRepository.save(viewerRole);

        log.info("Created {} roles", roleRepository.count());
    }

    /**
     * Create SuperAdmin user - FIXED: Added username field
     */
    private void createSuperAdmin() {
        log.info("Creating SuperAdmin user...");

        // FIXED: Check by username instead of email
        if (userRepository.findByUsernameAndActiveTrue("superadmin").isEmpty()) {
            Role superAdminRole = roleRepository.findByName("SuperAdmin").orElseThrow();                
            User superAdmin = User.builder()
                    .username("superadmin")  // FIXED: Added missing username field
                    .email("superadmin@fleetmanagement.com")
                    .firstName("Super")
                    .lastName("Admin")
                    .password(passwordEncoder.encode("SuperAdmin@123"))
                    .phoneNumber("+1234567890")
                    .tenantId(tenantId)
                    .active(true)
                    .roles(Set.of(superAdminRole))
                    .build();

            userRepository.save(superAdmin);
            log.info("SuperAdmin user created successfully with username: superadmin");
        } else {
            log.info("SuperAdmin user already exists");
        }
    }

    /**
     * Helper method to create permission
     */
    private void createPermission(String code, String name, String description, Permission.PermissionCategory category) {
        Permission permission = Permission.builder()
                .code(code)
                .name(name)
                .description(description)
                .category(category)                
                .active(true)
                .requiresScope(!"SUPER_ADMIN".equals(code))
                .build();

        permissionRepository.save(permission);
    }

    /**
     * Helper method to create role
     */
    private Role createRole(String name, String description) {
        return Role.builder()
                .name(name)
                .description(description)
                .active(true)
                .tenantId(tenantId)
                .scopeType(Role.ScopeType.TENANT)                
                .build();
    }

    /**
     * Helper method to get permissions by code
     */
    private Set<Permission> getPermissionsByCode(String... codes) {
        return Set.copyOf(permissionRepository.findByCodeIn(Set.of(codes)));
    }
}