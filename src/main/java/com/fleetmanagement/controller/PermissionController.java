package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.PermissionRequestDto;
import com.fleetmanagement.dto.response.PermissionResponseDto;
import com.fleetmanagement.dto.response.UserLoginResponse;
import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.service.PermissionManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing permissions
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionManagementService permissionManagementService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PERMISSION_CREATE')")
    public ResponseEntity<PermissionResponseDto> createPermission(
            @Valid @RequestBody PermissionRequestDto requestDto,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID userId = currentUser.getId();
        PermissionResponseDto responseDto = permissionManagementService.createPermission(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PERMISSION_UPDATE')")
    public ResponseEntity<PermissionResponseDto> updatePermission(
            @PathVariable UUID id,
            @Valid @RequestBody PermissionRequestDto requestDto,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID userId = currentUser.getId();
        PermissionResponseDto responseDto = permissionManagementService.updatePermission(userId, id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PERMISSION_READ')")
    // @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<PermissionResponseDto> getPermissionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID userId = currentUser.getId();
        PermissionResponseDto responseDto = permissionManagementService.getPermissionById(userId, id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PERMISSION_READ')")
    // @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<Page<PermissionResponseDto>> getAllPermissions(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Permission.PermissionCategory category,
            @RequestParam(required = false) Boolean requiresScope,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID roleId,
            @AuthenticationPrincipal UserLoginResponse currentUser,
            @ParameterObject Pageable pageable) {
        UUID userId = currentUser.getId();
        Page<PermissionResponseDto> permissions = permissionManagementService.getAllPermissions(
                userId, code, category, requiresScope, name, roleId, pageable);
        return ResponseEntity.ok(permissions);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PERMISSION_DELETE')")
    public ResponseEntity<Void> deletePermission(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID userId = currentUser.getId();
        permissionManagementService.deletePermission(userId, id);
        return ResponseEntity.noContent().build();
    }
}