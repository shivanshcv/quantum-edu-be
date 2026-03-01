package com.quantum.edu.bff.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bff.courses.catalog")
@Getter
@Setter
public class CourseCatalogProperties {
    private String badge;
    private String title;
    private String subtitle;
}
