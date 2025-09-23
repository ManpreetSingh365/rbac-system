package com.fleetmanagement.mapper;

import com.fleetmanagement.dto.request.VehicleRequestDto;
import com.fleetmanagement.dto.response.VehicleResponseDto;
import com.fleetmanagement.entity.Device;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.entity.Vehicle;
import com.fleetmanagement.repository.DeviceRepository;
import com.fleetmanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Vehicle entity and DTOs
 */
@Component
@RequiredArgsConstructor
public class VehicleMapper {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final ModelMapper modelMapper;


    public Vehicle toEntity(VehicleRequestDto dto) {
        Vehicle vehicle = modelMapper.map(dto, Vehicle.class);

        if (dto.getDeviceId() != null) {
            Device device = deviceRepository.findById(dto.getDeviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + dto.getDeviceId()));
            vehicle.setDevice(device);
        }

        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            Set<User> users = userRepository.findAllByIdIn(dto.getUserIds());
            if (users.size() != dto.getUserIds().size()) {
                throw new EntityNotFoundException("One or more users not found");
            }
            vehicle.setUsers(users);
        }

        return vehicle;
    }

    public void updateEntityFromDto(VehicleRequestDto dto, Vehicle vehicle) {
        modelMapper.map(dto, vehicle);

        if (dto.getDeviceId() != null) {
            Device device = deviceRepository.findById(dto.getDeviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + dto.getDeviceId()));
            vehicle.setDevice(device);
        } else {
            vehicle.setDevice(null);
        }

        if (dto.getUserIds() != null) {
            Set<User> users = userRepository.findAllByIdIn(dto.getUserIds());
            if (users.size() != dto.getUserIds().size()) {
                throw new EntityNotFoundException("One or more users not found");
            }
            vehicle.setUsers(users);
        } else {
            vehicle.setUsers(null);
        }
    }

    public VehicleResponseDto toResponseDto(Vehicle vehicle) {
        VehicleResponseDto response = modelMapper.map(vehicle, VehicleResponseDto.class);

        if(vehicle.getDevice() != null) {
            response.setDeviceId(vehicle.getDevice().getId());
        }

        if(vehicle.getUsers() != null) {
            Set<UUID> userIds = vehicle.getUsers().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            response.setUserIds(userIds);
        }

        return response;
    }
    
}