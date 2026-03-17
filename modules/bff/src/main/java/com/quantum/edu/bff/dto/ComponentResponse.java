package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentResponse {
    private String id;
    private String type;
    private Map<String, Object> config;
    private Object details;
}
