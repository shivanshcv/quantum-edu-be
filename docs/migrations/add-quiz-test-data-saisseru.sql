-- Quiz test data for saisseru: same questions/options for both quizzes, passed results
-- Run: mysql -h 127.0.0.1 -u root -proot quantum_education < docs/migrations/add-quiz-test-data-saisseru.sql

-- 1. Ensure user_assessment_result table exists
CREATE TABLE IF NOT EXISTS user_assessment_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_content_id BIGINT NOT NULL,
    passed TINYINT(1) NOT NULL,
    score_percentage INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_assessment_result (user_id, product_content_id),
    KEY idx_user_assessment_user (user_id),
    CONSTRAINT fk_user_assessment_user FOREIGN KEY (user_id) REFERENCES auth_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_assessment_content FOREIGN KEY (product_content_id) REFERENCES product_content (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Make Module 2 Quiz (assessment_id 3) have same questions as Module 1 Quiz
--    Delete existing Module 2 options and questions
DELETE ao FROM assessment_option ao
JOIN assessment_question aq ON ao.question_id = aq.id
WHERE aq.assessment_id = 3;
DELETE FROM assessment_question WHERE assessment_id = 3;

-- 3. Add same questions to Module 2 Quiz as Module 1 Quiz
--    Q1: What is the first step in a skincare routine? -> Cleansing (correct), Moisturizing, Sunscreen
--    Q2: Which ingredient is known for anti-aging benefits? -> Retinol (correct), Alcohol, Fragrance
INSERT INTO assessment_question (assessment_id, question_text)
SELECT 3, 'What is the first step in a skincare routine?' FROM DUAL
WHERE EXISTS (SELECT 1 FROM assessment WHERE id = 3);

INSERT INTO assessment_question (assessment_id, question_text)
SELECT 3, 'Which ingredient is known for anti-aging benefits?' FROM DUAL
WHERE EXISTS (SELECT 1 FROM assessment WHERE id = 3);

-- 4. Add options for Module 2 Quiz Q1 (get new question id)
SET @q1_id = (SELECT id FROM assessment_question WHERE assessment_id = 3 AND question_text LIKE '%first step%' LIMIT 1);
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q1_id, 'Cleansing', 1),
(@q1_id, 'Moisturizing', 0),
(@q1_id, 'Sunscreen', 0);

-- 5. Add options for Module 2 Quiz Q2
SET @q2_id = (SELECT id FROM assessment_question WHERE assessment_id = 3 AND question_text LIKE '%anti-aging%' LIMIT 1);
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q2_id, 'Retinol', 1),
(@q2_id, 'Alcohol', 0),
(@q2_id, 'Fragrance', 0);

-- 6. Insert passed quiz results for saisseru (Module 1 Quiz content_id=3, Module 2 Quiz content_id=8)
INSERT INTO user_assessment_result (user_id, product_content_id, passed, score_percentage)
SELECT u.id, 3, 1, 100
FROM auth_user u WHERE u.email = 'saisseru@gmail.com'
ON DUPLICATE KEY UPDATE passed = 1, score_percentage = 100;

INSERT INTO user_assessment_result (user_id, product_content_id, passed, score_percentage)
SELECT u.id, 8, 1, 100
FROM auth_user u WHERE u.email = 'saisseru@gmail.com'
ON DUPLICATE KEY UPDATE passed = 1, score_percentage = 100;
