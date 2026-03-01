package com.quantum.edu.bff.dto;

import lombok.*;

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
    private ImageResponse image;
    private List<HighlightResponse> highlights;
    private PriceDetailsResponse priceDetails;
    private List<CtaResponse> ctas;
}
