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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_code", columnList = "code"),
        @Index(name = "idx_permission_category", columnList = "category"),
        @Index(name = "idx_permission_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @EntityListeners(AuditingEntityListener.class)
public class Permission {


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
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    @ToString.Include
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    @ToString.Include
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionCategory category;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "requires_scope", nullable = false)
    private Boolean requiresScope = false;

    // =======================
    // Auditing Fields
    // =======================
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // @LastModifiedDate
    // @Column(name = "updated_at", nullable = false)
    // private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID modifiedBy;

    // =======================
    // Relations
    // =======================
    @JsonIgnore
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;

    // =======================
    // Lifecycle Hooks
    // =======================
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (code != null)
            code = code.trim().toUpperCase();
        if (name != null)
            name = name.trim();
    }

    // =======================
    // FIXED Enum Permission Category
    // =======================
    public enum PermissionCategory {
        SYSTEM_ADMINISTRATION,
        USER_MANAGEMENT,
        ROLE_MANAGEMENT, 
        DEVICE_MANAGEMENT,
        VEHICLE_MANAGEMENT,
        LOCATION_TRACKING,
        ALERTS_NOTIFICATIONS,
        REPORTS_ANALYTICS,
        SECURITY_COMPLIANCE
    }
}