package com.quantum.edu.bff.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemWithProductDto {
    private Long productId;
    private Instant addedAt;
    private ProductSummary product;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSummary {
        private Long id;
        private String title;
        private String slug;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal discountPrice;
        private PriceDetailsResponse priceDetails;
        private String thumbnailUrl;
        private String difficultyLevel;
        private Integer durationMinutes;
    }
}
