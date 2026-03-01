package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainSection {
    private String type;
    private List<ComponentResponse> components;
    @Builder.Default
    private Map<String, Object> data = Map.of();
}
