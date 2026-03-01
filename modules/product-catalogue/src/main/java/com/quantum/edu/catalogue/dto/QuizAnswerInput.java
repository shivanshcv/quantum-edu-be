package com.quantum.edu.catalogue.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerInput {

    @NotNull
    private Long questionId;

    @NotNull
    private Long optionId;
}
