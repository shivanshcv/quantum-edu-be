package com.quantum.edu.auth.api;

import java.util.Optional;

/**
 * Public API for Auth module.
 * Called by BFF to fetch user email (read-only).
 */
public interface AuthApi {

    Optional<String> getEmailByUserId(Long userId);
}
