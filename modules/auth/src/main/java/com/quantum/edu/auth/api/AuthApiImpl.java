package com.quantum.edu.auth.api;

import com.quantum.edu.auth.domain.AuthUser;
import com.quantum.edu.auth.repository.AuthUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthApiImpl implements AuthApi {

    private final AuthUserRepository authUserRepository;

    public AuthApiImpl(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public Optional<String> getEmailByUserId(Long userId) {
        return authUserRepository.findById(userId)
                .map(AuthUser::getEmail);
    }

    @Override
    public boolean isVerifiedByUserId(Long userId) {
        return authUserRepository.findById(userId)
                .map(AuthUser::isVerified)
                .orElse(false);
    }
}
