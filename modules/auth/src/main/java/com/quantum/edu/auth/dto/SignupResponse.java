package com.quantum.edu.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class SignupResponse {

    private String token;
    private Instant expiresAt;
    private UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        @JsonProperty("verified")
        private boolean verified;
    }
}
