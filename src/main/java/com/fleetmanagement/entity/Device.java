package com.fleetmanagement.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Device Entity - Represents GPS tracking devices
 * Updated: Added expiryAt field, changed createdAt to installedAt
 * Supports SMS registration by installers and assignment to vehicles/users
 */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_imei", columnList = "imei"),
    @Index(name = "idx_device_status", columnList = "status"),
    @Index(name = "idx_device_tenant", columnList = "tenant_id"),
    @Index(name = "idx_device_expiry", columnList = "expiryAt"),
    @Index(name = "idx_created_by", columnList = "created_by")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String imei;
    
    @Column(nullable = false, length = 100)
    private String deviceModel;
    
    @Column(length = 100)
    private String firmwareVersion;
    
    @Column(length = 20)
    private String simNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.REGISTERED;
    
    @Column(name = "tenant_id")
    private UUID tenantId;
    
    @Column(name = "registered_by_sms")
    @Builder.Default
    private Boolean registeredBySms = false;
    
    @Column(name = "installer_phone")
    private String installerPhone;
    
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;
    
    @CreationTimestamp
    @Column(name = "installed_at", nullable = false, updatable = false)
    private LocalDateTime installedAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "modified_by")
    private UUID modifiedBy;
    
    @ManyToMany(mappedBy = "devices", fetch = FetchType.LAZY)
    private Set<User> users;
    
    @OneToOne(mappedBy = "device", fetch = FetchType.LAZY)
    private Vehicle vehicle;
    
    public enum DeviceStatus {
        REGISTERED, ACTIVE, INACTIVE, MAINTENANCE, DECOMMISSIONED
    }
}