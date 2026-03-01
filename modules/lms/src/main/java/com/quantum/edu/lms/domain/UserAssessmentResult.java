package com.quantum.edu.lms.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_assessment_result",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_assessment_result", columnNames = {"user_id", "product_content_id"}),
        indexes = @Index(name = "idx_user_assessment_user", columnList = "user_id"))
public class UserAssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "product_content_id", nullable = false, updatable = false)
    private Long productContentId;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "score_percentage", nullable = false)
    private int scorePercentage;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected UserAssessmentResult() {
    }

    public UserAssessmentResult(Long userId, Long productContentId, boolean passed, int scorePercentage) {
        this.userId = userId;
        this.productContentId = productContentId;
        this.passed = passed;
        this.scorePercentage = scorePercentage;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductContentId() {
        return productContentId;
    }

    public boolean isPassed() {
        return passed;
    }

    public int getScorePercentage() {
        return scorePercentage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public void setScorePercentage(int scorePercentage) {
        this.scorePercentage = scorePercentage;
    }
}
