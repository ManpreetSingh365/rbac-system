
package com.fleetmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_device_imei", columnList = "imei"),
        @Index(name = "idx_device_status", columnList = "status"),
        @Index(name = "idx_device_tenant", columnList = "tenant_id"),
        @Index(name = "idx_device_expiry", columnList = "expiry_at"),
        @Index(name = "idx_created_by", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @EntityListeners(AuditingEntityListener.class)
public class Device {

    // =======================
    // Primary Key
    // =======================
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    // =======================
    // Basic Fields
    // =======================
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    @ToString.Include
    private String imei;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String deviceModel;

    @Size(max = 100)
    @Column(length = 100)
    private String firmwareVersion;

    @Size(max = 20)
    @Column(length = 20)
    private String simNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.REGISTERED;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "registered_by_sms", nullable = false)
    @Builder.Default
    private Boolean registeredBySms = false;

    @Size(max = 15)
    @Column(name = "installer_phone", length = 15)
    private String installerPhone;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;

    // =======================
    // Auditing Fields
    // =======================
    @CreatedDate
    @Column(name = "installed_at", nullable = false, updatable = false)
    private LocalDateTime installedAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID modifiedBy;

    // =======================
    // Relations
    // =======================
    @JsonIgnore
    @ManyToMany(mappedBy = "devices", fetch = FetchType.LAZY)
    private Set<User> users;

    @JsonIgnore
    @OneToOne(mappedBy = "device", fetch = FetchType.LAZY)
    private Vehicle vehicle;

    // =======================
    // Lifecycle Hooks
    // =======================
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (imei != null)
            imei = imei.trim().toUpperCase();
        if (deviceModel != null)
            deviceModel = deviceModel.trim();
    }

    // =======================
    // Enum Device Status
    // =======================
    public enum DeviceStatus {
        REGISTERED, ACTIVE, INACTIVE, MAINTENANCE, DECOMMISSIONED
    }
}
