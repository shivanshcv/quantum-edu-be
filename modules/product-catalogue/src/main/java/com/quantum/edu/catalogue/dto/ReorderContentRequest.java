package com.quantum.edu.catalogue.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderContentRequest {

    @NotEmpty
    @Valid
    private List<ContentOrder> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentOrder {
        @NotNull
        private Long contentId;
        @NotNull
        private Integer orderIndex;
    }
}
