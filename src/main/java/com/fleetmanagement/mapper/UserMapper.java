package com.fleetmanagement.mapper;

import com.fleetmanagement.dto.request.CreateUserRequest;
import com.fleetmanagement.dto.request.UpdateUserRequest;
import com.fleetmanagement.dto.response.UserResponse;
import com.fleetmanagement.entity.Device;
import com.fleetmanagement.entity.Role;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserResponse toResponseDto(User user){
        UserResponse response = modelMapper.map(user, UserResponse.class);

        if (user.getRoles() != null) {
            Set<UUID> roleIds = user.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            response.setRoleIds(roleIds);
        }

        if (user.getDevices() != null) {
            Set<UUID> deviceIds = user.getDevices().stream()
                    .map(Device::getId)
                    .collect(Collectors.toSet());
            response.setDevicesIds(deviceIds);
        }

        if (user.getVehicles() != null) {
            Set<UUID> vehicleIds = user.getVehicles().stream()
                    .map(Vehicle::getId)
                    .collect(Collectors.toSet());
            response.setVehiclesIds(vehicleIds);
        }

        return response;
    }

    public User toEntity(CreateUserRequest userRequest){
        User user = modelMapper.map(userRequest, User.class);

        if (userRequest.getRoleIds() != null && !userRequest.getRoleIds().isEmpty()) {
            Set<Role> roles = userRequest.getRoleIds().stream()
                    .map(id -> {
                        Role role = new Role();
                        role.setId(id);
                        return role;
                    })
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (userRequest.getDeviceIds() != null && !userRequest.getDeviceIds().isEmpty()) {
            Set<Device> devices = userRequest.getDeviceIds().stream()
                    .map(id -> {
                        Device device = new Device();
                        device.setId(id);
                        return device;
                    })
                    .collect(Collectors.toSet());
            user.setDevices(devices);
        }

        if (userRequest.getVehicleIds() != null && !userRequest.getVehicleIds().isEmpty()) {
            Set<Vehicle> vehicles = userRequest.getVehicleIds().stream()
                    .map(id -> {
                        Vehicle vehicle = new Vehicle();
                        vehicle.setId(id);
                        return vehicle;
                    })
                    .collect(Collectors.toSet());
            user.setVehicles(vehicles);
        }

        return user;
    }

    public void updateEntityFromDto(UpdateUserRequest dto, User user) {
        // Map simple fields
        modelMapper.map(dto, user);

        // Update roles
        if (dto.getRoleIds() != null) {
            Set<Role> roles = dto.getRoleIds().stream()
                    .map(id -> {
                        Role role = new Role();
                        role.setId(id);
                        return role;
                    })
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            user.setRoles(null);
        }

        // Update devices
        if (dto.getDeviceIds() != null) {
            Set<Device> devices = dto.getDeviceIds().stream()
                    .map(id -> {
                        Device device = new Device();
                        device.setId(id);
                        return device;
                    })
                    .collect(Collectors.toSet());
            user.setDevices(devices);
        } else {
            user.setDevices(null);
        }

        // Update vehicles
        if (dto.getVehicleIds() != null) {
            Set<Vehicle> vehicles = dto.getVehicleIds().stream()
                    .map(id -> {
                        Vehicle vehicle = new Vehicle();
                        vehicle.setId(id);
                        return vehicle;
                    })
                    .collect(Collectors.toSet());
            user.setVehicles(vehicles);
        } else {
            user.setVehicles(null);
        }
    }
}
