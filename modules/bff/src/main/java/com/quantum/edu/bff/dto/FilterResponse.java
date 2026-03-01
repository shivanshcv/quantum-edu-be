package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterResponse {
    private Long id;
    private String value;
    private String label;
}
