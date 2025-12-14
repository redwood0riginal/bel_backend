-- Insert test user W00194 with password w00194 (BCrypt hashed)
-- Password: w00194
-- Generated BCrypt hash using strength 10

INSERT INTO auth_schema.users (
    ucode,
    first_name,
    last_name,
    email,
    password_hash,
    enabled,
    signatory,
    failed_attempts,
    created_at,
    updated_at
) VALUES (
    'W00194',
    'Test',
    'User',
    'w00194@test.com',
    '$2a$12$2DNOEBdHiX81vMF6pJwjcuyr.9XRIx067UZ6xpOF.PtF/ey.qd0lu',
    true,
    false,
    0,
    NOW(),
    NOW()
) ON CONFLICT (ucode) DO UPDATE SET 
    password_hash = '$2a$12$2DNOEBdHiX81vMF6pJwjcuyr.9XRIx067UZ6xpOF.PtF/ey.qd0lu',
    updated_at = NOW();
