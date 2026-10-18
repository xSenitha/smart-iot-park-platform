-- ============================================================================
-- V1: Initial Database Schema for Smart IoT Park Platform
-- ============================================================================

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. User Roles Mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Parks
CREATE TABLE IF NOT EXISTS parks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    total_area_sqm DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Zones
CREATE TABLE IF NOT EXISTS zones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    park_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    zone_type VARCHAR(50) NOT NULL,
    polygon_geojson TEXT,
    target_moisture_level DECIMAL(5, 2) NOT NULL DEFAULT 45.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_zones_park FOREIGN KEY (park_id) REFERENCES parks (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Devices
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    zone_id BIGINT NOT NULL,
    device_uid VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL, -- BIN, SOIL, LIGHT
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    battery_level DECIMAL(5, 2) DEFAULT 100.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, FAULT, MAINTENANCE
    last_heartbeat TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_devices_zone FOREIGN KEY (zone_id) REFERENCES zones (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Alert Rules
CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    device_type VARCHAR(30) NOT NULL,
    metric_name VARCHAR(50) NOT NULL,
    operator VARCHAR(10) NOT NULL, -- GREATER_THAN, LESS_THAN, EQUALS
    threshold_value DECIMAL(10, 2) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'WARNING', -- INFO, WARNING, CRITICAL
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Alerts
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    rule_id BIGINT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, ACKNOWLEDGED, RESOLVED
    triggered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolved_by BIGINT NULL,
    resolution_notes TEXT,
    CONSTRAINT fk_alerts_device FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE,
    CONSTRAINT fk_alerts_rule FOREIGN KEY (rule_id) REFERENCES alert_rules (id) ON DELETE SET NULL,
    CONSTRAINT fk_alerts_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Maintenance Tickets
CREATE TABLE IF NOT EXISTS maintenance_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    park_id BIGINT NOT NULL,
    zone_id BIGINT NOT NULL,
    device_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',   -- PENDING, IN_PROGRESS, RESOLVED, CLOSED
    assigned_to BIGINT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',    -- MANUAL, AI_PREDICTION, ALERT_AUTO
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_tickets_park FOREIGN KEY (park_id) REFERENCES parks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tickets_zone FOREIGN KEY (zone_id) REFERENCES zones (id) ON DELETE CASCADE,
    CONSTRAINT fk_tickets_device FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE SET NULL,
    CONSTRAINT fk_tickets_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NULL,
    ip_address VARCHAR(50),
    details_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for Query Performance
CREATE INDEX idx_devices_type_status ON devices (type, status);
CREATE INDEX idx_alerts_status_severity ON alerts (status, severity);
CREATE INDEX idx_tickets_status_priority ON maintenance_tickets (status, priority);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);
