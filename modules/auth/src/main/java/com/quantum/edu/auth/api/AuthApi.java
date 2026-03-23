package com.quantum.edu.auth.api;

import java.util.Optional;

/**
 * Public API for Auth module.
 * Called by BFF to fetch user email and verification status (read-only).
 */
public interface AuthApi {

    Optional<String> getEmailByUserId(Long userId);

    /**
     * Returns current verification status from DB (source of truth).
     * JWT may have stale isVerified if user verified after login.
     */
    boolean isVerifiedByUserId(Long userId);
}
