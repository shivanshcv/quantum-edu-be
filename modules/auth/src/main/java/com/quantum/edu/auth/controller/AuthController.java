package com.quantum.edu.auth.controller;

import com.quantum.edu.auth.dto.*;
import com.quantum.edu.auth.service.AuthService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Auth signup request for email={}", request.getEmail());
        SignupResponse response = authService.signup(request);
        log.info("Auth signup success for email={}, userId={}", request.getEmail(), response.getUser().getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Auth login request for email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Auth login success for email={}, userId={}", request.getEmail(), response.getUser().getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        log.info("Auth verify-email request, tokenLength={}", request.getToken() != null ? request.getToken().length() : 0);
        VerifyEmailResponse response = authService.verifyEmail(request.getToken());
        log.info("Auth verify-email success for userId={}", response.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<ResendVerificationResponse>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        log.info("Auth resend-verification request for email={}", request.getEmail());
        ResendVerificationResponse response = authService.resendVerification(request.getEmail());
        log.info("Auth resend-verification completed for email={}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Auth change-password request for userId={}", userId);
        authService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
