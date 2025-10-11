package com.fleetmanagement.bootstrap;

import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.type.RoleScope;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.repository.PermissionRepository;
import com.fleetmanagement.repository.RoleRepository;
import com.fleetmanagement.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- 1️⃣ Master list of permissions ----
    private static final List<String> ALL_PERMISSIONS = List.of(
            "VEHICLE_ASSIGN_DEVICE", "BILLING_VIEW", "DEVICE_READ", "REPORT_GENERATE", "SYSTEM_CONFIG_READ",
            "VIEW_LOCATION_HISTORY", "ROLE_DELETE", "DEVICE_ACTIVATE", "USER_CREATE", "USER_RESET_PASSWORD",
            "VEHICLE_READ", "GEOFENCE_MANAGE", "VIEW_LOCATION_LIVE", "AUDIT_READ", "ROLE_ASSIGN",
            "DEVICE_ASSIGN", "USER_READ", "DEVICE_REMOTE_CONFIG", "ROLE_READ", "SYSTEM_MAINTENANCE",
            "SUPER_ADMIN", "VEHICLE_UPDATE", "MULTI_TENANT_MANAGE", "USER_DELETE", "DEVICE_REGISTER",
            "ALERT_READ", "USER_UPDATE", "DEVICE_BULK_OPERATIONS", "NOTIFICATION_SEND", "VEHICLE_CREATE",
            "ALERT_MANAGE", "ANALYTICS_ACCESS", "ROLE_CREATE", "FLEET_MANAGE", "REPORT_VIEW", "EXPORT_LOCATION"
    );

    // ---- 2️⃣ Initialize data ----
    @PostConstruct
    @Transactional
    public void init() {
        Map<String, Permission> permissionsMap = createPermissions();
        Map<String, Role> rolesMap = createRoles();
        syncRolePermissions(rolesMap, permissionsMap);
        createSuperAdminUser(rolesMap.get("SuperAdmin"));
    }

    // ---- 3️⃣ Create all permissions (idempotent) ----
    private Map<String, Permission> createPermissions() {
        return ALL_PERMISSIONS.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseGet(() -> {
                            Permission permission = Permission.builder()
                                    .code(code)
                                    .name(code.replace("_", " ").toLowerCase())
                                    .description("Permission for " + code.replace("_", " ").toLowerCase())
                                    .build();
                            permissionRepository.save(permission);
                            log.info("✅ Created permission: {}", code);
                            return permission;
                        }))
                .collect(Collectors.toMap(Permission::getCode, p -> p));
    }

    // ---- 4️⃣ Create roles (idempotent) ----
    private Map<String, Role> createRoles() {
        return Map.of(
                "SuperAdmin", createRoleIfMissing("SuperAdmin", "Full system access for all tenants", RoleScope.GLOBAL),
                "TenantAdmin", createRoleIfMissing("TenantAdmin", "Manages tenant-level configuration and users", RoleScope.TENANT),
                "FleetManager", createRoleIfMissing("FleetManager", "Manages fleet, devices, and vehicles under a tenant", RoleScope.TENANT),
                "Operator", createRoleIfMissing("Operator", "Performs operational tasks like tracking, reports, and alerts", RoleScope.TENANT),
                "Viewer", createRoleIfMissing("Viewer", "View-only access to reports, vehicles, and analytics", RoleScope.TENANT)
        );
    }

    private Role createRoleIfMissing(String name, String description, RoleScope scope) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(name)
                            .description(description)
                            .roleScope(scope)
                            .build();
                    roleRepository.save(role);
                    log.info("✅ Created role: {}", name);
                    return role;
                });
    }

    // ---- 5️⃣ Sync permissions per role ----
    private void syncRolePermissions(Map<String, Role> rolesMap, Map<String, Permission> permissionsMap) {
        // SuperAdmin → all permissions
        updateRolePermissions(rolesMap.get("SuperAdmin"), Set.copyOf(permissionsMap.values()));

        // TenantAdmin → system + user + device management
        updateRolePermissions(rolesMap.get("TenantAdmin"), getPermissions(permissionsMap,
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
                "DEVICE_READ", "DEVICE_REGISTER", "DEVICE_ASSIGN", "DEVICE_ACTIVATE",
                "VEHICLE_CREATE", "VEHICLE_UPDATE", "VEHICLE_READ", "ROLE_ASSIGN",
                "ROLE_READ", "REPORT_VIEW", "BILLING_VIEW", "ALERT_MANAGE"
        ));

        // FleetManager → operational and reporting
        updateRolePermissions(rolesMap.get("FleetManager"), getPermissions(permissionsMap,
                "DEVICE_READ", "VEHICLE_READ", "VIEW_LOCATION_LIVE",
                "VIEW_LOCATION_HISTORY", "REPORT_VIEW", "REPORT_GENERATE",
                "EXPORT_LOCATION", "GEOFENCE_MANAGE", "ALERT_READ", "ALERT_MANAGE"
        ));

        // Operator → limited operational access
        updateRolePermissions(rolesMap.get("Operator"), getPermissions(permissionsMap,
                "VIEW_LOCATION_LIVE", "VIEW_LOCATION_HISTORY",
                "REPORT_VIEW", "ALERT_READ"
        ));

        // Viewer → read-only
        updateRolePermissions(rolesMap.get("Viewer"), getPermissions(permissionsMap,
                "VEHICLE_READ", "DEVICE_READ", "REPORT_VIEW", "ANALYTICS_ACCESS"
        ));
    }

    private void updateRolePermissions(Role role, Set<Permission> expected) {
        if (role == null) return;
        if (!role.getPermissions().equals(expected)) {
            role.setPermissions(expected);
            roleRepository.save(role);
            log.info("🔄 Synced {} with {} permissions", role.getName(), expected.size());
        } else {
            log.info("{} already up-to-date ({} permissions)", role.getName(), expected.size());
        }
    }

    private Set<Permission> getPermissions(Map<String, Permission> map, String... codes) {
        return Set.of(codes).stream()
                .map(map::get)
                .filter(p -> p != null)
                .collect(Collectors.toSet());
    }

    // ---- 6️⃣ Create default SuperAdmin user ----
    private void createSuperAdminUser(Role superAdminRole) {
        if (superAdminRole == null) return;

        if (userRepository.findByEmailIgnoreCase("superadmin@system.com").isEmpty()) {
            User user = User.builder()
                    .email("superadmin@system.com")
                    .username("superadmin")
                    .password(passwordEncoder.encode("admin123"))
                    .active(true)
                    .roles(Set.of(superAdminRole))
                    .build();
            userRepository.save(user);
            log.info("✅ Created default SuperAdmin user: superadmin@system.com");
        }
    }
}