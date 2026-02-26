package com.quantum.edu.auth.controller;

import com.quantum.edu.auth.dto.*;
import com.quantum.edu.auth.service.AuthService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmailResponse response = authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<ResendVerificationResponse>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        ResendVerificationResponse response = authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
