package com.fleetmanagement.mapper;

import com.fleetmanagement.dto.request.RoleRequestDto;
import com.fleetmanagement.dto.response.RoleResponseDto;
import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Role entity and DTOs
 */
@Component
public class RoleMapper {

    public Role toEntity(RoleRequestDto dto) {
        return Role.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.getActive())
                .tenantId(dto.getTenantId())
                .scopeType(dto.getScopeType())
                .build();
    }

    public RoleResponseDto toResponseDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setActive(role.getActive());
        dto.setTenantId(role.getTenantId());
        dto.setScopeType(role.getScopeType());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        if (role.getPermissions() != null) {
            dto.setPermissionIds(
                    role.getPermissions().stream()
                            .map(Permission::getId)
                            .collect(Collectors.toSet())
            );
        }
        return dto;
    }

    public void updateEntityFromDto(RoleRequestDto dto, Role role) {
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setActive(dto.getActive());
        role.setTenantId(dto.getTenantId());
        role.setScopeType(dto.getScopeType());
    }
}