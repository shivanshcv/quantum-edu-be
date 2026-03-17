package com.quantum.edu.catalogue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quantum.edu.catalogue.domain.ProductAttributes;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String longDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String thumbnailUrl;
    private String previewVideoUrl;
    private String difficultyLevel;
    private Integer durationMinutes;
    private boolean published;
    private boolean free;
    private ProductAttributes attributes;
    private List<CategoryResponse> categories;
    private List<ContentSummary> contents;
    private List<ModuleSummary> modules;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentSummary {
        private Long id;
        private String contentType;
        private String title;
        private int orderIndex;
        private boolean mandatory;
        private Long moduleId;
        private Integer durationSeconds;
        private String url; // Media URL: video_url, pdf_url, or ppt_url based on type
        private String lessonType; // VIDEO, PDF, PPT (for LESSON only); null for ASSESSMENT
        private AssessmentSummary assessment; // For ASSESSMENT/QUIZ content only
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentSummary {
        private int passPercentage;
        private List<QuestionSummary> questions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionSummary {
        private Long id;
        private String questionText;
        private List<OptionSummary> options; // optionText only, no correct flag for display
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionSummary {
        private Long id;
        private String optionText;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModuleSummary {
        private Long id;
        private String title;
        private int orderIndex;
        private List<ContentSummary> contents;
    }
}
