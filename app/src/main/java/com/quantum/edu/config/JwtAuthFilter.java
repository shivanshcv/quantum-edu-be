package com.quantum.edu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantum.edu.auth.service.JwtService;
import com.quantum.edu.common.dto.ApiError;
import com.quantum.edu.common.dto.ApiResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PROTECTED_PATHS = List.of(
            "/api/v1/cart",
            "/api/v1/ownership",
            "/api/v1/bff",
            "/api/v1/usermgmt",
            "/api/v1/auth/changePassword",
            "/pages/verify-cart",
            "/pages/cart",
            "/pages/my-learning",
            "/pages/settings",
            "/lms"
    );
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/cart/webhook/"
    );
    private static final List<String> VERIFIED_ONLY_PATHS = List.of(
            "/api/v1/cart/checkout",
            "/api/v1/bff/checkout"
    );

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${app.jwt.dev-bypass:false}")
    private boolean devBypass;

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!PROTECTED_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }
        return EXCLUDED_PATHS.stream().anyMatch(path::contains);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Allow OPTIONS preflight without auth so CORS preflight succeeds
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String xUserId = request.getHeader("X-User-Id");

        // Dev bypass: allow X-User-Id header without Bearer token (for E2E tests, local dev)
        if (devBypass && xUserId != null && !xUserId.isBlank()) {
            try {
                long userId = Long.parseLong(xUserId.trim());
                request.setAttribute("userId", userId);
                request.setAttribute("isVerified", true);
                filterChain.doFilter(request, response);
                return;
            } catch (NumberFormatException ignored) {
                // Fall through to normal auth
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            var payload = devBypass
                    ? jwtService.parseTokenIgnoreExpiration(token)
                    : jwtService.parseToken(token);
            request.setAttribute("userId", payload.userId());
            request.setAttribute("isVerified", payload.isVerified());

            boolean requiresVerified = VERIFIED_ONLY_PATHS.stream().anyMatch(path -> request.getRequestURI().contains(path));
            if (requiresVerified && !payload.isVerified()) {
                response.setStatus(ApiErrorCode.EMAIL_NOT_VERIFIED.getHttpStatus());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ApiError error = ApiError.of(ApiErrorCode.EMAIL_NOT_VERIFIED);
                objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(error));
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorized(response);
        }
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(ApiErrorCode.UNAUTHORIZED.getHttpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = ApiError.of(ApiErrorCode.UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(error));
    }
}
