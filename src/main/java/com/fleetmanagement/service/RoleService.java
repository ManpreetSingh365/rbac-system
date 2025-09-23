package com.fleetmanagement.service;

import com.fleetmanagement.dto.request.RoleRequestDto;
import com.fleetmanagement.dto.response.RoleResponseDto;
import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.mapper.RoleMapper;
import com.fleetmanagement.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Service for managing roles, including creation, update, deletion, and permission management
*/
@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    public RoleResponseDto createRole(RoleRequestDto requestDto) {
        validateRoleRequest(requestDto);
        if (roleRepository.existsByNameAndTenantId(requestDto.getName(), requestDto.getTenantId())) {
            throw new IllegalArgumentException("Role with name " + requestDto.getName() + " already exists for this tenant");
        }

        Role role = roleMapper.toEntity(requestDto);
        if (requestDto.getPermissionIds() != null && !requestDto.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = permissionService.findAllByIds(requestDto.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponseDto(savedRole);        
    }

    public RoleResponseDto updateRole(UUID id, RoleRequestDto requestDto) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        validateRoleRequest(requestDto);
        if (roleRepository.existsByNameAndTenantIdExcluding(requestDto.getName(), requestDto.getTenantId(), id)) {
            throw new IllegalArgumentException("Role with name " + requestDto.getName() + " already exists for this tenant");
        }

        roleMapper.updateEntityFromDto(requestDto, role);
        if (requestDto.getPermissionIds() != null) {
            Set<Permission> permissions = permissionService.findAllByIds(requestDto.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
         return  roleMapper.toResponseDto(updatedRole);        
    }

    public RoleResponseDto getRoleById(UUID id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
        return  roleMapper.toResponseDto(role);
    }

    public Page<RoleResponseDto> getAllRoles(UUID tenantId, Role.ScopeType scopeType, Pageable pageable) {
        Page<Role> roles;

        if (tenantId != null && scopeType != null) {
            roles = roleRepository.findByTenantIdAndScopeTypeAndActiveTrue(tenantId, scopeType, pageable);
        } else if (tenantId != null) {
            roles = roleRepository.findByTenantIdAndActiveTrue(tenantId, pageable);
        } else if (scopeType != null) {
            roles = roleRepository.findByScopeTypeAndActiveTrue(scopeType, pageable);
        } else {
            roles = roleRepository.findByActiveTrue(pageable);
        }

        return roles.map(roleMapper::toResponseDto);
    }


    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
        role.setActive(false);
        roleRepository.save(role);
    }

    public RoleResponseDto updatePermissions(UUID id, Set<UUID> permissionIds) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        if (permissionIds != null) {
            Set<Permission> permissions = permissionService.findAllByIds(permissionIds);
            role.setPermissions(permissions);
        } else {
            role.setPermissions(null);
        }

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponseDto(updatedRole);
    }

    private void validateRoleRequest(RoleRequestDto requestDto) {
        if (requestDto.getScopeType() == Role.ScopeType.TENANT && requestDto.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID is required for TENANT scope roles");
        }
        if (requestDto.getScopeType() == Role.ScopeType.GLOBAL && requestDto.getTenantId() != null) {
            throw new IllegalArgumentException("Tenant ID must be null for GLOBAL scope roles");
        }
    }
}