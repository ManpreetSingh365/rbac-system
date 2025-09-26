package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.VehicleRequestDto;
import com.fleetmanagement.dto.response.VehicleResponseDto;
import com.fleetmanagement.entity.Vehicle;
import com.fleetmanagement.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing vehicles
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VEHICLE_CREATE')")
    public ResponseEntity<VehicleResponseDto> createVehicle(@Valid @RequestBody VehicleRequestDto requestDto) {
        VehicleResponseDto responseDto = vehicleService.createVehicle(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VEHICLE_UPDATE')")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequestDto requestDto) {
        VehicleResponseDto responseDto = vehicleService.updateVehicle(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VEHICLE_READ')")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable UUID id) {
        VehicleResponseDto responseDto = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VEHICLE_READ')")
    public ResponseEntity<Page<VehicleResponseDto>> getAllVehicles(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) Vehicle.VehicleStatus status,
            @RequestParam(required = false) UUID fleetId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Vehicle.VehicleType vehicleType,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            Pageable pageable) {
        Page<VehicleResponseDto> vehicles = vehicleService.getAllVehicles(
                tenantId, status, fleetId, brand, model, vehicleType, startYear, endYear, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VEHICLE_DELETE')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}