package com.fleetmanagement.service;

import com.fleetmanagement.dto.request.DeviceRequestDto;
import com.fleetmanagement.dto.response.DeviceResponseDto;
import com.fleetmanagement.entity.Device;
import com.fleetmanagement.mapper.DeviceMapper;
import com.fleetmanagement.repository.DeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Service for managing device CRUD operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final PermissionService permissionService;

    public DeviceResponseDto createDevice(UUID userId, DeviceRequestDto requestDto) {
        log.debug("Creating device with IMEI: {} by user: {}", requestDto.getImei(), userId);
        if (!permissionService.hasPermission(userId, "DEVICE_REGISTER", requestDto.getTenantId())) {
            throw new SecurityException("User lacks DEVICE_REGISTER permission for tenant: " + requestDto.getTenantId());
        }
        if (deviceRepository.existsByImei(requestDto.getImei())) {
            throw new IllegalArgumentException("Device with IMEI " + requestDto.getImei() + " already exists");
        }

        Device device = deviceMapper.toEntity(requestDto);
        Device savedDevice = deviceRepository.save(device);
        return deviceMapper.toResponseDto(savedDevice);
    }

    public DeviceResponseDto updateDevice(UUID userId, UUID id, DeviceRequestDto requestDto) {
        log.debug("Updating device: {} by user: {}", id, userId);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));
        if (!permissionService.hasPermission(userId, "DEVICE_UPDATE", device.getTenantId())) {
            throw new SecurityException("User lacks DEVICE_UPDATE permission for tenant: " + device.getTenantId());
        }
        if (!device.getImei().equals(requestDto.getImei()) &&
                deviceRepository.existsByImei(requestDto.getImei())) {
            throw new IllegalArgumentException("Device with IMEI " + requestDto.getImei() + " already exists");
        }

        deviceMapper.updateEntityFromDto(requestDto, device);
        Device updatedDevice = deviceRepository.save(device);
        return deviceMapper.toResponseDto(updatedDevice);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponseDto> getAllDevices(
            UUID userId,
            String imei,
            Device.DeviceStatus status,
            UUID tenantId,
            String deviceModel,
            String installerPhone,
            UUID vehicleId,
            UUID assignedUserId,
            Boolean registeredBySms,
            LocalDateTime lastHeartbeatBefore,
            LocalDateTime expiryBefore,
            LocalDateTime expiryBetweenStart,
            LocalDateTime expiryBetweenEnd,
            Pageable pageable) {
        log.debug("Fetching devices for user: {} with filters", userId);
        if (!permissionService.hasPermission(userId, "DEVICE_READ", tenantId)) {
            throw new SecurityException("User lacks DEVICE_READ permission for tenant: " + tenantId);
        }
        Page<Device> devices;
        if (imei != null) {
            devices = deviceRepository.findByImei(imei)
                    .map(device -> new PageImpl<>(Collections.singletonList(device), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
        } else if (status != null && tenantId != null) {
            devices = deviceRepository.findByStatusAndTenantId(status, tenantId, pageable);
        } else if (status != null) {
            devices = deviceRepository.findByStatus(status, pageable);
        } else if (tenantId != null) {
            devices = deviceRepository.findByTenantId(tenantId, pageable);
        } else if (deviceModel != null) {
            devices = deviceRepository.findByDeviceModel(deviceModel, pageable);
        } else if (installerPhone != null) {
            devices = deviceRepository.findByInstallerPhone(installerPhone, pageable);
        } else if (vehicleId != null) {
            devices = deviceRepository.findByVehicleId(vehicleId)
                    .map(device -> new PageImpl<>(Collections.singletonList(device), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
        } else if (assignedUserId != null) {
            devices = deviceRepository.findByUserId(assignedUserId, pageable);
        } else if (registeredBySms != null && registeredBySms) {
            devices = deviceRepository.findByRegisteredBySmsTrue(pageable);
        } else if (lastHeartbeatBefore != null) {
            devices = deviceRepository.findInactiveDevices(lastHeartbeatBefore, pageable);
        } else if (expiryBefore != null) {
            devices = deviceRepository.findExpiredDevices(expiryBefore, pageable);
        } else if (expiryBetweenStart != null && expiryBetweenEnd != null) {
            devices = deviceRepository.findDevicesExpiringSoon(expiryBetweenStart, expiryBetweenEnd, pageable);
        } else {
            devices = deviceRepository.findAll(pageable);
        }
        return devices.map(deviceMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public DeviceResponseDto getDeviceById(UUID userId, UUID id) {
        log.debug("Fetching device: {} by user: {}", id, userId);
        if (!permissionService.hasPermission(userId, "DEVICE_READ", null)) {
            throw new SecurityException("User lacks DEVICE_READ permission");
        }
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));
        if (!permissionService.canAccessTenant(userId, device.getTenantId())) {
            throw new SecurityException("User cannot access tenant: " + device.getTenantId());
        }
        return deviceMapper.toResponseDto(device);
    }

    public void deleteDevice(UUID userId, UUID id) {
        log.debug("Deleting device: {} by user: {}", id, userId);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));
        if (!permissionService.hasPermission(userId, "DEVICE_DELETE", device.getTenantId())) {
            throw new SecurityException("User lacks DEVICE_DELETE permission for tenant: " + device.getTenantId());
        }
        deviceRepository.delete(device);
    }
}