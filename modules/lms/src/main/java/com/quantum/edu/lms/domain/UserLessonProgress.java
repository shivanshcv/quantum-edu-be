package com.quantum.edu.lms.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_lesson_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_progress_user_content", columnNames = {"user_id", "product_content_id"}),
        indexes = @Index(name = "idx_progress_user", columnList = "user_id"))
public class UserLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "product_content_id", nullable = false, updatable = false)
    private Long productContentId;

    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected UserLessonProgress() {
    }

    public UserLessonProgress(Long userId, Long productContentId, Instant completedAt) {
        this.userId = userId;
        this.productContentId = productContentId;
        this.completedAt = completedAt;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
