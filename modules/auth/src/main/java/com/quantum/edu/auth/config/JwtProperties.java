package com.quantum.edu.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expiryHours) {

    public long getExpirySeconds() {
        return expiryHours * 3600L;
    }
}
