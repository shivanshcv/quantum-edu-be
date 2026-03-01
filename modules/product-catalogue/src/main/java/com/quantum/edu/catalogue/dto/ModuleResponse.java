package com.quantum.edu.catalogue.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleResponse {

    private Long id;
    private Long productId;
    private String title;
    private int orderIndex;
    private Instant createdAt;
}
