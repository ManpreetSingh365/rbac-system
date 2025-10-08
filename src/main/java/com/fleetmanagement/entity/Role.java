
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
import jakarta.persistence.JoinTable;
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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Modern, production-ready Role entity.
 * Features:
 * - UUID primary key
 * - Equals/hashCode on ID only
 * - Lazy Many-to-Many collections
 * - Validations
 * - Safe toString (collections excluded)
 * - Automatic auditing
 * - Normalized role names
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name"),
        @Index(name = "idx_role_tenant", columnList = "tenant_id"),
        @Index(name = "idx_role_active", columnList = "active"),
        @Index(name = "idx_role_scope", columnList = "scope_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @EntityListeners(AuditingEntityListener.class)
public class Role {

    // =======================
    // Primary Key
    // =======================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    // =======================
    // Basic Fields
    // =======================
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Builder.Default
    @NotNull
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @NotNull(message = "Scope type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    @Builder.Default
    private ScopeType scopeType = ScopeType.TENANT;

    // =======================
    // Auditing Fields
    // =======================
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID modifiedBy;

    // =======================
    // Relations
    // =======================
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users;

    // =======================
    // Lifecycle Hooks
    // =======================
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (name != null)
            name = name.trim().toLowerCase();
    }

    public enum ScopeType {
        GLOBAL, TENANT, FLEET, REGIONAL
    }
}