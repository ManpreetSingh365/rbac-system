-- V1__init_schema.sql
-- Complete database schema for Fleet Management RBAC System
-- Matches all JPA entities exactly

-- Create permissions table
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    requires_scope BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id UUID,
    scope_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id UUID,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create devices table
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    imei VARCHAR(20) NOT NULL UNIQUE,
    device_model VARCHAR(100) NOT NULL,
    firmware_version VARCHAR(100),
    sim_number VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    tenant_id UUID,
    registered_by_sms BOOLEAN NOT NULL DEFAULT FALSE,
    installer_phone VARCHAR(20),
    last_heartbeat TIMESTAMP,
    expiry_at TIMESTAMP,
    installed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create vehicles table
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    license_plate VARCHAR(15) NOT NULL UNIQUE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    vin VARCHAR(20),
    vehicle_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tenant_id UUID,
    fleet_id UUID,
    device_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create join table for role_permissions
CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create join table for user_roles
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create join table for user_devices
CREATE TABLE user_devices (
    user_id UUID NOT NULL,
    device_id UUID NOT NULL,
    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create join table for user_vehicles
CREATE TABLE user_vehicles (
    user_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    PRIMARY KEY (user_id, vehicle_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Create indexes for permissions table
CREATE INDEX idx_permission_code ON permissions (code);
CREATE INDEX idx_permission_category ON permissions (category);
CREATE INDEX idx_permission_active ON permissions (active);

-- Create indexes for roles table
CREATE INDEX idx_role_name ON roles (name);
CREATE INDEX idx_role_tenant ON roles (tenant_id);
CREATE INDEX idx_role_active ON roles (active);

-- Create indexes for users table
CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_active ON users (active);
CREATE INDEX idx_user_tenant ON users (tenant_id);

-- Create indexes for devices table
CREATE INDEX idx_device_imei ON devices (imei);
CREATE INDEX idx_device_status ON devices (status);
CREATE INDEX idx_device_tenant ON devices (tenant_id);
CREATE INDEX idx_device_expiry ON devices (expiry_at);

-- Create indexes for vehicles table
CREATE INDEX idx_vehicle_plate ON vehicles (license_plate);
CREATE INDEX idx_vehicle_tenant ON vehicles (tenant_id);
CREATE INDEX idx_vehicle_status ON vehicles (status);
CREATE INDEX idx_vehicle_brand ON vehicles (brand);

-- Add foreign key constraint for vehicle device relationship
ALTER TABLE vehicles 
ADD CONSTRAINT fk_vehicle_device 
FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL;