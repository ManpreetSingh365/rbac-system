package com.fleetmanagement.service;

import com.fleetmanagement.dto.request.VehicleRequestDto;
import com.fleetmanagement.dto.response.VehicleResponseDto;
import com.fleetmanagement.entity.Vehicle;
import com.fleetmanagement.mapper.VehicleMapper;
import com.fleetmanagement.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing vehicles
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final PermissionService permissionService;

    public VehicleResponseDto createVehicle(VehicleRequestDto requestDto, UUID tenandId, UUID userId) {

        if (!permissionService.hasPermission(userId, "VEHICLE_CREATE", tenandId)) {
            throw new SecurityException("User lacks DEVICE_REGISTER permission for tenant: " + tenandId);
        }

        validateVehicleRequest(requestDto);
        Vehicle vehicle = vehicleMapper.toEntity(requestDto);
        vehicle.setTenantId(tenandId);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponseDto(savedVehicle);
    }

    public VehicleResponseDto updateVehicle(UUID id, VehicleRequestDto requestDto, UUID tenandId, UUID userId) {

        if (!permissionService.hasPermission(userId, "VEHICLE_UPDATE", tenandId)) {
            throw new SecurityException("User lacks VEHICLE_UPDATE permission for tenant: " + tenandId);
        }

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        validateVehicleRequest(requestDto);
        if (!vehicle.getLicensePlate().equals(requestDto.getLicensePlate()) &&
                vehicleRepository.existsByLicensePlate(requestDto.getLicensePlate())) {
            throw new IllegalArgumentException("Vehicle with license plate " + requestDto.getLicensePlate() + " already exists");
        }
        if (requestDto.getVin() != null && !requestDto.getVin().equals(vehicle.getVin()) &&
                vehicleRepository.existsByVin(requestDto.getVin())) {
            throw new IllegalArgumentException("Vehicle with VIN " + requestDto.getVin() + " already exists");
        }

        vehicleMapper.updateEntityFromDto(requestDto, vehicle);
        vehicle.setTenantId(tenandId);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponseDto(updatedVehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(UUID id, UUID tenandId, UUID userId) {

        if (!permissionService.hasPermission(userId, "VEHICLE_READ", tenandId)) {
            throw new SecurityException("User lacks VEHICLE_UPDATE permission for tenant: " + tenandId);
        }

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
        return vehicleMapper.toResponseDto(vehicle);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDto> getAllVehicles(
            UUID userId,
            UUID tenantId,
            Vehicle.VehicleStatus status,
            UUID fleetId,
            String brand,
            String model,
            Vehicle.VehicleType vehicleType,
            Integer startYear,
            Integer endYear,
            Pageable pageable) {
        if (!permissionService.hasPermission(userId, "VEHICLE_READ", tenantId)) {
            throw new SecurityException("User lacks VEHICLE_READ permission for tenant: " + tenantId);
        }
        Page<Vehicle> vehicles;
        if (tenantId != null && status != null) {
            vehicles = vehicleRepository.findByStatusAndTenantId(status, tenantId, pageable);
        } else if (tenantId != null) {
            vehicles = vehicleRepository.findByTenantId(tenantId, pageable);
        } else if (status != null) {
            vehicles = vehicleRepository.findByStatus(status, pageable);
        } else if (fleetId != null) {
            vehicles = vehicleRepository.findByFleetId(fleetId, pageable);
        } else if (brand != null && model != null) {
            vehicles = vehicleRepository.findByBrandAndModel(brand, model, pageable);
        } else if (brand != null) {
            vehicles = vehicleRepository.findByBrand(brand, pageable);
        } else if (vehicleType != null) {
            vehicles = vehicleRepository.findByVehicleType(vehicleType, pageable);
        } else if (startYear != null && endYear != null) {
            vehicles = vehicleRepository.findByYearBetween(startYear, endYear, pageable);
        } else {
            vehicles = vehicleRepository.findAll(pageable);
        }
        return vehicles.map(vehicleMapper::toResponseDto);
    }

    public void deleteVehicle(UUID id, UUID tenandId, UUID userId) {

        if (!permissionService.hasPermission(userId, "VEHICLE_DELETE", tenandId)) {
            throw new SecurityException("User lacks VEHICLE_DELETE permission for tenant: " + tenandId);
        }
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
        vehicle.setStatus(Vehicle.VehicleStatus.RETIRED);
        vehicleRepository.save(vehicle);
    }

    private void validateVehicleRequest(VehicleRequestDto requestDto) {
        if (requestDto.getYear() != null && requestDto.getYear() > LocalDateTime.now().getYear() + 1) {
            throw new IllegalArgumentException("Year cannot be in the future");
        }

        if (vehicleRepository.existsByLicensePlate(requestDto.getLicensePlate())) {
            throw new IllegalArgumentException("Vehicle with license plate " + requestDto.getLicensePlate() + " already exists");
        }
        if (requestDto.getVin() != null && vehicleRepository.existsByVin(requestDto.getVin())) {
            throw new IllegalArgumentException("Vehicle with VIN " + requestDto.getVin() + " already exists");
        }
    }
}