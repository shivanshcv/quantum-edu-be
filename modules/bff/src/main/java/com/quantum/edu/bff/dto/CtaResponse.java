package com.quantum.edu.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CtaResponse {
    private String label;
    private String url;
    private String variant;
    private String type;
    private String action;
}
