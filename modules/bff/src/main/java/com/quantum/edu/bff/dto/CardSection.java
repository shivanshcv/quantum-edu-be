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
public class CardSection {
    private Long id;
    private String title;
    private String description;
    private String badge;
    private ImageResponse image;
    private PriceDetailsResponse priceDetails;
    private List<CtaResponse> ctas;
    /**
     * Raw catalogue list price (decimal). Lets the FE show strikethrough MRP when {@code priceDetails.originalPrice}
     * is absent (e.g. older clients). Omit when null.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal price;
    /**
     * Raw catalogue discount price when set. Omit when null.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal discountPrice;
}
