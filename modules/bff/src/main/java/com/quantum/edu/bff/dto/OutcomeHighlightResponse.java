package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutcomeHighlightResponse {
    private Long id;
    private String title;
    private String description;
}
