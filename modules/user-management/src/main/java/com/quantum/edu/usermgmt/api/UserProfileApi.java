package com.quantum.edu.usermgmt.api;

import com.quantum.edu.usermgmt.domain.UserProfile;

import java.util.Optional;

/**
 * Public API for User Management module.
 * Called by Auth module during signup to create user profile.
 */
public interface UserProfileApi {

    UserProfile createProfile(Long userId, String firstName, String lastName, String phone);

    Optional<UserProfile> getProfile(Long userId);
}
