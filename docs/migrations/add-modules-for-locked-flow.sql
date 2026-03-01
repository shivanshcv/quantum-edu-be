-- Add Module 2 and Module 3 to product 1 for testing locked module flow
-- Run: mysql -u root -proot quantum_education < docs/migrations/add-modules-for-locked-flow.sql
-- Locked flow: Module 2 unlocks when all Module 1 lessons are completed; Module 3 unlocks when Module 2 is complete.

-- ---------------------------------------------------------------------------
-- 1. Add product_module 2 and 3 for product 1
-- ---------------------------------------------------------------------------
INSERT INTO product_module (product_id, title, order_index)
SELECT 1, 'Module 2: Advanced Techniques', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM product_module WHERE product_id = 1 AND order_index = 1);

INSERT INTO product_module (product_id, title, order_index)
SELECT 1, 'Module 3: Professional Treatments', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM product_module WHERE product_id = 1 AND order_index = 2);

-- ---------------------------------------------------------------------------
-- 2. Add product_content for Module 2 (2 lessons + 1 assessment)
--    order_index 3,4,5 (continuing from Module 1's 0,1,2)
-- ---------------------------------------------------------------------------
INSERT INTO product_content (product_id, module_id, content_type, title, order_index, is_mandatory)
SELECT 1, (SELECT id FROM product_module WHERE product_id = 1 AND order_index = 1 LIMIT 1), 'LESSON', 'Serums and Actives', 3, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_content WHERE product_id = 1 AND order_index = 3);

INSERT INTO product_content (product_id, module_id, content_type, title, order_index, is_mandatory)
SELECT 1, (SELECT id FROM product_module WHERE product_id = 1 AND order_index = 1 LIMIT 1), 'LESSON', 'Sun Protection Guide', 4, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_content WHERE product_id = 1 AND order_index = 4);

INSERT INTO product_content (product_id, module_id, content_type, title, order_index, is_mandatory)
SELECT 1, (SELECT id FROM product_module WHERE product_id = 1 AND order_index = 1 LIMIT 1), 'ASSESSMENT', 'Module 2 Quiz', 5, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_content WHERE product_id = 1 AND order_index = 5);

-- ---------------------------------------------------------------------------
-- 3. Add product_content for Module 3 (2 lessons)
-- ---------------------------------------------------------------------------
INSERT INTO product_content (product_id, module_id, content_type, title, order_index, is_mandatory)
SELECT 1, (SELECT id FROM product_module WHERE product_id = 1 AND order_index = 2 LIMIT 1), 'LESSON', 'Chemical Peels Overview', 6, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_content WHERE product_id = 1 AND order_index = 6);

INSERT INTO product_content (product_id, module_id, content_type, title, order_index, is_mandatory)
SELECT 1, (SELECT id FROM product_module WHERE product_id = 1 AND order_index = 2 LIMIT 1), 'LESSON', 'Retinol and Vitamin C', 7, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_content WHERE product_id = 1 AND order_index = 7);

-- ---------------------------------------------------------------------------
-- 4. Add lesson records for new LESSON content (VIDEO and PDF)
--    Get product_content ids by (product_id, order_index)
-- ---------------------------------------------------------------------------
INSERT INTO lesson (product_content_id, lesson_type, video_url, pdf_url, duration_seconds)
SELECT pc.id, 'VIDEO', 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/video.mp4', NULL, 1200
FROM product_content pc
WHERE pc.product_id = 1 AND pc.order_index = 3 AND pc.content_type = 'LESSON'
  AND NOT EXISTS (SELECT 1 FROM lesson l WHERE l.product_content_id = pc.id);

INSERT INTO lesson (product_content_id, lesson_type, video_url, pdf_url, duration_seconds)
SELECT pc.id, 'PDF', NULL, 'https://example.com/placeholders/sun-protection.pdf', 900
FROM product_content pc
WHERE pc.product_id = 1 AND pc.order_index = 4 AND pc.content_type = 'LESSON'
  AND NOT EXISTS (SELECT 1 FROM lesson l WHERE l.product_content_id = pc.id);

INSERT INTO lesson (product_content_id, lesson_type, video_url, pdf_url, duration_seconds)
SELECT pc.id, 'VIDEO', 'https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev/video.mp4', NULL, 1500
FROM product_content pc
WHERE pc.product_id = 1 AND pc.order_index = 6 AND pc.content_type = 'LESSON'
  AND NOT EXISTS (SELECT 1 FROM lesson l WHERE l.product_content_id = pc.id);

INSERT INTO lesson (product_content_id, lesson_type, video_url, pdf_url, duration_seconds)
SELECT pc.id, 'PDF', NULL, 'https://example.com/placeholders/retinol-vitaminc.pdf', 600
FROM product_content pc
WHERE pc.product_id = 1 AND pc.order_index = 7 AND pc.content_type = 'LESSON'
  AND NOT EXISTS (SELECT 1 FROM lesson l WHERE l.product_content_id = pc.id);

-- ---------------------------------------------------------------------------
-- 5. Add assessment for Module 2 Quiz
-- ---------------------------------------------------------------------------
INSERT INTO assessment (product_content_id, pass_percentage)
SELECT pc.id, 70
FROM product_content pc
WHERE pc.product_id = 1 AND pc.order_index = 5 AND pc.content_type = 'ASSESSMENT'
  AND NOT EXISTS (SELECT 1 FROM assessment a WHERE a.product_content_id = pc.id);

-- ---------------------------------------------------------------------------
-- 6. Add assessment_question for Module 2 Quiz
-- ---------------------------------------------------------------------------
INSERT INTO assessment_question (assessment_id, question_text)
SELECT a.id, 'Which vitamin is most effective for sun protection when applied topically?'
FROM assessment a
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5
  AND NOT EXISTS (SELECT 1 FROM assessment_question aq WHERE aq.assessment_id = a.id AND aq.question_text LIKE '%vitamin%');

INSERT INTO assessment_question (assessment_id, question_text)
SELECT a.id, 'What is the recommended SPF for daily use?'
FROM assessment a
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5
  AND NOT EXISTS (SELECT 1 FROM assessment_question aq WHERE aq.assessment_id = a.id AND aq.question_text LIKE '%SPF%');

-- ---------------------------------------------------------------------------
-- 7. Add assessment_option for Module 2 Quiz questions
-- ---------------------------------------------------------------------------
INSERT INTO assessment_option (question_id, option_text, is_correct)
SELECT q.id, 'Vitamin C', 0 FROM assessment_question q
JOIN assessment a ON q.assessment_id = a.id
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5 AND q.question_text LIKE '%vitamin%'
  AND NOT EXISTS (SELECT 1 FROM assessment_option ao WHERE ao.question_id = q.id AND ao.option_text = 'Vitamin C');

INSERT INTO assessment_option (question_id, option_text, is_correct)
SELECT q.id, 'Vitamin E', 0 FROM assessment_question q
JOIN assessment a ON q.assessment_id = a.id
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5 AND q.question_text LIKE '%vitamin%'
  AND NOT EXISTS (SELECT 1 FROM assessment_option ao WHERE ao.question_id = q.id AND ao.option_text = 'Vitamin E');

INSERT INTO assessment_option (question_id, option_text, is_correct)
SELECT q.id, 'Niacinamide', 1 FROM assessment_question q
JOIN assessment a ON q.assessment_id = a.id
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5 AND q.question_text LIKE '%vitamin%'
  AND NOT EXISTS (SELECT 1 FROM assessment_option ao WHERE ao.question_id = q.id AND ao.option_text = 'Niacinamide');

INSERT INTO assessment_option (question_id, option_text, is_correct)
SELECT q.id, 'SPF 15', 0 FROM assessment_question q
JOIN assessment a ON q.assessment_id = a.id
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5 AND q.question_text LIKE '%SPF%'
  AND NOT EXISTS (SELECT 1 FROM assessment_option ao WHERE ao.question_id = q.id AND ao.option_text = 'SPF 15');

INSERT INTO assessment_option (question_id, option_text, is_correct)
SELECT q.id, 'SPF 30 or higher', 1 FROM assessment_question q
JOIN assessment a ON q.assessment_id = a.id
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5 AND q.question_text LIKE '%SPF%'
  AND NOT EXISTS (SELECT 1 FROM assessment_option ao WHERE ao.question_id = q.id AND ao.option_text = 'SPF 30 or higher');

INSERT INTO assessment_option (question_id, option_text, is_correct)
SELECT q.id, 'SPF 50', 0 FROM assessment_question q
JOIN assessment a ON q.assessment_id = a.id
JOIN product_content pc ON a.product_content_id = pc.id
WHERE pc.product_id = 1 AND pc.order_index = 5 AND q.question_text LIKE '%SPF%'
  AND NOT EXISTS (SELECT 1 FROM assessment_option ao WHERE ao.question_id = q.id AND ao.option_text = 'SPF 50');
