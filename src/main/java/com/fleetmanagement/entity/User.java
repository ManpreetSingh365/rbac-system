package com.fleetmanagement.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Modern, production-ready User entity.
 * Features:
 * - UUID primary key
 * - Equals/hashCode on ID only
 * - Lazy Many-to-Many collections
 * - Validations
 * - Safe toString (collections excluded)
 * - Automatic auditing
 */
@Entity
@Table(name = "users", indexes = {
                @Index(name = "idx_user_username", columnList = "username", unique = true),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_active_tenant", columnList = "active, tenant_id"),
                @Index(name = "idx_user_last_login", columnList = "last_login")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @EntityListeners(AuditingEntityListener.class)
public class User {

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
        // Basic Info
        // =======================
        @NotBlank
        @Size(min = 3, max = 100)
        @Column(nullable = false, unique = true, length = 100)
        @ToString.Include
        private String username;

        @Email
        @Column(length = 100)
        @ToString.Include
        private String email;

        @NotBlank
        @Size(max = 100)
        @Column(nullable = false, length = 100)
        private String firstName;

        @NotBlank
        @Size(max = 100)
        @Column(nullable = false, length = 100)
        private String lastName;

        @JsonIgnore
        @NotBlank
        @Column(nullable = false, length = 255)
        private String password;

        @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Invalid phone number format")
        @Column(nullable = false, length = 15)
        private String phoneNumber;

        @Builder.Default
        @Column(nullable = false)
        private Boolean active = true;

        private UUID tenantId;
        private LocalDateTime lastLogin;

        // =======================
        // Auditing
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
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        private Set<Role> roles;

        @JsonIgnore
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_devices", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "device_id"))
        private Set<Device> devices;

        @JsonIgnore
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_vehicles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
        private Set<Vehicle> vehicles;

        // =======================
        // Lifecycle Hooks
        // =======================
        @PrePersist
        @PreUpdate
        private void normalize() {
                if (username != null)
                        username = username.trim().toLowerCase();
                if (email != null)
                        email = email.trim().toLowerCase();
        }

        // =======================
        // Transient / Derived Fields
        // =======================
        @Transient
        public String getFullName() {
                return firstName + " " + lastName;
        }
}