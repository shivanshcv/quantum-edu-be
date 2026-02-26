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
public class ProductListResponse {

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
    private Instant createdAt;
    private Instant updatedAt;
}
