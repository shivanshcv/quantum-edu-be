package com.quantum.edu.catalogue.dto;

import com.quantum.edu.catalogue.domain.Product;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String shortDescription;

    @NotBlank
    private String longDescription;

    @NotNull
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

    private boolean free;

    private Set<Long> categoryIds;
}
