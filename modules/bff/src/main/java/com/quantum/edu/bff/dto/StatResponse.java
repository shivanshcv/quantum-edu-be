package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatResponse {
    private String id;
    private String value;
    private String label;
    private String icon;
}
