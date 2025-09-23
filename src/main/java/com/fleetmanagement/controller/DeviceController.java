package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.DeviceRequestDto;
import com.fleetmanagement.dto.response.DeviceResponseDto;
import com.fleetmanagement.entity.Device;
import com.fleetmanagement.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST Controller for managing devices
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_REGISTER')")
    public ResponseEntity<DeviceResponseDto> createDevice(
            @Valid @RequestBody DeviceRequestDto requestDto,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        DeviceResponseDto responseDto = deviceService.createDevice(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_UPDATE')")
    public ResponseEntity<DeviceResponseDto> updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceRequestDto requestDto,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        DeviceResponseDto responseDto = deviceService.updateDevice(userId, id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_READ')")
    public ResponseEntity<DeviceResponseDto> getDeviceById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        DeviceResponseDto responseDto = deviceService.getDeviceById(userId, id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_READ')")
    public ResponseEntity<Page<DeviceResponseDto>> getAllDevices(
            @RequestParam(required = false) String imei,
            @RequestParam(required = false) Device.DeviceStatus status,
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) String deviceModel,
            @RequestParam(required = false) String installerPhone,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID assignedUserId,
            @RequestParam(required = false) Boolean registeredBySms,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastHeartbeatBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiryBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiryBetweenStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiryBetweenEnd,
            Pageable pageable,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Page<DeviceResponseDto> devices = deviceService.getAllDevices(
                userId, imei, status, tenantId, deviceModel, installerPhone, vehicleId,
                assignedUserId, registeredBySms, lastHeartbeatBefore, expiryBefore,
                expiryBetweenStart, expiryBetweenEnd, pageable);
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_DELETE')")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        deviceService.deleteDevice(userId, id);
        return ResponseEntity.noContent().build();
    }
}