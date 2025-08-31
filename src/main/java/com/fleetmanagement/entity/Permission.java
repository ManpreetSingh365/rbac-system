package com.fleetmanagement.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Entity - Defines atomic permissions for RBAC system
 * Based on the essential permissions model for fleet management
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "code"),
    @Index(name = "idx_permission_category", columnList = "category"),
    @Index(name = "idx_permission_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionCategory category;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    
    @Builder.Default
    @Column(name = "requires_scope", nullable = false)
    private Boolean requiresScope = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;
    
    public enum PermissionCategory {
        SYSTEM_ADMINISTRATION,
        USER_MANAGEMENT, 
        DEVICE_MANAGEMENT,
        VEHICLE_MANAGEMENT,
        LOCATION_TRACKING,
        ALERTS_NOTIFICATIONS,
        REPORTS_ANALYTICS,
        SECURITY_COMPLIANCE
    }
}