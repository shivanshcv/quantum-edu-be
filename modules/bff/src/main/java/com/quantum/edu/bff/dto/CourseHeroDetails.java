package com.quantum.edu.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseHeroDetails {
    private String badge;
    private String title;
    private String shortDescription;
    private String longDescription;
    private ImageResponse image;
    private List<HighlightResponse> highlights;
    private PriceDetailsResponse priceDetails;
    private List<CtaResponse> ctas;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal price;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal discountPrice;
}
