package com.quantum.edu.ownership.api;

import java.util.List;

/**
 * Public API for ownership checks. Consumed by Cart, LMS, and BFF modules.
 */
public interface OwnershipApi {

    /**
     * Check if the user owns the given course (product).
     */
    boolean ownsCourse(Long userId, Long productId);

    /**
     * Get course IDs the user has enrolled in, ordered by purchase date (newest first).
     */
    List<Long> getEnrolledCourseIds(Long userId);

    /**
     * Record ownership after successful payment. Called by Cart webhook.
     */
    void createOwnership(Long userId, Long courseId, Long orderId);
}
