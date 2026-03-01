package com.quantum.edu.bff.dto;

import lombok.*;

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
}
