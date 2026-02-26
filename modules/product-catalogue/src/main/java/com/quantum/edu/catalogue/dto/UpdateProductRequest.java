package com.quantum.edu.catalogue.dto;

import com.quantum.edu.catalogue.domain.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @Size(max = 255)
    private String title;

    private String shortDescription;

    private String longDescription;

    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @DecimalMin(value = "0.00")
    private BigDecimal discountPrice;

    @Size(max = 500)
    private String thumbnailUrl;

    @Size(max = 500)
    private String previewVideoUrl;

    private Product.DifficultyLevel difficultyLevel;

    @Min(0)
    private Integer durationMinutes;

    private Set<Long> categoryIds;
}
