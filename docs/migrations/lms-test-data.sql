-- LMS Migration: Create user_lesson_progress table (if not exists) and add test data
-- Run: mysql -u root -p quantum_education < docs/migrations/lms-test-data.sql
-- Does NOT delete any existing data. Safe to run multiple times.

-- ---------------------------------------------------------------------------
-- 1. Create user_lesson_progress table if not exists
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_lesson_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_content_id BIGINT NOT NULL,
    completed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_progress_user_content (user_id, product_content_id),
    KEY idx_progress_user (user_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES auth_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_content FOREIGN KEY (product_content_id) REFERENCES product_content (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 2. Ensure user 2 owns product 1 (for LMS access)
--    Creates order + course_ownership only if ownership doesn't exist.
--    Assumes auth_user id=2 and product id=1 exist.
-- ---------------------------------------------------------------------------
-- Insert order (needed for course_ownership FK) only when ownership doesn't exist
INSERT INTO orders (
    user_id, product_id, price, discount_price, final_price, gst_amount,
    billing_name, billing_address_line1, billing_address_line2,
    billing_city, billing_state, billing_country, billing_postal_code,
    billing_gst_number, status
)
SELECT 2, 1, 2999.00, 1999.00, 1999.00, 359.82,
    'Test User', '123 Test Street', NULL,
    'Mumbai', 'Maharashtra', 'India', '400001',
    NULL, 'SUCCESS'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_ownership WHERE user_id = 2 AND course_id = 1);

-- Link ownership to the order we just created (or use existing order)
INSERT INTO course_ownership (user_id, course_id, order_id, purchased_at)
SELECT 2, 1, COALESCE(
    (SELECT id FROM orders WHERE user_id = 2 AND product_id = 1 ORDER BY id DESC LIMIT 1),
    LAST_INSERT_ID()
), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_ownership WHERE user_id = 2 AND course_id = 1);

-- ---------------------------------------------------------------------------
-- 3. Optional: Add instructor to product 1 for richer LMS response
--    (Only updates if attributes is NULL or doesn't have instructor)
-- ---------------------------------------------------------------------------
UPDATE product
SET attributes = JSON_SET(COALESCE(attributes, '{}'), '$.instructor', JSON_OBJECT('name', 'Dr. Alex Carter', 'role', 'Lead Instructor'))
WHERE id = 1 AND (attributes IS NULL OR JSON_EXTRACT(attributes, '$.instructor') IS NULL);
