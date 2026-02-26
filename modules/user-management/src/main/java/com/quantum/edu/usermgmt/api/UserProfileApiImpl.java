package com.quantum.edu.usermgmt.api;

import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.usermgmt.domain.UserProfile;
import com.quantum.edu.usermgmt.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

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
    public UserProfile createProfile(Long userId, String firstName, String lastName, String phone) {
        if (userProfileRepository.existsByUserId(userId)) {
            throw new InternalException(InternalErrorCode.USER_PROFILE_ALREADY_EXISTS);
        }

        validateCreateProfile(userId, firstName);

        UserProfile profile = new UserProfile(userId, firstName, lastName, phone);
        return userProfileRepository.save(profile);
    }

    private void validateCreateProfile(Long userId, String firstName) {
        if (userId == null || firstName == null || firstName.isBlank() || firstName.length() > 100) {
            throw new InternalException(InternalErrorCode.USER_MANAGEMENT_VALIDATION_FAILED);
        }
    }
}
