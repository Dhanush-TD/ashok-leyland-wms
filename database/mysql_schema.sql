-- =============================================================================
-- ASHOK LEYLAND ENTERPRISE WMS - MYSQL PRODUCTION DATABASE SCHEMA
-- Target Database: MySQL 8.0+
-- Converted from the original PostgreSQL schema (UUID -> CHAR(36), ENUM types
-- inlined, SERIAL -> AUTO_INCREMENT, JSONB -> JSON, TIMESTAMPTZ -> DATETIME)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ashok_leyland_wms
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ashok_leyland_wms;

-- 1. USER & ROLE MANAGEMENT
CREATE TABLE users (
    user_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(30) UNIQUE NOT NULL,
    role ENUM('OPERATOR','SUPERVISOR','ADMIN') NOT NULL DEFAULT 'OPERATOR',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME NULL
) ENGINE=InnoDB;

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_badge ON users(badge_id);

-- 2. ENGINE MODEL SPECIFICATIONS
CREATE TABLE engine_models (
    model_id INT AUTO_INCREMENT PRIMARY KEY,
    model_code VARCHAR(30) UNIQUE NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    engine_type VARCHAR(50) NOT NULL,
    displacement_cc INT NOT NULL,
    power_hp INT NOT NULL,
    weight_kg DECIMAL(8,2) NOT NULL,
    dimensions_mm VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

-- 3. WAREHOUSE STORAGE MATRIX (CINEMA GRID A-Z, 1-50)
CREATE TABLE warehouse_locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    location_code VARCHAR(10) UNIQUE NOT NULL,
    row_code VARCHAR(5) NOT NULL,
    col_number INT NOT NULL,
    zone_name VARCHAR(30) DEFAULT 'MAIN_BAY',
    is_temp_allowed BOOLEAN DEFAULT TRUE,
    status ENUM('AVAILABLE','OCCUPIED','PENDING_CONFIRMATION','RESERVED_TEMP','BLOCKED','RELOCATING')
        NOT NULL DEFAULT 'AVAILABLE',
    current_engine_id CHAR(36) NULL,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_locations_code ON warehouse_locations(location_code);
CREATE INDEX idx_locations_row_col ON warehouse_locations(row_code, col_number);
CREATE INDEX idx_locations_status ON warehouse_locations(status);

-- 4. ENGINES INVENTORY
CREATE TABLE engines (
    engine_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    engine_number VARCHAR(50) UNIQUE NOT NULL,
    barcode VARCHAR(100) UNIQUE NOT NULL,
    model_id INT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    mfg_date DATE NOT NULL,
    arrival_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    current_status ENUM('STORED','PENDING_PLACEMENT','IN_RELOCATION','RETRIEVED_DISPATCHED','BLOCKED_BY_FRONT')
        NOT NULL DEFAULT 'PENDING_PLACEMENT',
    current_location_id INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_engine_model FOREIGN KEY (model_id) REFERENCES engine_models(model_id),
    CONSTRAINT fk_engine_location FOREIGN KEY (current_location_id) REFERENCES warehouse_locations(location_id)
) ENGINE=InnoDB;

ALTER TABLE warehouse_locations
  ADD CONSTRAINT fk_location_engine
  FOREIGN KEY (current_engine_id) REFERENCES engines(engine_id) ON DELETE SET NULL;

CREATE INDEX idx_engines_number ON engines(engine_number);
CREATE INDEX idx_engines_barcode ON engines(barcode);
CREATE INDEX idx_engines_batch ON engines(batch_number);
CREATE INDEX idx_engines_status ON engines(current_status);

-- 5. RETRIEVAL ORDERS & BULK DISPATCH
CREATE TABLE retrieval_orders (
    order_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    supervisor_id CHAR(36) NOT NULL,
    excel_filename VARCHAR(255) NULL,
    total_engines INT NOT NULL DEFAULT 0,
    status ENUM('DRAFT','APPROVED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    approved_at DATETIME NULL,
    completed_at DATETIME NULL,
    CONSTRAINT fk_order_supervisor FOREIGN KEY (supervisor_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE retrieval_order_items (
    item_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    order_id CHAR(36) NOT NULL,
    engine_id CHAR(36) NOT NULL,
    retrieval_sequence INT NOT NULL,
    requires_relocation BOOLEAN DEFAULT FALSE,
    relocation_count INT DEFAULT 0,
    status VARCHAR(30) DEFAULT 'PENDING',
    retrieved_at DATETIME NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES retrieval_orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_engine FOREIGN KEY (engine_id) REFERENCES engines(engine_id)
) ENGINE=InnoDB;

-- 6. INTELLIGENT TEMPORARY RELOCATION RECORDS
CREATE TABLE temporary_relocation_records (
    relocation_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    order_id CHAR(36) NOT NULL,
    target_engine_id CHAR(36) NOT NULL,
    blocking_engine_id CHAR(36) NOT NULL,
    original_location_id INT NOT NULL,
    temporary_location_id INT NOT NULL,
    sequence_order INT NOT NULL,
    state ENUM('SCHEDULED','FRONT_MOVED_TEMP','TARGET_RETRIEVED','FRONT_RESTORED') NOT NULL DEFAULT 'SCHEDULED',
    operator_id CHAR(36) NULL,
    moved_to_temp_at DATETIME NULL,
    restored_at DATETIME NULL,
    CONSTRAINT fk_reloc_order FOREIGN KEY (order_id) REFERENCES retrieval_orders(order_id),
    CONSTRAINT fk_reloc_target FOREIGN KEY (target_engine_id) REFERENCES engines(engine_id),
    CONSTRAINT fk_reloc_blocker FOREIGN KEY (blocking_engine_id) REFERENCES engines(engine_id),
    CONSTRAINT fk_reloc_orig_loc FOREIGN KEY (original_location_id) REFERENCES warehouse_locations(location_id),
    CONSTRAINT fk_reloc_temp_loc FOREIGN KEY (temporary_location_id) REFERENCES warehouse_locations(location_id),
    CONSTRAINT fk_reloc_operator FOREIGN KEY (operator_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- 7. REAL-TIME MOVEMENT AUDIT LOGS
CREATE TABLE movement_logs (
    movement_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    engine_id CHAR(36) NOT NULL,
    operator_id CHAR(36) NOT NULL,
    from_location_id INT NULL,
    to_location_id INT NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    scanned_engine_barcode VARCHAR(100) NOT NULL,
    scanned_location_barcode VARCHAR(100) NOT NULL,
    confirmation_status VARCHAR(30) NOT NULL,
    initiated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME NULL,
    CONSTRAINT fk_move_engine FOREIGN KEY (engine_id) REFERENCES engines(engine_id),
    CONSTRAINT fk_move_operator FOREIGN KEY (operator_id) REFERENCES users(user_id),
    CONSTRAINT fk_move_from FOREIGN KEY (from_location_id) REFERENCES warehouse_locations(location_id),
    CONSTRAINT fk_move_to FOREIGN KEY (to_location_id) REFERENCES warehouse_locations(location_id)
) ENGINE=InnoDB;

CREATE INDEX idx_movements_engine ON movement_logs(engine_id);
CREATE INDEX idx_movements_operator ON movement_logs(operator_id);

-- 8. SYSTEM AUDIT TRAIL
CREATE TABLE audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NULL,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    payload_json JSON NULL,
    ip_address VARCHAR(45) NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- 9. SEED DEMO DATA
INSERT INTO engine_models (model_code, model_name, engine_type, displacement_cc, power_hp, weight_kg, dimensions_mm) VALUES
('AL-H-6CYL-220', 'Ashok Leyland H-Series 6-Cylinder 220HP', 'Diesel', 5759, 220, 680.00, '1150x780x1050'),
('AL-A-4CYL-160', 'Ashok Leyland A-Series 4-Cylinder 160HP', 'Diesel', 3839, 160, 490.00, '980x680x920'),
('AL-CNG-6CYL-200', 'Ashok Leyland iGen6 CNG 200HP', 'CNG', 5759, 200, 710.00, '1180x800x1080');

-- Populate Sample Grid Locations (Rows A-H, Cols 1-12) using a numbers helper
DROP TEMPORARY TABLE IF EXISTS tmp_cols;
CREATE TEMPORARY TABLE tmp_cols (n INT);
INSERT INTO tmp_cols (n) VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12);

DROP TEMPORARY TABLE IF EXISTS tmp_rows;
CREATE TEMPORARY TABLE tmp_rows (r CHAR(1));
INSERT INTO tmp_rows (r) VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G'),('H');

INSERT IGNORE INTO warehouse_locations (location_code, row_code, col_number, status)
SELECT CONCAT(tr.r, tc.n), tr.r, tc.n, 'AVAILABLE'
FROM tmp_rows tr CROSS JOIN tmp_cols tc;

-- Sample stored engines (matches the worked example in API_Documentation.md #4:
-- AL-ENG-2026-9041/9042/9043 block AL-ENG-2026-9044 in row A)
INSERT INTO engines (engine_number, barcode, model_id, batch_number, mfg_date, current_status, current_location_id) VALUES
('AL-ENG-2026-9041', 'AL-89410', 1, 'BATCH-2026-Q3', '2026-06-15',
    'STORED', (SELECT location_id FROM warehouse_locations WHERE location_code = 'A1')),
('AL-ENG-2026-9042', 'AL-89411', 1, 'BATCH-2026-Q3', '2026-06-15',
    'STORED', (SELECT location_id FROM warehouse_locations WHERE location_code = 'A2')),
('AL-ENG-2026-9043', 'AL-89412', 2, 'BATCH-2026-Q3', '2026-06-16',
    'STORED', (SELECT location_id FROM warehouse_locations WHERE location_code = 'A3')),
('AL-ENG-2026-9044', 'AL-89413', 2, 'BATCH-2026-Q3', '2026-06-16',
    'STORED', (SELECT location_id FROM warehouse_locations WHERE location_code = 'A4'));

UPDATE warehouse_locations SET status = 'OCCUPIED', current_engine_id =
    (SELECT engine_id FROM engines WHERE engine_number = 'AL-ENG-2026-9041') WHERE location_code = 'A1';
UPDATE warehouse_locations SET status = 'OCCUPIED', current_engine_id =
    (SELECT engine_id FROM engines WHERE engine_number = 'AL-ENG-2026-9042') WHERE location_code = 'A2';
UPDATE warehouse_locations SET status = 'OCCUPIED', current_engine_id =
    (SELECT engine_id FROM engines WHERE engine_number = 'AL-ENG-2026-9043') WHERE location_code = 'A3';
UPDATE warehouse_locations SET status = 'OCCUPIED', current_engine_id =
    (SELECT engine_id FROM engines WHERE engine_number = 'AL-ENG-2026-9044') WHERE location_code = 'A4';

-- A spare unplaced engine, ready for the placement/scan demo workflow
INSERT INTO engines (engine_number, barcode, model_id, batch_number, mfg_date, current_status) VALUES
('AL-ENG-2026-9901', 'AL-99012', 3, 'BATCH-2026-Q3', '2026-07-01', 'PENDING_PLACEMENT');

-- Default admin/supervisor/operator users
-- Password hash below is bcrypt for the demo password "Password123" (change before production use)
INSERT INTO users (username, email, password_hash, full_name, badge_id, role) VALUES
('admin', 'admin@ashokleyland.com', '$2b$10$LG0PXOOq6e2vXMVzbmhgK.CMhaiC4PG6gwDKa2rVmwo6smeoBWaFe', 'System Administrator', 'EMP-0001', 'ADMIN'),
('supervisor1', 'supervisor1@ashokleyland.com', '$2b$10$LG0PXOOq6e2vXMVzbmhgK.CMhaiC4PG6gwDKa2rVmwo6smeoBWaFe', 'Suresh Nair', 'EMP-5021', 'SUPERVISOR'),
('operator1', 'operator1@ashokleyland.com', '$2b$10$LG0PXOOq6e2vXMVzbmhgK.CMhaiC4PG6gwDKa2rVmwo6smeoBWaFe', 'Ramesh Kumar', 'EMP-8042', 'OPERATOR');
