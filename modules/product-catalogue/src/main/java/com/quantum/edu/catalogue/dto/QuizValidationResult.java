package com.quantum.edu.catalogue.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizValidationResult {

    private boolean passed;
    private int scorePercentage;
    private int passPercentage;
    private List<QuestionResult> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResult {
        private Long questionId;
        private boolean correct;
    }
}
