package com.quantum.edu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC config. CORS is handled by CorsFilter (CorsConfig) which runs first
 * in the filter chain so headers are present on all responses including 401/403.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}
