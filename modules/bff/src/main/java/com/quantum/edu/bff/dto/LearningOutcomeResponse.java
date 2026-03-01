package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningOutcomeResponse {
    private Long id;
    private String text;
    private boolean checked;
}
