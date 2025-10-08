package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.RoleRequestDto;
import com.fleetmanagement.dto.response.RoleResponseDto;
import com.fleetmanagement.dto.response.UserLoginResponse;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.service.RoleService;
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

import java.util.Set;
import java.util.UUID;

/**
 * REST Controller for managing roles
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_CREATE')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto,
        @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID currentUserId = currentUser.getId();
        UUID tenantId = currentUser.getTenantId();
        RoleResponseDto responseDto = roleService.createRole(currentUserId, requestDto, tenantId);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_UPDATE')")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable UUID id,
      @Valid @RequestBody RoleRequestDto requestDto,
      @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID currentUserId = currentUser.getId();
        UUID tenantId = currentUser.getTenantId();
        RoleResponseDto responseDto = roleService.updateRole(currentUserId, id, requestDto, tenantId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_READ')")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id,
         @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID currentUserId = currentUser.getId();
        UUID tenantId = currentUser.getTenantId();
        RoleResponseDto responseDto = roleService.getRoleById(currentUserId, id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_READ')")
    public ResponseEntity<Page<RoleResponseDto>> getAllRoles(            
            @RequestParam(defaultValue = "TENANT") Role.ScopeType scopeType,
            @AuthenticationPrincipal UserLoginResponse currentUser,
            @ParameterObject Pageable pageable) {
        UUID currentUserId = currentUser.getId();
        UUID tenantId = currentUser.getTenantId();
        Page<RoleResponseDto> roles = roleService.getAllRoles(currentUserId, tenantId, scopeType, pageable);
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id,
     @AuthenticationPrincipal UserLoginResponse currentUser) {
        UUID currentUserId = currentUser.getId();
        UUID tenantId = currentUser.getTenantId();
        roleService.deleteRole(currentUserId, id);
        return ResponseEntity.noContent().build();
    }

    // @PatchMapping(value = "/{id}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ASSIGN_PERMISSION')")
    // public ResponseEntity<RoleResponseDto> updatePermissions(
    //         @PathVariable UUID id,
    //         @RequestBody Set<UUID> permissionIds,
    //         @AuthenticationPrincipal UserLoginResponse currentUser) {
    //     UUID currentUserId = currentUser.getId();
    //     RoleResponseDto responseDto = roleService.updatePermissions(currentUserId, id, permissionIds);
    //     return ResponseEntity.ok(responseDto);
    // }
}