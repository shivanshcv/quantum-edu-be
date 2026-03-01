package com.quantum.edu.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyLearningPageResponse {
    private String badge;
    private String title;
    private String subtitle;
    private int enrollmentCount;
    private EmptyStateResponse emptyState;
    private List<MyLearningSection> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmptyStateResponse {
        private String icon;
        private String title;
        private String message;
        private CtaResponse cta;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyLearningSection {
        private Long id;
        private String title;
        private String description;
        private String badge;
        private ImageResponse image;
        private List<CtaResponse> ctas;
    }
}
