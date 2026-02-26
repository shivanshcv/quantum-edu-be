package com.quantum.edu.ownership.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "course_ownership",
        uniqueConstraints = @UniqueConstraint(name = "uk_ownership_user_course", columnNames = {"user_id", "course_id"}),
        indexes = @Index(name = "idx_ownership_user_course", columnList = "user_id, course_id"))
public class CourseOwnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false, updatable = false)
    private Long courseId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private Instant purchasedAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected CourseOwnership() {
    }

    public CourseOwnership(Long userId, Long courseId, Long orderId, Instant purchasedAt) {
        this.userId = userId;
        this.courseId = courseId;
        this.orderId = orderId;
        this.purchasedAt = purchasedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
