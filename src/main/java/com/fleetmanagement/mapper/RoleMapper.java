package com.fleetmanagement.mapper;

import com.fleetmanagement.dto.request.RoleRequestDto;
import com.fleetmanagement.dto.response.RoleResponseDto;
import com.fleetmanagement.entity.Permission;
import com.fleetmanagement.entity.Role;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class RoleMapper {


    private final ModelMapper modelMapper;

    public RoleResponseDto toResponseDto(Role role) {
        RoleResponseDto response = modelMapper.map(role, RoleResponseDto.class);

        if (role.getPermissions() != null) {
            Set<UUID> permissionIds = role.getPermissions().stream()
                    .map(Permission::getId)
                    .collect(java.util.stream.Collectors.toSet());
            response.setPermissionIds(permissionIds);
        }

        return response;
    }

    public Role toEntity(RoleRequestDto dto) {

        Role response = modelMapper.map(dto, Role.class);

        if(dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = dto.getPermissionIds().stream()
                    .map(id -> {
                        Permission permission = new Permission();
                        permission.setId(id);
                        return permission;
                    })
                    .collect(java.util.stream.Collectors.toSet());
            response.setPermissions(permissions);
        }
        return response;
    }

    public void updateEntityFromDto(RoleRequestDto dto, Role role) {

        modelMapper.map(dto, role);

        if(dto.getPermissionIds() != null) {
            Set<Permission> permissions = dto.getPermissionIds().stream()
                    .map(id -> {
                        Permission permission = new Permission();
                        permission.setId(id);
                        return permission;
                    })
                    .collect(java.util.stream.Collectors.toSet());
            role.setPermissions(permissions);
        } else {
            role.setPermissions(null);
        }
    }

}