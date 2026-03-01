-- Store passed quiz attempts so complete API can validate before marking lesson complete
-- Run: mysql -h 127.0.0.1 -u root -proot quantum_education < docs/migrations/add-user-assessment-result.sql

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
