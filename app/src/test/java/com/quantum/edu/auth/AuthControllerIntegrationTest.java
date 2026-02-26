package com.quantum.edu.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantum.edu.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Test
    void signup_createsUserAndReturns201() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "password", "password123",
                "firstName", "Test",
                "lastName", "User"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.userId").exists())
                .andExpect(jsonPath("$.response.email").value("test@example.com"))
                .andExpect(jsonPath("$.response.requiresEmailVerification").value(true));
    }

    @Test
    void signup_duplicateEmail_returns409() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "duplicate@example.com",
                "password", "password123",
                "firstName", "Test"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("QE_AUTH_002"));
    }

    @Test
    void signup_validationFails_returns400() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "invalid-email",
                "password", "short",
                "firstName", ""
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("QE_AUTH_001"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    void login_unverifiedUser_returns403() throws Exception {
        Map<String, Object> signupRequest = Map.of(
                "email", "unverified@example.com",
                "password", "password123",
                "firstName", "Test"
        );
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, Object> loginRequest = Map.of(
                "email", "unverified@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("QE_AUTH_004"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "nonexistent@example.com",
                "password", "wrongpassword"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("QE_AUTH_003"));
    }

    @Test
    void verifyEmailAndLogin_fullFlow() throws Exception {
        String email = "fullflow" + System.currentTimeMillis() + "@example.com";
        Map<String, Object> signupRequest = Map.of(
                "email", email,
                "password", "password123",
                "firstName", "Test"
        );
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        String token = authUserRepository.findByEmail(email)
                .orElseThrow()
                .getEmailVerificationToken();

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.message").value("Email verified successfully"));

        Map<String, Object> loginRequest = Map.of("email", email, "password", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.token").exists())
                .andExpect(jsonPath("$.response.user.email").value(email))
                .andExpect(jsonPath("$.response.user.isVerified").value(true));
    }

    @Test
    void resendVerification_returns200() throws Exception {
        Map<String, Object> request = Map.of("email", "resend@example.com");

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.message").exists());
    }
}
