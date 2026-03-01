-- Add full LMS course for saisseru@gmail.com (product 1)
-- Run: mysql -u root -proot quantum_education < docs/migrations/add-lms-course-saisseru.sql
-- Does NOT delete any existing data. Safe to run multiple times.
-- Ensures: user exists, product 1 has full attributes/modules/content, user owns course.

-- ---------------------------------------------------------------------------
-- 1. Create user saisseru@gmail.com if not exists (password: password123)
-- ---------------------------------------------------------------------------
INSERT INTO auth_user (email, password_hash, role, is_verified)
SELECT 'saisseru@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM auth_user WHERE email = 'saisseru@gmail.com');

SET @user_id = (SELECT id FROM auth_user WHERE email = 'saisseru@gmail.com' LIMIT 1);

INSERT INTO user_profile (user_id, first_name, last_name, phone)
SELECT @user_id, 'User', 'Saisseru', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_profile WHERE user_id = @user_id);

-- ---------------------------------------------------------------------------
-- 2. Add product_module for product 1 if not exists
-- ---------------------------------------------------------------------------
INSERT INTO product_module (product_id, title, order_index)
SELECT 1, 'Module 1: Skin Basics', 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM product WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM product_module WHERE product_id = 1);

SET @mod_id = (SELECT id FROM product_module WHERE product_id = 1 ORDER BY order_index LIMIT 1);

UPDATE product_content SET module_id = @mod_id WHERE product_id = 1 AND (module_id IS NULL OR module_id = 0);

-- ---------------------------------------------------------------------------
-- 3. Update product 1 with full attributes and media placeholders
-- ---------------------------------------------------------------------------
UPDATE product
SET
    thumbnail_url = 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/thumbnail.jpg',
    preview_video_url = 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/video.mp4',
    is_featured = 1,
    attributes = JSON_OBJECT(
        'badge', 'BESTSELLER',
        'highlights', JSON_ARRAY(
            JSON_OBJECT('icon', 'clock', 'label', 'Duration', 'value', '8 hours'),
            JSON_OBJECT('icon', 'users', 'label', 'Students', 'value', '2.5k+'),
            JSON_OBJECT('icon', 'award', 'label', 'Level', 'value', 'Beginner')
        ),
        'learningOutcomes', JSON_ARRAY(
            'Identify your skin type and build a personalized routine',
            'Understand cleansers, serums, moisturizers, and sun protection',
            'Apply professional techniques for common skin concerns'
        ),
        'instructor', JSON_OBJECT(
            'name', 'Dr. Alex Carter',
            'role', 'Lead Skincare Instructor',
            'imageUrl', 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/thumbnail.jpg',
            'bio', 'Board-certified dermatologist with 15+ years of experience in clinical and cosmetic dermatology.',
            'credentials', JSON_ARRAY('MD, FAAD', 'Certified Skincare Specialist')
        ),
        'certification', JSON_OBJECT(
            'icon', 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/thumbnail.jpg',
            'title', 'Professional Skincare Certificate',
            'description', 'Earn a certificate upon completion to showcase your expertise.',
            'highlights', JSON_ARRAY('Industry-recognized', 'Shareable on LinkedIn')
        ),
        'outcomeHighlights', JSON_ARRAY(
            JSON_OBJECT('title', 'Career Ready', 'description', 'Skills applicable in spas, clinics, and wellness centers.'),
            JSON_OBJECT('title', 'Self-Care Mastery', 'description', 'Build a sustainable routine for healthy, glowing skin.')
        )
    )
WHERE id = 1;

-- ---------------------------------------------------------------------------
-- 4. Ensure lessons have media URLs (placeholders)
-- ---------------------------------------------------------------------------
UPDATE lesson l
JOIN product_content pc ON l.product_content_id = pc.id
SET
    l.video_url = CASE WHEN l.lesson_type = 'VIDEO' THEN 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/video.mp4' ELSE l.video_url END,
    l.pdf_url = CASE WHEN l.lesson_type = 'PDF' AND (l.pdf_url IS NULL OR l.pdf_url = '') 
        THEN 'https://example.com/placeholders/lesson-handout.pdf' ELSE l.pdf_url END
WHERE pc.product_id = 1;

-- ---------------------------------------------------------------------------
-- 5. Create order + course_ownership for saisseru -> product 1
-- ---------------------------------------------------------------------------
INSERT INTO orders (
    user_id, product_id, price, discount_price, final_price, gst_amount,
    billing_name, billing_address_line1, billing_address_line2,
    billing_city, billing_state, billing_country, billing_postal_code,
    billing_gst_number, status
)
SELECT @user_id, 1, 2999.00, 1999.00, 1999.00, 359.82,
    'Saisseru User', '123 Test Street', NULL,
    'Mumbai', 'Maharashtra', 'India', '400001',
    NULL, 'SUCCESS'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_ownership WHERE user_id = @user_id AND course_id = 1);

INSERT INTO course_ownership (user_id, course_id, order_id, purchased_at)
SELECT @user_id, 1,
    COALESCE(
        (SELECT id FROM orders WHERE user_id = @user_id AND product_id = 1 ORDER BY id DESC LIMIT 1),
        LAST_INSERT_ID()
    ),
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_ownership WHERE user_id = @user_id AND course_id = 1);
