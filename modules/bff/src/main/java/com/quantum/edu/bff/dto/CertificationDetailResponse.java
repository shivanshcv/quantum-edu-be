package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDetailResponse {
    private String icon;
    private String title;
    private String description;
    private List<String> highlights;
}
