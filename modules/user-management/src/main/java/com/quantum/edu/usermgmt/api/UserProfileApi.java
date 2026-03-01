package com.quantum.edu.usermgmt.api;

import com.quantum.edu.usermgmt.domain.UserProfile;

import java.util.Optional;

/**
 * Public API for User Management module.
 * Called by Auth module during signup to create user profile.
 * Called by BFF to fetch and update billing info.
 */
public interface UserProfileApi {

    UserProfile createProfile(Long userId, String firstName, String lastName, String phone);

    Optional<UserProfile> getProfile(Long userId);

    /**
     * Updates billing info for the user. Splits billingName on first space into firstName/lastName.
     * Profile must exist (created at signup).
     */
    UserProfile updateBillingInfo(Long userId, String billingName, String addressLine1,
                                  String addressLine2, String city, String state,
                                  String country, String postalCode, String gstNumber);
}
