package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCertificationDetails {
    private String title;
    private CertificationDetailResponse certificationDetails;
    private List<OutcomeHighlightResponse> outcomesHighlights;
}
