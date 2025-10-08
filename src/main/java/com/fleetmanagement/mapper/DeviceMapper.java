package com.fleetmanagement.mapper;

import com.fleetmanagement.dto.request.DeviceRequestDto;
import com.fleetmanagement.dto.response.DeviceResponseDto;
import com.fleetmanagement.entity.Device;
import com.fleetmanagement.entity.User;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between Device entity and DTOs
 */
@Component
@RequiredArgsConstructor
public class DeviceMapper {

    private final ModelMapper modelMapper;

    public Device toEntity(DeviceRequestDto dto) {
        Device device = modelMapper.map(dto, Device.class);

        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            device.setUsers(dto.getUserIds().stream()
                    .map(id -> {
                        User user = new User();
                        user.setId(id);
                        return user;
                    })
                    .collect(Collectors.toSet()));
        }

        return device;
    }

    public void updateEntityFromDto(DeviceRequestDto dto, Device device) {
        modelMapper.map(dto, device);

        if (dto.getUserIds() != null) {
            device.setUsers(dto.getUserIds().stream()
                    .map(id -> {
                        User user = new User();
                        user.setId(id);
                        return user;
                    })
                    .collect(Collectors.toSet()));
        } else {
            device.setUsers(null);
        }
    }


    public DeviceResponseDto toResponseDto(Device device) {
        DeviceResponseDto response = modelMapper.map(device, DeviceResponseDto.class);

        if (device.getUsers() != null) {
            response.setUserIds(device.getUsers().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet()));
        }

        if (device.getVehicle() != null) {
            response.setVehicleId(device.getVehicle().getId());
        }

        return response;
    }
}