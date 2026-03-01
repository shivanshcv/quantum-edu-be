package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroSectionDetails {
    private String title;
    private HeadlineResponse headline;
    private String subtitle;
    private String badge;
    private ImageResponse image;
    private List<StatResponse> stats;
    private List<FloatingCardResponse> floatingCards;
    private List<CtaResponse> ctas;
}
