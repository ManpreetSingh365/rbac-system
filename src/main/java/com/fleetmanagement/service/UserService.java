package com.fleetmanagement.service;

import com.fleetmanagement.dto.request.CreateUserRequest;
import com.fleetmanagement.dto.request.UpdateUserRequest;
import com.fleetmanagement.dto.response.UserResponse;
import com.fleetmanagement.entity.Device;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.entity.Vehicle;
import com.fleetmanagement.exception.ResourceNotFoundException;
import com.fleetmanagement.mapper.UserMapper;
import com.fleetmanagement.repository.DeviceRepository;
import com.fleetmanagement.repository.RoleRepository;
import com.fleetmanagement.repository.UserRepository;
import com.fleetmanagement.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Service - Handles user management operations
 * Updated: Removed all caching, implemented proper business logic
 * Follows recommended Spring Boot best practices for scalability
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeviceRepository deviceRepository;
    private final VehicleRepository vehicleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;
    private final UserMapper userMapper;

    /**
     * Create new user with roles and assignments
     * SuperAdmin can create any user, Admin can create users with limited scope
     * Business Logic: Validates permissions, checks uniqueness, assigns relationships
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID currentUserId, UUID tenantId) {
        log.debug("Creating user with username: {} by user: {}", request.getUsername(), currentUserId);

        // Step 1: Validate current user permissions
        validateUserCreationPermissions(currentUserId, tenantId);
        

        log.debug("GOT READ PERMISSIONS");

        // Step 2: Business validation - Check uniqueness
        validateUserUniqueness(request.getUsername(), request.getEmail());

        // Step 3: Create and populate user entity
        User user = userMapper.toEntity(request);

        user.setTenantId(tenantId);

        // Step 7: Save user and return response
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {} and username: {}", savedUser.getId(), savedUser.getUsername());

        return userMapper.toResponseDto(savedUser);
    }

    /**
     * Get user by ID with business logic validation
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId, UUID currentUserId, UUID tenantId) {
        log.debug("Fetching user with ID: {} by user: {}", userId, currentUserId);

        // Validate permission to read user
        validateUserReadPermissions(currentUserId, tenantId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if current user can view this user (scope validation)
        if (!canAccessUser(currentUserId, user)) {
            throw new SecurityException("Access denied to user: " + userId);
        }

        return userMapper.toResponseDto(user);
    }

    /**
     * Get all users with business logic filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(UUID tenantId, UUID currentUserId, Pageable pageable) {
        log.debug("Fetching users by {} with pagination", currentUserId);

        // Validate permission to read users

        validateUserReadPermissions(currentUserId, tenantId);

        log.debug("GOT USER READ PERMISSION : SERVICE");

        Page<User> users;


        // Regular admin can only see users in their tenant
        UUID currentUserTenantId = getCurrentUserTenantId(currentUserId);
        log.debug("GOT TENANT ID : {}", tenantId);
        log.debug("GOT CURRENT TENANT ID : {}", currentUserTenantId);
        UUID targetTenantId = tenantId != null ? tenantId : currentUserTenantId;

        if (!currentUserTenantId.equals(targetTenantId)) {
            throw new SecurityException("Access denied to tenant: " + targetTenantId);
        }

        users = userRepository.findAllWithRelations(tenantId, pageable);

        log.debug("FINAL STAGE");
        return users.map(userMapper::toResponseDto);
    }

    /**
     * Update user information with business logic validation
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, UUID currentUserId) {
        log.info("Updating user with ID: {} by user: {}", userId, currentUserId);

        User existingUser = userRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate permissions and business rules
        validateUserUpdatePermissions(currentUserId, existingUser.getTenantId());
        validateUserUpdateBusiness(existingUser, request, currentUserId);

        // Update user fields with business logic
        userMapper.updateEntityFromDto(request, existingUser);

        User savedUser = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", savedUser.getId());

        return userMapper.toResponseDto(savedUser);
    }

    /**
     * Soft delete user with business validation
     */
    @Transactional
    public void deleteUser(UUID userId, UUID currentUserId) {
        log.info("Deleting user with ID: {} by user: {}", userId, currentUserId);

        User user = userRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Business validation - cannot delete self
        if (userId.equals(currentUserId)) {
            throw new IllegalStateException("Cannot delete your own account");
        }

        // Validate permissions
        validateUserDeletePermissions(currentUserId, user.getTenantId());

        // Business logic - check if user has critical assignments
        validateUserDeletion(user);

        // Soft delete
        user.setActive(false);
        userRepository.save(user);

        log.info("User soft deleted successfully with ID: {}", userId);
    }

    /**
     * Assign roles to user with comprehensive business validation
     */
    @Transactional
    public UserResponse assignRoles(UUID userId, Set<UUID> roleIds, UUID currentUserId) {
        log.info("Assigning {} roles to user ID: {} by user: {}", roleIds.size(), userId, currentUserId);

        User user = userRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate permissions
        validateRoleAssignmentPermissions(currentUserId, user.getTenantId());

        // Validate and get roles with business logic
        Set<Role> validRoles = validateAndGetRoles(roleIds, currentUserId);

        // Business logic - prevent assigning higher privilege roles
        validateRoleAssignmentHierarchy(currentUserId, validRoles);

        user.setRoles(validRoles);
        User savedUser = userRepository.save(user);

        log.info("Roles assigned successfully to user ID: {}", userId);
        return userMapper.toResponseDto(savedUser);
    }

    /**
     * Assign devices to user with business validation
     */
    @Transactional
    public UserResponse assignDevices(UUID userId, Set<UUID> deviceIds, UUID currentUserId) {
        log.debug("Assigning {} devices to user ID: {} by user: {}", deviceIds.size(), userId, currentUserId);

        User user = userRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        validateDeviceAssignmentPermissions(currentUserId, user.getTenantId());

        log.debug("VALID DEVICES: DONE ");

        Set<Device> validDevices = validateAndGetDevices(deviceIds, user.getTenantId());

        // Business logic - check device availability and conflicts
        validateDeviceAssignments(validDevices, userId);

        log.debug("VALID DEVICES: {}", validDevices.size());

        user.setDevices(validDevices);
        log.debug("VALID DEVICES: SET");
        User savedUser = userRepository.save(user);

        log.debug("VALID DEVICES: SAVED");

        log.info("Devices assigned successfully to user ID: {}", userId);
        return userMapper.toResponseDto(savedUser);
    }

    /**
     * Assign vehicles to user with business validation
     */
    @Transactional
    public UserResponse assignVehicles(UUID userId, Set<UUID> vehicleIds, UUID currentUserId) {
        log.info("Assigning {} vehicles to user ID: {} by user: {}", vehicleIds.size(), userId, currentUserId);

        User user = userRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        validateVehicleAssignmentPermissions(currentUserId, user.getTenantId());

        Set<Vehicle> validVehicles = validateAndGetVehicles(vehicleIds, user.getTenantId());

        // Business logic - validate vehicle assignments
        validateVehicleAssignments(validVehicles, userId);

        user.setVehicles(validVehicles);
        User savedUser = userRepository.save(user);

        log.info("Vehicles assigned successfully to user ID: {}", userId);
        return userMapper.toResponseDto(savedUser);
    }
    public Boolean resetPassword(String email, String oldPassword, String newPassword){
        return true;
    }

    // =================== PRIVATE HELPER METHODS ===================

    

    private void validateUserUniqueness(String username, String email) {
        if (userRepository.existsByUsernameAndActiveTrue(username)) {
            throw new IllegalArgumentException("User with username already exists: " + username);
        }

        if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email already exists: " + email);
        }
    }

    private Set<Role> validateAndGetRoles(Set<UUID> roleIds, UUID currentUserId) {
        Set<Role> roles = new HashSet<>();

        for (UUID roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

            if (!role.isActive()) {
                throw new IllegalStateException("Cannot assign inactive role: " + role.getName());
            }

            // Business logic - validate role assignment permissions
            if (!canAssignRole(currentUserId, role)) {
                throw new SecurityException("Cannot assign role: " + role.getName());
            }

            roles.add(role);
        }

        return roles;
    }

    private Set<Device> validateAndGetDevices(Set<UUID> deviceIds, UUID tenantId) {
        return deviceIds.stream()
                .map(deviceId -> {
                    Device device = deviceRepository.findById(deviceId)
                            .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

                    // Business validation - tenant scope
                    if (!device.getTenantId().equals(tenantId)) {
                        throw new SecurityException("Device does not belong to the same tenant");
                    }

                    if (device.getStatus() == Device.DeviceStatus.DECOMMISSIONED) {
                        throw new IllegalStateException("Cannot assign decommissioned device: " + device.getImei());
                    }

                    return device;
                })
                .collect(Collectors.toSet());
    }

    private Set<Vehicle> validateAndGetVehicles(Set<UUID> vehicleIds, UUID tenantId) {
        return vehicleIds.stream()
                .map(vehicleId -> {
                    Vehicle vehicle = vehicleRepository.findById(vehicleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

                    // Business validation - tenant scope
                    if (!vehicle.getTenantId().equals(tenantId)) {
                        throw new SecurityException("Vehicle does not belong to the same tenant");
                    }

                    if (vehicle.getStatus() == Vehicle.VehicleStatus.RETIRED) {
                        throw new IllegalStateException("Cannot assign retired vehicle: " + vehicle.getLicensePlate());
                    }

                    return vehicle;
                })
                .collect(Collectors.toSet());
    }

    private void updateUserFields(User existingUser, UpdateUserRequest request) {
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPhoneNumber(request.getPhoneNumber());

        // Update email only if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            existingUser.setEmail(request.getEmail());
        }

        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setFullName(user.getFullName());
        return response;
    }

    // =================== VALIDATION METHODS ===================

    private void validateUserCreationPermissions(UUID currentUserId, UUID targetTenantId) {
        if (!permissionService.hasPermission(currentUserId, "USER_CREATE", targetTenantId)) {
            throw new SecurityException("Insufficient permissions to create user");
        }
    }

    private void validateUserReadPermissions(UUID currentUserId, UUID targetUserId) {
        if (!permissionService.hasPermission(currentUserId, "USER_READ", targetUserId)) {
            throw new SecurityException("Insufficient permissions to read user");
        }
    }

    private void validateUserUpdatePermissions(UUID currentUserId, UUID targetTenantId) {
        if (!permissionService.hasPermission(currentUserId, "USER_UPDATE", targetTenantId)) {
            throw new SecurityException("Insufficient permissions to update user");
        }
    }

    private void validateUserDeletePermissions(UUID currentUserId, UUID targetTenantId) {
        if (!permissionService.hasPermission(currentUserId, "USER_DELETE", targetTenantId)) {
            throw new SecurityException("Insufficient permissions to delete user");
        }
    }

    private void validateRoleAssignmentPermissions(UUID currentUserId, UUID targetTenantId) {
        if (!permissionService.hasPermission(currentUserId, "ROLE_ASSIGN", targetTenantId)) {
            throw new SecurityException("Insufficient permissions to assign roles");
        }
    }

    private void validateDeviceAssignmentPermissions(UUID currentUserId, UUID targetTenantId) {
        if (!permissionService.hasPermission(currentUserId, "DEVICE_ASSIGN", targetTenantId)) {
            throw new SecurityException("Insufficient permissions to assign devices");
        }
    }

    private void validateVehicleAssignmentPermissions(UUID currentUserId, UUID targetTenantId) {
        if (!permissionService.hasPermission(currentUserId, "VEHICLE_ASSIGN_DEVICE", targetTenantId)) {
            throw new SecurityException("Insufficient permissions to assign vehicles");
        }
    }

    // =================== BUSINESS LOGIC METHODS ===================

    private boolean canAccessUser(UUID currentUserId, User targetUser) {
        // SuperAdmin can access all users
        if (permissionService.hasPermission(currentUserId, "SUPER_ADMIN", null)) {
            return true;
        }

        // Users can access users in their tenant
        UUID currentUserTenantId = getCurrentUserTenantId(currentUserId);
        return currentUserTenantId.equals(targetUser.getTenantId());
    }

    private boolean canAssignRole(UUID currentUserId, Role role) {
        // SuperAdmin can assign any role
        if (permissionService.hasPermission(currentUserId, "SUPER_ADMIN", null)) {
            return true;
        }

        // Cannot assign SuperAdmin role unless you are SuperAdmin
        if (role.getPermissions().stream().anyMatch(p -> "SUPER_ADMIN".equals(p.getCode()))) {
            return false;
        }

        // Can assign roles within same tenant
        UUID currentUserTenantId = getCurrentUserTenantId(currentUserId);
        return currentUserTenantId.equals(role.getTenantId());
    }

    private void validateUserUpdateBusiness(User existingUser, UpdateUserRequest request, UUID currentUserId) {

        // Email uniqueness check if updating
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
        }
    }

    private void validateUserDeletion(User user) {
        // Check for critical assignments or business rules
        long roleCount = user.getRoles() != null ? user.getRoles().size() : 0;
        long deviceCount = user.getDevices() != null ? user.getDevices().size() : 0;
        long vehicleCount = user.getVehicles() != null ? user.getVehicles().size() : 0;

        log.info("User {} has {} roles, {} devices, {} vehicles assigned",
                user.getUsername(), roleCount, deviceCount, vehicleCount);
    }

    private void validateRoleAssignmentHierarchy(UUID currentUserId, Set<Role> roles) {
        // Business logic to prevent privilege escalation
        boolean hasElevatedRoles = roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> "SUPER_ADMIN".equals(permission.getCode()));

        if (hasElevatedRoles && !permissionService.hasPermission(currentUserId, "SUPER_ADMIN", null)) {
            throw new SecurityException("Cannot assign elevated privilege roles");
        }
    }

    private void validateDeviceAssignments(Set<Device> devices, UUID userId) {
        // Business logic - check for conflicts, limits, etc.
        devices.forEach(device -> {
            if (device.getUsers() != null && device.getUsers().size() >= 10) {
                throw new IllegalStateException("Device " + device.getImei() + " has reached maximum user assignments");
            }
        });
    }

    private void validateVehicleAssignments(Set<Vehicle> vehicles, UUID userId) {
        // Business logic - check for conflicts, limits, etc.
        vehicles.forEach(vehicle -> {
            if (vehicle.getUsers() != null && vehicle.getUsers().size() >= 5) {
                throw new IllegalStateException("Vehicle " + vehicle.getLicensePlate() + " has reached maximum user assignments");
            }
        });
    }

    private UUID getCurrentUserTenantId(UUID userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return currentUser.getTenantId();
    }
}