package com.quantum.edu.catalogue.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {

    @NotBlank
    private String questionText;

    @NotEmpty
    @Valid
    private List<OptionRequest> options;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionRequest {
        @NotBlank
        private String optionText;
        @NotNull
        private Boolean correct;
    }
}
