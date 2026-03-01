package com.quantum.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CORS filter that runs first (before JwtAuthFilter) so CORS headers are added
 * to all responses, including 401/403 from auth failures.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:5174,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private String allowedOriginsConfig;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = StringUtils.hasText(allowedOriginsConfig)
                ? Arrays.stream(allowedOriginsConfig.split(",\\s*"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList())
                : Collections.singletonList("http://localhost:3000");

        if (origins.contains("*")) {
            config.setAllowedOriginPatterns(Collections.singletonList("*"));
            config.setAllowCredentials(false);
        } else {
            config.setAllowedOrigins(origins);
            config.setAllowCredentials(true);
        }

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-User-Id", "Accept", "Origin"));
        config.setExposedHeaders(Collections.singletonList("Authorization"));
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);

        CorsFilter filter = new CorsFilter(source);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
