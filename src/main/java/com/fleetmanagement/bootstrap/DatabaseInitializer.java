// package com.fleetmanagement.bootstrap;

// import java.sql.Connection;
// import java.sql.DatabaseMetaData;
// import java.sql.ResultSet;

// import javax.sql.DataSource;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.core.annotation.Order;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Component;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// /**
//  * Intelligent Database Initializer
//  *
//  * Automatically handles both scenarios:
//  * 1. First-time setup: Creates schema if database is empty
//  * 2. Existing database: Skips schema creation if tables exist
//  *
//  * This ensures seamless deployment in any environment without manual intervention.
//  */
// @Slf4j
// @RequiredArgsConstructor
// @Order(1) // Execute before DataInitializer
// public class DatabaseInitializer implements CommandLineRunner {

//     private final DataSource dataSource;
//     private final JdbcTemplate jdbcTemplate;

//     @Override
//     public void run(String... args) throws Exception {
//         log.info("🔍 Checking database schema status...");

//         if (isDatabaseEmpty()) {
//             log.info("📦 Database is empty - Creating schema for first-time setup");
//             createSchema();
//             log.info("✅ Schema created successfully");
//         } else {
//             log.info("✅ Database schema already exists - Skipping schema creation");
//         }
//     }

//     /**
//      * Checks if the database is empty by looking for core tables
//      */
//     private boolean isDatabaseEmpty() {
//         try (Connection connection = dataSource.getConnection()) {
//             DatabaseMetaData metaData = connection.getMetaData();
//             ResultSet tables = metaData.getTables(null, "public", "permissions", new String[]{"TABLE"});

//             boolean hasPermissionsTable = tables.next();

//             if (hasPermissionsTable) {
//                 log.debug("Found existing permissions table - database is not empty");
//                 return false;
//             } else {
//                 log.debug("No permissions table found - database is empty");
//                 return true;
//             }
//         } catch (Exception e) {
//             log.warn("Error checking database status: {}", e.getMessage());
//             // If we can't determine, assume empty to be safe
//             return true;
//         }
//     }

//     /**
//      * Creates the complete database schema
//      */
//     private void createSchema() {
//         try {
//             // Create core tables
//             createPermissionsTable();
//             createRolesTable();
//             createUsersTable();
//             createDevicesTable();
//             createVehiclesTable();

//             // Create join tables
//             createJoinTables();

//             // Create indexes
//             createIndexes();

//             // Create Flyway history table
//             createFlywayHistoryTable();

//             log.info("🎯 Database schema creation completed successfully");

//         } catch (Exception e) {
//             log.error("❌ Failed to create database schema: {}", e.getMessage(), e);
//             throw new RuntimeException("Database schema creation failed", e);
//         }
//     }

//     private void createPermissionsTable() {
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS permissions (
//                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//                 code VARCHAR(100) NOT NULL UNIQUE,
//                 name VARCHAR(200) NOT NULL,
//                 description TEXT,
//                 category VARCHAR(50) NOT NULL,
//                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//             )
//         """);
//     }

//     private void createRolesTable() {
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS roles (
//                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//                 name VARCHAR(100) NOT NULL UNIQUE,
//                 description VARCHAR(500),
//                 active BOOLEAN DEFAULT true,
//                 role_scope VARCHAR(50) DEFAULT 'TENANT',
//                 tenant_id UUID,
//                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//             )
//         """);
//     }

//     private void createUsersTable() {
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS users (
//                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//                 username VARCHAR(50) NOT NULL UNIQUE,
//                 email VARCHAR(100),
//                 password VARCHAR(255) NOT NULL,
//                 first_name VARCHAR(100) NOT NULL,
//                 last_name VARCHAR(100) NOT NULL,
//                 phone_number VARCHAR(20),
//                 active BOOLEAN DEFAULT true,
//                 tenant_id UUID,
//                 last_login TIMESTAMP,
//                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//             )
//         """);
//     }

//     private void createDevicesTable() {
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS devices (
//                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//                 device_id VARCHAR(100) NOT NULL UNIQUE,
//                 name VARCHAR(200) NOT NULL,
//                 type VARCHAR(50) NOT NULL,
//                 status VARCHAR(20) DEFAULT 'INACTIVE',
//                 registered_by_sms BOOLEAN DEFAULT false,
//                 tenant_id UUID,
//                 installed_at TIMESTAMP,
//                 expiry_at TIMESTAMP,
//                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//             )
//         """);
//     }

//     private void createVehiclesTable() {
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS vehicles (
//                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//                 license_plate VARCHAR(15) NOT NULL UNIQUE,
//                 brand VARCHAR(100) NOT NULL,
//                 model VARCHAR(100) NOT NULL,
//                 year INTEGER NOT NULL,
//                 vin VARCHAR(20),
//                 vehicle_type VARCHAR(20) NOT NULL,
//                 status VARCHAR(20) DEFAULT 'ACTIVE',
//                 tenant_id UUID,
//                 fleet_id UUID,
//                 device_id UUID,
//                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//             )
//         """);
//     }

//     private void createJoinTables() {
//         // Role-Permission mapping
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS role_permissions (
//                 role_id UUID NOT NULL,
//                 permission_id UUID NOT NULL,
//                 PRIMARY KEY (role_id, permission_id),
//                 FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
//                 FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
//             )
//         """);

//         // User-Role mapping
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS user_roles (
//                 user_id UUID NOT NULL,
//                 role_id UUID NOT NULL,
//                 PRIMARY KEY (user_id, role_id),
//                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
//                 FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
//             )
//         """);

//         // User-Device mapping
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS user_devices (
//                 user_id UUID NOT NULL,
//                 device_id UUID NOT NULL,
//                 PRIMARY KEY (user_id, device_id),
//                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
//                 FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
//             )
//         """);

//         // User-Vehicle mapping
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS user_vehicles (
//                 user_id UUID NOT NULL,
//                 vehicle_id UUID NOT NULL,
//                 PRIMARY KEY (user_id, vehicle_id),
//                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
//                 FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
//             )
//         """);
//     }

//     private void createIndexes() {
//         // User indexes
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_username ON users(username)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_email ON users(email)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_tenant ON users(tenant_id)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_active ON users(active)");

//         // Role indexes
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_role_name ON roles(name)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_role_tenant ON roles(tenant_id)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_role_active ON roles(active)");

//         // Permission indexes
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_permission_code ON permissions(code)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_permission_category ON permissions(category)");

//         // Device indexes
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_device_device_id ON devices(device_id)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_device_status ON devices(status)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_device_tenant ON devices(tenant_id)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_device_expiry ON devices(expiry_at)");

//         // Vehicle indexes
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vehicle_plate ON vehicles(license_plate)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vehicle_tenant ON vehicles(tenant_id)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vehicle_status ON vehicles(status)");
//         jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vehicle_brand ON vehicles(brand)");
//     }

//     private void createFlywayHistoryTable() {
//         jdbcTemplate.execute("""
//             CREATE TABLE IF NOT EXISTS flyway_schema_history (
//                 installed_rank INTEGER NOT NULL,
//                 version VARCHAR(50),
//                 description VARCHAR(200),
//                 type VARCHAR(20) NOT NULL,
//                 script VARCHAR(1000) NOT NULL,
//                 checksum INTEGER,
//                 installed_by VARCHAR(100) NOT NULL,
//                 installed_on TIMESTAMP NOT NULL DEFAULT now(),
//                 execution_time INTEGER NOT NULL,
//                 success BOOLEAN NOT NULL
//             )
//         """);

//         // Mark V1 migration as applied
//         jdbcTemplate.execute("""
//             INSERT INTO flyway_schema_history 
//             (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
//             VALUES (1, '1', 'init schema', 'SQL', 'V1__init_schema.sql', 1335123138, 'system', NOW(), 0, true)
//             ON CONFLICT DO NOTHING
//         """);
//     }
// }