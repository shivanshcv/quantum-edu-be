-- Quantum Education - Test/Seed Data
-- Run: mysql -u root -p quantum_education < docs/seed-test-data.sql
-- Or: mysql -u root -proot quantum_education < docs/seed-test-data.sql
-- Note: If Asia/Kolkata timezone is not loaded, comment out or remove the SET time_zone line below.

-- SET time_zone = 'Asia/Kolkata';

-- Clear existing data (reverse dependency order)
DELETE FROM assessment_option;
DELETE FROM assessment_question;
DELETE FROM assessment;
DELETE FROM lesson;
DELETE FROM product_content;
DELETE FROM product_category;
DELETE FROM product;
DELETE FROM category;
DELETE FROM user_profile;
DELETE FROM auth_user;

-- Reset auto-increment (optional, for predictable IDs)
ALTER TABLE auth_user AUTO_INCREMENT = 1;
ALTER TABLE user_profile AUTO_INCREMENT = 1;
ALTER TABLE category AUTO_INCREMENT = 1;
ALTER TABLE product AUTO_INCREMENT = 1;
ALTER TABLE product_content AUTO_INCREMENT = 1;
ALTER TABLE lesson AUTO_INCREMENT = 1;
ALTER TABLE assessment AUTO_INCREMENT = 1;
ALTER TABLE assessment_question AUTO_INCREMENT = 1;
ALTER TABLE assessment_option AUTO_INCREMENT = 1;

-- ---------------------------------------------------------------------------
-- 1. auth_user (password: password123 - bcrypt hash)
-- ---------------------------------------------------------------------------
INSERT INTO auth_user (email, password_hash, role, is_verified) VALUES
('admin@quantumedu.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1),
('user@quantumedu.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 1);

-- ---------------------------------------------------------------------------
-- 2. user_profile
-- ---------------------------------------------------------------------------
INSERT INTO user_profile (user_id, first_name, last_name, phone) VALUES
(1, 'Admin', 'User', '+919876543210'),
(2, 'Test', 'User', '+919876543211');

-- ---------------------------------------------------------------------------
-- 3. category (Beauty > Skincare, Makeup; Science > Physics)
-- ---------------------------------------------------------------------------
INSERT INTO category (name, slug, parent_id, level, is_active) VALUES
('Beauty', 'beauty', NULL, 0, 1),
('Science', 'science', NULL, 0, 1);

INSERT INTO category (name, slug, parent_id, level, is_active) VALUES
('Skincare', 'skincare', 1, 1, 1),
('Makeup', 'makeup', 1, 1, 1),
('Physics', 'physics', 2, 1, 1);

INSERT INTO category (name, slug, parent_id, level, is_active) VALUES
('Anti-Aging', 'anti-aging', 3, 2, 1);

-- ---------------------------------------------------------------------------
-- 4. product (2 published, 1 unpublished for testing)
-- ---------------------------------------------------------------------------
INSERT INTO product (title, slug, short_description, long_description, price, discount_price, thumbnail_url, preview_video_url, difficulty_level, duration_minutes, is_published) VALUES
('Complete Skincare Masterclass', 'complete-skincare-masterclass',
 'Learn professional skincare techniques from experts.',
 'A comprehensive course covering cleansers, serums, moisturizers, sun protection, and advanced treatments. Perfect for beginners and enthusiasts alike. Includes practical demonstrations and product recommendations.',
 2999.00, 1999.00, 'https://example.com/thumb1.jpg', 'https://example.com/preview1.mp4', 'BEGINNER', 480, 1),

('Quantum Physics Fundamentals', 'quantum-physics-fundamentals',
 'Introduction to quantum mechanics and its applications.',
 'Explore wave-particle duality, Schrödinger equation, quantum entanglement, and applications in technology. Designed for students with basic physics knowledge.',
 4999.00, NULL, 'https://example.com/thumb2.jpg', NULL, 'INTERMEDIATE', 600, 1),

('Advanced Makeup Artistry', 'advanced-makeup-artistry',
 'Master professional makeup techniques.',
 'Dive deep into contouring, color theory, bridal makeup, and editorial looks. For makeup artists and enthusiasts ready to level up.',
 3999.00, 3499.00, NULL, 'https://example.com/preview3.mp4', 'ADVANCED', 360, 0);

-- ---------------------------------------------------------------------------
-- 5. product_category (many-to-many)
-- ---------------------------------------------------------------------------
INSERT INTO product_category (product_id, category_id) VALUES
(1, 1), (1, 3), (1, 6),  -- Skincare Masterclass: Beauty, Skincare, Anti-Aging
(2, 2), (2, 5),           -- Quantum Physics: Science, Physics
(3, 1), (3, 4);           -- Makeup: Beauty, Makeup

-- ---------------------------------------------------------------------------
-- 6. product_content (Product 1: 2 lessons + 1 assessment; Product 2: 1 lesson + 1 assessment)
-- ---------------------------------------------------------------------------
INSERT INTO product_content (product_id, content_type, title, order_index, is_mandatory) VALUES
(1, 'LESSON', 'Introduction to Skincare', 0, 1),
(1, 'LESSON', 'Understanding Your Skin Type', 1, 1),
(1, 'ASSESSMENT', 'Module 1 Quiz', 2, 1),
(2, 'LESSON', 'Wave-Particle Duality', 0, 1),
(2, 'ASSESSMENT', 'Quantum Basics Quiz', 1, 1);

-- ---------------------------------------------------------------------------
-- 7. lesson
-- ---------------------------------------------------------------------------
INSERT INTO lesson (product_content_id, lesson_type, video_url, pdf_url, duration_seconds) VALUES
(1, 'VIDEO', 'https://example.com/videos/intro-skincare.mp4', NULL, 900),
(2, 'PDF', NULL, 'https://example.com/pdfs/skin-type.pdf', 600),
(4, 'VIDEO', 'https://example.com/videos/wave-particle.mp4', NULL, 1200);

-- ---------------------------------------------------------------------------
-- 8. assessment
-- ---------------------------------------------------------------------------
INSERT INTO assessment (product_content_id, pass_percentage) VALUES
(3, 70),
(5, 80);

-- ---------------------------------------------------------------------------
-- 9. assessment_question
-- ---------------------------------------------------------------------------
INSERT INTO assessment_question (assessment_id, question_text) VALUES
(1, 'What is the first step in a skincare routine?'),
(1, 'Which ingredient is known for anti-aging benefits?'),
(2, 'Who proposed the wave-particle duality?');

-- ---------------------------------------------------------------------------
-- 10. assessment_option
-- ---------------------------------------------------------------------------
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(1, 'Cleansing', 1),
(1, 'Moisturizing', 0),
(1, 'Sunscreen', 0),
(2, 'Retinol', 1),
(2, 'Alcohol', 0),
(2, 'Fragrance', 0),
(3, 'Niels Bohr', 0),
(3, 'Albert Einstein', 0),
(3, 'Louis de Broglie', 1);
