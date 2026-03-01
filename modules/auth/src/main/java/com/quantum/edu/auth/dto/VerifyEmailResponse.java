package com.quantum.edu.auth.dto;

import com.quantum.edu.auth.domain.AuthUser.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyEmailResponse {

    private String token;
    private Instant expiresAt;
    private AuthResponse.UserInfo user;
    private String message;
    private Long userId;
}
