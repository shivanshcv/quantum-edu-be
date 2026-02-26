package com.quantum.edu.auth.service;

import com.quantum.edu.auth.config.Argon2PasswordEncoder;
import com.quantum.edu.auth.domain.AuthUser;
import com.quantum.edu.auth.dto.*;
import com.quantum.edu.auth.notification.NotificationService;
import com.quantum.edu.auth.repository.AuthUserRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.usermgmt.api.UserProfileApi;
import com.quantum.edu.usermgmt.domain.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    private final AuthUserRepository authUserRepository;
    private final UserProfileApi userProfileApi;
    private final Argon2PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public AuthService(AuthUserRepository authUserRepository,
                       UserProfileApi userProfileApi,
                       Argon2PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       NotificationService notificationService) {
        this.authUserRepository = authUserRepository;
        this.userProfileApi = userProfileApi;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new InternalException(InternalErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        AuthUser authUser = new AuthUser(request.getEmail(), passwordHash, AuthUser.Role.USER);
        String verificationToken = UUID.randomUUID().toString();
        Instant verificationExpiry = Instant.now().plusSeconds(VERIFICATION_TOKEN_EXPIRY_HOURS * 3600L);
        authUser.setEmailVerification(verificationToken, verificationExpiry);
        authUser = authUserRepository.save(authUser);

        userProfileApi.createProfile(
                authUser.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );

        notificationService.sendVerification(request.getEmail(), verificationToken);

        return SignupResponse.builder()
                .userId(authUser.getId())
                .email(request.getEmail())
                .message("Registration successful. Please verify your email.")
                .requiresEmailVerification(true)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InternalException(InternalErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InternalException(InternalErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isVerified()) {
            throw new InternalException(InternalErrorCode.EMAIL_NOT_VERIFIED);
        }

        user.recordLogin();
        authUserRepository.save(user);

        String token = jwtService.generateToken(user);
        Instant expiresAt = jwtService.getExpiryInstant(token);

        UserProfile profile = userProfileApi.getProfile(user.getId())
                .orElseThrow(() -> new IllegalStateException("User profile missing for verified user"));

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName() != null ? profile.getLastName() : "")
                .role(user.getRole())
                .isVerified(user.isVerified())
                .build();

        return AuthResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .user(userInfo)
                .build();
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new InternalException(InternalErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        AuthUser user = authUserRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InternalException(InternalErrorCode.VERIFICATION_TOKEN_EXPIRED));

        if (user.getEmailVerificationExpiry() != null && user.getEmailVerificationExpiry().isBefore(Instant.now())) {
            throw new InternalException(InternalErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        user.setVerified(true);
        user.clearEmailVerification();
        authUserRepository.save(user);

        return VerifyEmailResponse.builder()
                .message("Email verified successfully")
                .userId(user.getId())
                .build();
    }

    public ResendVerificationResponse resendVerification(String email) {
        authUserRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isVerified()) {
                String token = UUID.randomUUID().toString();
                Instant expiry = Instant.now().plusSeconds(VERIFICATION_TOKEN_EXPIRY_HOURS * 3600L);
                user.setEmailVerification(token, expiry);
                authUserRepository.save(user);
                notificationService.sendVerification(user.getEmail(), token);
            }
        });

        return ResendVerificationResponse.builder()
                .message("If an account exists with this email, a verification link has been sent.")
                .build();
    }
}
