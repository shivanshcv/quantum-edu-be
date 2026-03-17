package com.quantum.edu.usermgmt.api;

import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.usermgmt.domain.UserProfile;
import com.quantum.edu.usermgmt.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserProfileApiImpl implements UserProfileApi {

    private final UserProfileRepository userProfileRepository;

    public UserProfileApiImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public Optional<UserProfile> getProfile(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public UserProfile updateProfile(Long userId, String firstName, String lastName, String phone) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.USER_PROFILE_NOT_FOUND));
        if (firstName != null && !firstName.isBlank()) profile.setFirstName(firstName.trim());
        if (lastName != null) profile.setLastName(lastName.isBlank() ? null : lastName.trim());
        if (phone != null) profile.setPhone(phone.isBlank() ? null : phone.trim());
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile createProfile(Long userId, String firstName, String lastName, String phone) {
        if (userProfileRepository.existsByUserId(userId)) {
            throw new InternalException(InternalErrorCode.USER_PROFILE_ALREADY_EXISTS);
        }

        validateCreateProfile(userId, firstName);

        UserProfile profile = new UserProfile(userId, firstName, lastName, phone);
        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile updateBillingInfo(Long userId, String billingName, String addressLine1,
                                        String addressLine2, String city, String state,
                                        String country, String postalCode, String gstNumber) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.USER_PROFILE_NOT_FOUND));

        if (billingName != null && !billingName.isBlank()) {
            profile.setBillingName(billingName.trim());
        }
        if (addressLine1 != null) profile.setAddressLine1(addressLine1);
        if (addressLine2 != null) profile.setAddressLine2(addressLine2);
        if (city != null) profile.setCity(city);
        if (state != null) profile.setState(state);
        if (country != null) profile.setCountry(country);
        if (postalCode != null) profile.setPostalCode(postalCode);
        if (gstNumber != null) profile.setGstNumber(gstNumber);

        return userProfileRepository.save(profile);
    }

    private void validateCreateProfile(Long userId, String firstName) {
        if (userId == null || firstName == null || firstName.isBlank() || firstName.length() > 100) {
            throw new InternalException(InternalErrorCode.USER_MANAGEMENT_VALIDATION_FAILED);
        }
    }
}
