package com.fleetmanagement.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * User Entity - Represents system users with roles and permissions
 * Updated: Changed email to username, made email optional
 * Optimized for high scalability with proper indexing and caching
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_active", columnList = "active"),
        @Index(name = "idx_user_tenant", columnList = "tenant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;  // Changed from email to username

    @Column(nullable = true, length = 100)  // Made email optional
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

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
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
      @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_devices",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "device_id")
    )
        @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Device> devices;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_vehicles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_id")
    )
      @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Vehicle> vehicles;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}