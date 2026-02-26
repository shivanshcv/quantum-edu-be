package com.quantum.edu.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_user", indexes = @Index(unique = true, name = "uk_auth_user_email", columnList = "email"))
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_expiry")
    private Instant emailVerificationExpiry;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6) on update current_timestamp(6)")
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected AuthUser() {
    }

    public AuthUser(String email, String passwordHash, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public Instant getEmailVerificationExpiry() {
        return emailVerificationExpiry;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void clearEmailVerification() {
        this.emailVerificationToken = null;
        this.emailVerificationExpiry = null;
    }

    public void setEmailVerification(String token, Instant expiry) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiry = expiry;
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public enum Role {
        USER, ADMIN
    }
}
