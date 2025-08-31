package com.fleetmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Fleet Management RBAC System
 * 
 * High-scalable Spring Boot 3.5.5 application with Java 21
 * Implements comprehensive RBAC for fleet management operations
 * 
 * Features:
 * - Role-based access control with hierarchical permissions
 * - Redis caching for high performance
 * - JWT authentication and authorization
 * - Multi-tenant support with scope-based permissions
 * - Device registration via SMS by installers
 * - Optimized for 100K+ vehicles and devices
 * 
 * Architecture:
 * - SuperAdmin → creates multiple Admins with permissions
 * - Admin → creates Roles and Users with device/vehicle assignments
 * - Installer → registers devices via SMS
 * - Users → assigned to roles, devices, and vehicles
 * 
 * @author Fleet Management Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableJpaRepositories
@EnableTransactionManagement
public class RbacSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbacSystemApplication.class, args);
    }
}