package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCatalogDetails {
    private String badge;
    private String title;
    private String subtitle;
    private List<FilterResponse> filters;
    private List<CardSection> sections;
    private PaginationResponse pagination;
}
