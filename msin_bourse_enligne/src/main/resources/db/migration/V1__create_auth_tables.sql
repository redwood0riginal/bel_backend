-- Create auth schema tables

-- User Profiles
CREATE TABLE auth_schema.user_profiles (
    id SERIAL PRIMARY KEY,
    ucode VARCHAR(50) UNIQUE,
    name VARCHAR(100),
    admin_role BOOLEAN NOT NULL DEFAULT FALSE,
    skip_controls BOOLEAN NOT NULL DEFAULT FALSE,
    category_id VARCHAR(50),
    network_id INTEGER,
    crm_clt_categ VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_profile_ucode ON auth_schema.user_profiles(ucode);

-- Users
CREATE TABLE auth_schema.users (
    id BIGSERIAL PRIMARY KEY,
    ucode VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    signatory BOOLEAN NOT NULL DEFAULT FALSE,
    employee_number VARCHAR(50),
    function VARCHAR(100),
    profile_id INTEGER REFERENCES auth_schema.user_profiles(id),
    manager_id BIGINT,
    category_id VARCHAR(50),
    password_date TIMESTAMP,
    grace_date TIMESTAMP,
    failed_attempts INTEGER DEFAULT 0,
    lock_time TIMESTAMP,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_users_email ON auth_schema.users(email);
CREATE INDEX idx_users_ucode ON auth_schema.users(ucode);
CREATE INDEX idx_users_profile ON auth_schema.users(profile_id);

-- Refresh Tokens
CREATE TABLE auth_schema.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth_schema.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON auth_schema.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON auth_schema.refresh_tokens(token);

-- Insert default profiles
INSERT INTO auth_schema.user_profiles (ucode, name, admin_role, skip_controls) VALUES
('ADMIN', 'Administrator', TRUE, TRUE),
('INVESTOR', 'Investor', FALSE, FALSE),
('TRADER', 'Trader', FALSE, FALSE),
('VIEWER', 'Viewer', FALSE, FALSE);

-- Insert default admin user (password: admin123)
INSERT INTO auth_schema.users (ucode, first_name, last_name, email, password_hash, enabled, signatory, profile_id)
VALUES ('ADMIN001', 'Admin', 'User', 'admin@msinbourse.com', '$2a$10$xqnY7Z5JZ5Z5Z5Z5Z5Z5ZeK5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', TRUE, TRUE, 1);
