package com.fleetmanagement.mapper;

import com.fleetmanagement.dto.request.PermissionRequestDto;
import com.fleetmanagement.dto.response.PermissionResponseDto;
import com.fleetmanagement.entity.Permission;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class PermissionMapper {

    private final ModelMapper modelMapper;

    public Permission toEntity(PermissionRequestDto dto) {
        Permission response = modelMapper.map(dto, Permission.class);

        if(dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<com.fleetmanagement.entity.Role> roles = dto.getRoleIds().stream()
                    .map(id -> {
                        com.fleetmanagement.entity.Role role = new com.fleetmanagement.entity.Role();
                        role.setId(id);
                        return role;
                    })
                    .collect(Collectors.toSet());
            response.setRoles(roles);
        }
        return response;
    } 

    public void updateEntityFromDto(PermissionRequestDto dto, Permission permission) {
        modelMapper.map(dto, permission);

        if(dto.getRoleIds() != null) {
            Set<com.fleetmanagement.entity.Role> roles = dto.getRoleIds().stream()
                    .map(id -> {
                        com.fleetmanagement.entity.Role role = new com.fleetmanagement.entity.Role();
                        role.setId(id);
                        return role;
                    })
                    .collect(Collectors.toSet());
            permission.setRoles(roles);
        } else {
            permission.setRoles(null);
        }

        
    }

    public PermissionResponseDto toResponseDto(Permission permission) {
        PermissionResponseDto response = modelMapper.map(permission, PermissionResponseDto.class);

        if (permission.getRoles() != null) {
            Set<UUID> roleIds = permission.getRoles().stream()
                    .map(role -> role.getId())
                    .collect(java.util.stream.Collectors.toSet());
            response.setRoleIds(roleIds);
        }

        return response;
    }
}