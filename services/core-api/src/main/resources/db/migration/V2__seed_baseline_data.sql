-- ============================================================================
-- V2: Baseline Demonstration Seed Data for Smart IoT Park Platform
-- Passwords:
--   admin       -> Admin123!
--   planner     -> Planner123!
--   maintenance -> Tech123!
-- ============================================================================

-- 1. Seed Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Platform Administrator with full municipal control'),
(2, 'ROLE_CITY_PLANNER', 'City Urban Planner managing parks, zones, and analytical reports'),
(3, 'ROLE_MAINTENANCE_CREW', 'Field technician handling device repairs, alerts, and work orders')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Seed Users (BCrypt hashes for Admin123!, Planner123!, Tech123!)
INSERT INTO users (id, username, email, password_hash, full_name, is_active, created_at, updated_at) VALUES
(1, 'admin', 'senithagihean@gmail.com', '$2a$10$ZP9IZvG9lu7bHkT3bcU5f.16g0IQ64fLsxOClCm2bMEtKaHiwdyxi', 'Senitha Gihen', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'planner', 'planner@smartpark.local', '$2a$10$Aryzt4Zgt.SQhU2VORwDceijoXXFiM1rXP4f/i2Kot/H4PFjpf3jO', 'Urban Planner Jane', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'maintenance', 'tech@smartpark.local', '$2a$10$zVroNNH93ZCXgNc8DlbZN.XXiaD.lv/Vbf2eFwIquPYAFHjeR86T6', 'Technician Alex', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 3. Seed User Roles Mapping
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- planner -> ROLE_CITY_PLANNER
(3, 3)  -- maintenance -> ROLE_MAINTENANCE_CREW
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 4. Seed Municipal Park
INSERT INTO parks (id, name, location, total_area_sqm, timezone, status, created_at, updated_at) VALUES
(1, 'Metropolitan Central Park', 'Downtown Urban Center, District 4', 550000.00, 'UTC', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 5. Seed Park Zones
INSERT INTO zones (id, park_id, name, zone_type, polygon_geojson, target_moisture_level, created_at, updated_at) VALUES
(1, 1, 'Central Plaza & Promenade', 'PLAZA', '{"type":"Polygon","coordinates":[[[-73.97,40.78],[-73.96,40.78],[-73.96,40.77],[-73.97,40.77],[-73.97,40.78]]]}', 40.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'North Botanical Gardens', 'BOTANICAL', '{"type":"Polygon","coordinates":[[[-73.96,40.79],[-73.95,40.79],[-73.95,40.78],[-73.96,40.78],[-73.96,40.79]]]}', 55.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'West Sports & Recreation Complex', 'SPORTS_LAWN', '{"type":"Polygon","coordinates":[[[-73.98,40.77],[-73.97,40.77],[-73.97,40.76],[-73.98,40.76],[-73.98,40.77]]]}', 35.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 6. Seed IoT Devices
INSERT INTO devices (id, zone_id, device_uid, name, type, model, firmware_version, battery_level, status, last_heartbeat, created_at, updated_at) VALUES
-- Smart Bins (Zone 1, 2, 3)
(1, 1, 'DEV-BIN-0001', 'Plaza North Solar Bin', 'BIN', 'EcoCompactor-X1', 'v2.1.0', 94.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'DEV-BIN-0002', 'Plaza Fountain South Bin', 'BIN', 'EcoCompactor-X1', 'v2.1.0', 88.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'DEV-BIN-0003', 'Botanical Rose Garden Bin', 'BIN', 'EcoCompactor-X1', 'v2.1.0', 91.20, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'DEV-BIN-0004', 'Botanical Greenhouse Bin', 'BIN', 'EcoCompactor-X1', 'v2.0.4', 79.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 3, 'DEV-BIN-0005', 'Sports Bleachers Bin', 'BIN', 'EcoCompactor-X1', 'v2.1.0', 82.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 'DEV-BIN-0006', 'Sports Pavilion Bin', 'BIN', 'EcoCompactor-X1', 'v2.1.0', 65.40, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Soil Moisture Sensors (Zone 1, 2, 3)
(7, 1, 'DEV-SOIL-0001', 'Central Lawn Soil Monitor', 'SOIL', 'TerraProbe-S4', 'v1.4.2', 96.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, 'DEV-SOIL-0002', 'Botanical Orchid Bed Probe', 'SOIL', 'TerraProbe-S4', 'v1.4.2', 92.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 2, 'DEV-SOIL-0003', 'Botanical Arboretum Probe', 'SOIL', 'TerraProbe-S4', 'v1.4.2', 89.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 3, 'DEV-SOIL-0004', 'Football Pitch East Probe', 'SOIL', 'TerraProbe-S4', 'v1.4.2', 78.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 3, 'DEV-SOIL-0005', 'Football Pitch West Probe', 'SOIL', 'TerraProbe-S4', 'v1.4.2', 84.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Smart Lighting Poles (Zone 1, 2, 3)
(12, 1, 'DEV-LIGHT-0001', 'Plaza Entrance Solar Luminaire', 'LIGHT', 'LumiGrid-SmartLED', 'v3.0.1', 98.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 2, 'DEV-LIGHT-0002', 'Botanical Pathway LED 01', 'LIGHT', 'LumiGrid-SmartLED', 'v3.0.1', 95.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 3, 'DEV-LIGHT-0003', 'Jogging Track LED Pole A', 'LIGHT', 'LumiGrid-SmartLED', 'v3.0.1', 89.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 3, 'DEV-LIGHT-0004', 'Jogging Track LED Pole B', 'LIGHT', 'LumiGrid-SmartLED', 'v3.0.1', 91.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 7. Seed Alert Rules
INSERT INTO alert_rules (id, name, device_type, metric_name, operator, threshold_value, severity, is_active, created_at) VALUES
(1, 'Bin Overflow Warning', 'BIN', 'fillPercentage', 'GREATER_THAN', 85.00, 'WARNING', TRUE, CURRENT_TIMESTAMP),
(2, 'Bin Overflow Critical', 'BIN', 'fillPercentage', 'GREATER_THAN', 95.00, 'CRITICAL', TRUE, CURRENT_TIMESTAMP),
(3, 'Low Soil Moisture Deficit', 'SOIL', 'soilMoisture', 'LESS_THAN', 20.00, 'WARNING', TRUE, CURRENT_TIMESTAMP),
(4, 'Critical Device Battery Level', 'BIN', 'batteryLevel', 'LESS_THAN', 15.00, 'CRITICAL', TRUE, CURRENT_TIMESTAMP),
(5, 'Light Malfunction / Lux Failure', 'LIGHT', 'lightLux', 'LESS_THAN', 5.00, 'WARNING', TRUE, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 8. Seed Initial Demonstration Alert
INSERT INTO alerts (id, device_id, rule_id, alert_type, severity, title, message, status, triggered_at) VALUES
(1, 1, 1, 'THRESHOLD_EXCEEDED', 'WARNING', 'High Fill Level: Plaza North Solar Bin', 'Bin has reached 86.4% capacity. Scheduled collection recommended.', 'OPEN', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 9. Seed Initial Demonstration Maintenance Ticket
INSERT INTO maintenance_tickets (id, park_id, zone_id, device_id, title, description, priority, status, assigned_to, source, created_at, updated_at) VALUES
(1, 1, 1, 1, 'Routine Bin Emptying & Sensor Check', 'Empty Plaza North Bin before afternoon crowd surge. Verify ultrasonic sensor calibration.', 'MEDIUM', 'IN_PROGRESS', 3, 'MANUAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE title=VALUES(title);
