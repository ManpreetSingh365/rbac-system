package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.RoleRequestDto;
import com.fleetmanagement.dto.response.RoleResponseDto;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto responseDto = roleService.createRole(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto responseDto = roleService.updateRole(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id) {
        RoleResponseDto responseDto = roleService.getRoleById(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<RoleResponseDto>> getAllRoles(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) Role.ScopeType scopeType,
            Pageable pageable) {
        Page<RoleResponseDto> roles = roleService.getAllRoles(tenantId, scopeType, pageable);
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleResponseDto> updatePermissions(
            @PathVariable UUID id,
            @RequestBody Set<UUID> permissionIds) {
        RoleResponseDto responseDto = roleService.updatePermissions(id, permissionIds);
        return ResponseEntity.ok(responseDto);
    }
}