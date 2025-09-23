package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.CreateUserRequest;
import com.fleetmanagement.dto.request.UpdateUserRequest;
import com.fleetmanagement.dto.response.UserLoginResponse;
import com.fleetmanagement.dto.response.UserResponse;
import com.fleetmanagement.service.UserService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
// import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

/**
 * User Management Controller
 * Handles user CRUD operations with role-based access control
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
// @Tag(name = "User Management", description = "User CRUD operations with RBAC")
// @SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    // @Operation(summary = "Create new user", description = "Create user with roles and assignments")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserLoginResponse currentUser) {

        UUID currentUserId = currentUser.getId();
        log.info("API call: CreateUser by {}", currentUserId);
        UserResponse response = userService.createUser(request, currentUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    // @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserLoginResponse currentUser) {

        UUID currentUserId = currentUser.getId();
        log.info("API call: GetUser {} by {}", userId, currentUserId);
        UserResponse response = userService.getUserById(userId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) UUID tenantId,
            @AuthenticationPrincipal UserLoginResponse currentUser,
            Pageable pageable) {
        
        UUID currentUserId = currentUser.getId(); // Add getId() in CustomUserDetails
        log.info("API call: ListUsers tenant:{} by {}", tenantId, currentUserId);
        Page<UserResponse> page = userService.getAllUsers(tenantId, currentUserId, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{userId}")
    // @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
            
        UUID currentUserId = currentUser.getId();
        log.info("API call: UpdateUser {} by {}", userId, currentUserId);
        UserResponse response = userService.updateUser(userId, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    // @Operation(summary = "Delete user", description = "Soft delete user by ID")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserLoginResponse currentUser) {

        UUID currentUserId = currentUser.getId();
        log.info("API call: DeleteUser {} by {}", userId, currentUserId);
        userService.deleteUser(userId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    // @Operation(summary = "Assign roles", description = "Assign roles to a user")
    public ResponseEntity<UserResponse> assignRoles(
            @PathVariable UUID userId,
            @RequestBody Set<UUID> roleIds,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
        
        UUID currentUserId = currentUser.getId();
        log.info("API call: AssignRoles {} to {} by {}", roleIds, userId, currentUserId);
        UserResponse response = userService.assignRoles(userId, roleIds, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/devices")
    // @Operation(summary = "Assign devices", description = "Assign devices to a user")
    public ResponseEntity<UserResponse> assignDevices(
            @PathVariable UUID userId,
            @RequestBody Set<UUID> deviceIds,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
        
        UUID currentUserId = currentUser.getId();
        log.info("API call: AssignDevices {} to {} by {}", deviceIds, userId, currentUserId);
        UserResponse response = userService.assignDevices(userId, deviceIds, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/vehicles")
    // @Operation(summary = "Assign vehicles", description = "Assign vehicles to a user")
    public ResponseEntity<UserResponse> assignVehicles(
            @PathVariable UUID userId,
            @RequestBody Set<UUID> vehicleIds,
            @AuthenticationPrincipal UserLoginResponse currentUser) {

        UUID currentUserId = currentUser.getId();
        log.info("API call: AssignVehicles {} to {} by {}", vehicleIds, userId, currentUserId);
        UserResponse response = userService.assignVehicles(userId, vehicleIds, currentUserId);
        return ResponseEntity.ok(response);
    }
}