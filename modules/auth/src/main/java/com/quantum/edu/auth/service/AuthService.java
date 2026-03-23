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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
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
            log.error("Auth signup failed: email already exists, email={}", request.getEmail());
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

        log.info("[AUTH] Signup: sending verification email to {}, userId={}", request.getEmail(), authUser.getId());
        notificationService.sendVerification(request.getEmail(), verificationToken);
        log.debug("[AUTH] Signup: sendVerification completed for {}", request.getEmail());

        String token = jwtService.generateToken(authUser);
        Instant expiresAt = jwtService.getExpiryInstant(token);

        SignupResponse.UserInfo userInfo = SignupResponse.UserInfo.builder()
                .userId(authUser.getId())
                .email(authUser.getEmail())
                .firstName(request.getFirstName() != null ? request.getFirstName() : "User")
                .lastName(request.getLastName() != null ? request.getLastName() : "")
                .role(authUser.getRole())
                .verified(false)
                .build();

        return SignupResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .user(userInfo)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Auth login failed: user not found, email={}", request.getEmail());
                    return new InternalException(InternalErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.error("Auth login failed: invalid password, email={}", request.getEmail());
            throw new InternalException(InternalErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isVerified()) {
            log.error("Auth login failed: email not verified, email={}, userId={}", request.getEmail(), user.getId());
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
                .verified(user.isVerified())
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
            log.error("Auth verify-email failed: token is null or blank");
            throw new InternalException(InternalErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        AuthUser user = authUserRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> {
                    log.error("Auth verify-email failed: token not found or invalid, tokenLength={}", token.length());
                    return new InternalException(InternalErrorCode.VERIFICATION_TOKEN_EXPIRED);
                });

        if (user.getEmailVerificationExpiry() != null && user.getEmailVerificationExpiry().isBefore(Instant.now())) {
            log.error("Auth verify-email failed: token expired, userId={}, email={}", user.getId(), user.getEmail());
            throw new InternalException(InternalErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        user.setVerified(true);
        user.clearEmailVerification();
        authUserRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        Instant expiresAt = jwtService.getExpiryInstant(jwtToken);

        UserProfile profile = userProfileApi.getProfile(user.getId()).orElse(null);
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile != null ? profile.getFirstName() : "")
                .lastName(profile != null && profile.getLastName() != null ? profile.getLastName() : "")
                .role(user.getRole())
                .verified(true)
                .build();

        return VerifyEmailResponse.builder()
                .token(jwtToken)
                .expiresAt(expiresAt)
                .user(userInfo)
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
                log.info("[AUTH] Resend-verification: sending verification email to {}, userId={}", email, user.getId());
                notificationService.sendVerification(user.getEmail(), token);
                log.info("[AUTH] Resend-verification: verification email sent, email={}, userId={}", email, user.getId());
            } else {
                log.info("Auth resend-verification: user already verified, email={}, userId={}", email, user.getId());
            }
        });
        if (!authUserRepository.existsByEmail(email)) {
            log.info("Auth resend-verification: no user found for email={}", email);
        }

        return ResendVerificationResponse.builder()
                .message("If an account exists with this email, a verification link has been sent.")
                .build();
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.error("Auth change-password failed: invalid current password, userId={}", userId);
            throw new InternalException(InternalErrorCode.INVALID_CREDENTIALS);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
        log.info("Auth change-password success for userId={}", userId);
    }
}
