package com.fleetmanagement.service;

import com.fleetmanagement.dto.request.PermissionRequestDto;
import com.fleetmanagement.dto.response.PermissionResponseDto;
import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.mapper.PermissionMapper;
import com.fleetmanagement.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

/**
 * Service for managing permission CRUD operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final PermissionService permissionService;

    public PermissionResponseDto createPermission(UUID userId, PermissionRequestDto requestDto) {
        log.debug("Creating permission with code: {} by user: {}", requestDto.getCode(), userId);
        if (!permissionService.canGrantPermission(userId, requestDto.getCode(), null)) {
            throw new SecurityException("User lacks permission to create permission: " + requestDto.getCode());
        }
        if (permissionRepository.existsByCode(requestDto.getCode())) {
            throw new IllegalArgumentException("Permission with code " + requestDto.getCode() + " already exists");
        }

        Permission permission = permissionMapper.toEntity(requestDto);
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponseDto(savedPermission);
    }

    public PermissionResponseDto updatePermission(UUID userId, UUID id, PermissionRequestDto requestDto) {
        log.debug("Updating permission: {} by user: {}", id, userId);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
        if (!permissionService.canGrantPermission(userId, requestDto.getCode(), null)) {
            throw new SecurityException("User lacks permission to update permission: " + requestDto.getCode());
        }
        if (!permission.getCode().equals(requestDto.getCode()) &&
                permissionRepository.existsByCode(requestDto.getCode())) {
            throw new IllegalArgumentException("Permission with code " + requestDto.getCode() + " already exists");
        }

        permissionMapper.updateEntityFromDto(requestDto, permission);
        Permission updatedPermission = permissionRepository.save(permission);
        permissionService.refreshUserPermissions(userId); // Refresh permissions if needed
        return permissionMapper.toResponseDto(updatedPermission);
    }

    @Transactional(readOnly = true)
    public PermissionResponseDto getPermissionById(UUID userId, UUID id) {
        log.debug("Fetching permission: {} by user: {}", id, userId);
        if (!permissionService.hasPermission(userId, "PERMISSION_READ", null)) {
            throw new SecurityException("User lacks PERMISSION_READ permission");
        }
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
        return permissionMapper.toResponseDto(permission);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponseDto> getAllPermissions(
            UUID userId,
            String code,
            Permission.PermissionCategory category,
            Boolean requiresScope,
            String name,
            UUID roleId,
            Pageable pageable) {
        log.debug("Fetching permissions for user: {} with filters", userId);
        if (!permissionService.hasPermission(userId, "PERMISSION_READ", null)) {
            throw new SecurityException("User lacks PERMISSION_READ permission");
        }
        Page<Permission> permissions;
        if (code != null) {
            permissions = permissionRepository.findByCode(code)
                    .map(permission -> new PageImpl<>(Collections.singletonList(permission), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
        } else if (category != null) {
            permissions = permissionRepository.findByCategoryAndActiveTrue(category, pageable);
        } else if (requiresScope != null) {
            permissions = permissionRepository.findByRequiresScopeAndActiveTrue(requiresScope, pageable);
        } else if (name != null) {
            permissions = permissionRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
        } else if (roleId != null) {
            permissions = permissionRepository.findByRoleId(roleId, pageable);
        } else {
            permissions = permissionRepository.findByActiveTrue(pageable);
        }
        return permissions.map(permissionMapper::toResponseDto);
    }

    public void deletePermission(UUID userId, UUID id) {
        log.debug("Deleting permission: {} by user: {}", id, userId);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
        if (!permissionService.canGrantPermission(userId, permission.getCode(), null)) {
            throw new SecurityException("User lacks permission to delete permission: " + permission.getCode());
        }
        permission.setActive(false); // Soft delete
        permissionRepository.save(permission);
        permissionService.refreshUserPermissions(userId); // Refresh permissions
    }
}