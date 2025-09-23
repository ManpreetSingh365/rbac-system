package com.fleetmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Role Entity - Represents user roles with associated permissions
 * Supports hierarchical role structure for complex organizations
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Builder.Default
    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @NotNull(message = "Scope type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    @Builder.Default
    private ScopeType scopeType = ScopeType.TENANT;

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

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users;

    public enum ScopeType {
        GLOBAL, TENANT, FLEET, REGIONAL
    }
}