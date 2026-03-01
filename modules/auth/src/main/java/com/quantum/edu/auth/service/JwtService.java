package com.quantum.edu.auth.service;

import com.quantum.edu.auth.config.JwtProperties;
import com.quantum.edu.auth.domain.AuthUser;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getExpirySeconds());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("isVerified", user.isVerified())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Instant getExpiryInstant(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .toInstant();
    }

    public record TokenPayload(Long userId, String role, boolean isVerified) {
    }

    public TokenPayload parseToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        Boolean isVerified = claims.get("isVerified", Boolean.class);
        return new TokenPayload(userId, role, isVerified != null && isVerified);
    }

    /**
     * Parses JWT and extracts payload without validating expiration.
     * Used in dev-bypass mode to allow expired tokens while still requiring a valid JWT.
     */
    public TokenPayload parseTokenIgnoreExpiration(String token) {
        var claims = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(Instant.EPOCH))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        Boolean isVerified = claims.get("isVerified", Boolean.class);
        return new TokenPayload(userId, role, isVerified != null && isVerified);
    }
}
