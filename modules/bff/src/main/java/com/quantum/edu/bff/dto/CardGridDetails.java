package com.quantum.edu.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardGridDetails {
    private String title;
    private String subtitle;
    private String badge;
    private List<CardSection> sections;
    private CtaResponse cta;
}
