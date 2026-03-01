package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighlightResponse {
    private String icon;
    private String label;
    private String value;
}
