
package com.fleetmanagement.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_plate", columnList = "licensePlate"),
        @Index(name = "idx_vehicle_tenant", columnList = "tenant_id"),
        @Index(name = "idx_vehicle_status", columnList = "status"),
        @Index(name = "idx_vehicle_brand", columnList = "brand")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @EntityListeners(AuditingEntityListener.class)
public class Vehicle {

    // =======================
    // Primary Key
    // =======================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    // =======================
    // Basic Fields
    // =======================
    @NotBlank
    @Size(max = 15)
    @Column(nullable = false, unique = true, length = 15)
    @ToString.Include
    private String licensePlate;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String model;

    @NotNull
    @Min(1900)
    @Max(2100)
    @Column(nullable = false)
    private Integer year;

    @Size(max = 20)
    @Column(length = 20)
    private String vin;

    @NotNull
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

    // =======================
    // Auditing Fields
    // =======================
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID modifiedBy;

    // =======================
    // Relations
    // =======================
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_id")
    private Device device;

    @JsonIgnore
    @ManyToMany(mappedBy = "vehicles", fetch = FetchType.LAZY)
    private Set<User> users;

    // =======================
    // Lifecycle Hooks
    // =======================
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (licensePlate != null)
            licensePlate = licensePlate.trim().toUpperCase();
        if (brand != null)
            brand = brand.trim();
        if (model != null)
            model = model.trim();
    }

    // =======================
    // Enums
    // =======================
    public enum VehicleType {
        CAR, TRUCK, VAN, MOTORCYCLE, BUS, TRAILER
    }

    public enum VehicleStatus {
        ACTIVE, INACTIVE, MAINTENANCE, RETIRED
    }
}