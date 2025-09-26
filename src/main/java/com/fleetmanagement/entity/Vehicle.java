package com.fleetmanagement.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vehicle Entity - Represents fleet vehicles with device assignments
 * Updated: Changed make to brand
 * Supports assignment to multiple users and single device mapping
 */
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_plate", columnList = "licensePlate"),
        @Index(name = "idx_vehicle_tenant", columnList = "tenant_id"),
        @Index(name = "idx_vehicle_status", columnList = "status"),
        @Index(name = "idx_vehicle_brand", columnList = "brand")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, unique = true, length = 15)
    private String licensePlate;

    @Column(nullable = false, length = 100)
    private String brand;  // Changed from make to brand

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 20)
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "fleet_id")
    private UUID fleetId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToMany(mappedBy = "vehicles", fetch = FetchType.LAZY)
    private Set<User> users;

    public enum VehicleType {
        CAR, TRUCK, VAN, MOTORCYCLE, BUS, TRAILER
    }

    public enum VehicleStatus {
        ACTIVE, INACTIVE, MAINTENANCE, RETIRED
    }
}