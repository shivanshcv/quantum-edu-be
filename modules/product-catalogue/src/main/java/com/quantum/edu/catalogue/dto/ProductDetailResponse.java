package com.quantum.edu.catalogue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private List<CategoryResponse> categories;
    private List<ContentSummary> contents;
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
    }
}
