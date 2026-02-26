package com.quantum.edu.ownership.api;

/**
 * Public API for ownership checks. Consumed by Cart and LMS modules.
 */
public interface OwnershipApi {

    /**
     * Check if the user owns the given course (product).
     */
    boolean ownsCourse(Long userId, Long productId);

    /**
     * Record ownership after successful payment. Called by Cart webhook.
     */
    void createOwnership(Long userId, Long courseId, Long orderId);
}
