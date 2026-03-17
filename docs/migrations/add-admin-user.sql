-- Add admin user for E2E testing (if not exists)
-- Run: mysql -u root -p quantum_education < docs/migrations/add-admin-user.sql
-- Password: password123 (bcrypt hash)

INSERT IGNORE INTO auth_user (email, password_hash, role, is_verified)
VALUES ('admin@quantumedu.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1);

-- Ensure admin has profile (user_id = id from auth_user for admin@quantumedu.com)
INSERT IGNORE INTO user_profile (user_id, first_name, last_name, phone)
SELECT id, 'Admin', 'User', '+919876543210' FROM auth_user WHERE email = 'admin@quantumedu.com' LIMIT 1;
