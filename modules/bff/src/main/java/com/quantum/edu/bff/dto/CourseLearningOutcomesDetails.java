package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLearningOutcomesDetails {
    private String title;
    private List<LearningOutcomeResponse> outcomes;
}
