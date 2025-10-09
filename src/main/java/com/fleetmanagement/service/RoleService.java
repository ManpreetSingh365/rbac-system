package com.fleetmanagement.service;

import com.fleetmanagement.dto.request.RoleRequestDto;
import com.fleetmanagement.dto.response.RoleResponseDto;
import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.entity.type.RoleScope;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.exception.ResourceNotFoundException;
import com.fleetmanagement.mapper.RoleMapper;
import com.fleetmanagement.repository.RoleRepository;
import com.fleetmanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fleetmanagement.entity.type.RoleScope;

import java.util.Optional;
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
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    public RoleResponseDto createRole(UUID currentUserId, RoleRequestDto requestDto, UUID tenantId) {
        validateRoleRequest(currentUserId, requestDto, tenantId);
        if (roleRepository.existsByNameAndTenantId(requestDto.getName(), tenantId)) {
            throw new IllegalArgumentException("Role with name " + requestDto.getName() + " already exists for this tenant");
        }

        User user = userRepository.getReferenceById(currentUserId);


        Role role = roleMapper.toEntity(requestDto);
        if (requestDto.getPermissionIds() != null && !requestDto.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = permissionService.validateAndFindAllByIds(requestDto.getPermissionIds(), user);
            role.setPermissions(permissions);
        }
        role.setTenantId(tenantId);

        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponseDto(savedRole);
    }

    public RoleResponseDto updateRole(UUID currentUserId, UUID id, RoleRequestDto requestDto, UUID tenantId) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        validateRoleRequest(currentUserId, requestDto, tenantId);
        if (roleRepository.existsByNameAndTenantIdExcluding(requestDto.getName(), tenantId, id)) {
            throw new IllegalArgumentException("Role with name " + requestDto.getName() + " already exists for this tenant");
        }

        User user = userRepository.getReferenceById(currentUserId);

        roleMapper.updateEntityFromDto(requestDto, role);
        if (requestDto.getPermissionIds() != null) {
            Set<Permission> permissions = permissionService.validateAndFindAllByIds(requestDto.getPermissionIds(), user);
            role.setPermissions(permissions);
        }
        role.setTenantId(tenantId);

        Role updatedRole = roleRepository.save(role);
        return  roleMapper.toResponseDto(updatedRole);
    }

    public RoleResponseDto getRoleById(UUID currentUserId, UUID id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        return  roleMapper.toResponseDto(role);
    }

    public Page<RoleResponseDto> getAllRoles(UUID currentUserId,UUID tenantId, RoleScope scopeType, Pageable pageable) {
        Page<Role> roles;

        validateReadRequest(currentUserId, tenantId, scopeType);

        roles = roleRepository.findByTenantIdAndRoleScopeAndActiveTrue(tenantId, scopeType, pageable);
        // if (tenantId != null && scopeType != null) {
        //     roles = roleRepository.findByTenantIdAndScopeTypeAndActiveTrue(tenantId, scopeType, pageable);
        // } else if (tenantId != null) {
        //     roles = roleRepository.findByTenantIdAndActiveTrue(tenantId, pageable);
        // } else if (scopeType != null) {
        //     roles = roleRepository.findByScopeTypeAndActiveTrue(scopeType, pageable);
        // } else {
        //     roles = roleRepository.findByActiveTrue(pageable);
        // }


        return roles.map(roleMapper::toResponseDto);
    }


    public void deleteRole(UUID currentUserId, UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
        role.setActive(false);
        roleRepository.save(role);
    }

    // public RoleResponseDto updatePermissions(UUID currentUserId, UUID id, Set<UUID> permissionIds) {
    //     Role role = roleRepository.findByIdWithPermissions(id)
    //             .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

    //     if (permissionIds != null) {
    //         Set<Permission> permissions = permissionService.findAllByIds(permissionIds);
    //         role.setPermissions(permissions);
    //     } else {
    //         role.setPermissions(null);
    //     }

    //     Role updatedRole = roleRepository.save(role);
    //     return roleMapper.toResponseDto(updatedRole);
    // }

    private void validateRoleRequest(UUID currentUserId, RoleRequestDto requestDto, UUID tenantId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));
        if(currentUser.getTenantId().equals(tenantId) && tenantId != null){
            throw new IllegalArgumentException("Tenant Scope failed");
        }

        if (requestDto.getRoleScope() == RoleScope.TENANT && tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required for TENANT scope roles");
        }
        if (requestDto.getRoleScope() == RoleScope.GLOBAL && tenantId != null) {

            throw new IllegalArgumentException("Tenant ID must be null for GLOBAL scope roles");
        }
    }

    private void validateReadRequest(UUID currentUserId, UUID tenantId,  RoleScope scopeType) {


        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

       if(tenantId == null || tenantId.equals(null)){
           throw new IllegalArgumentException("Tenant ID is required");
       }

         if (scopeType == RoleScope.GLOBAL && tenantId != null) {
              throw new IllegalArgumentException("Tenant ID must be null for GLOBAL scope roles");
         }
    }
    
}