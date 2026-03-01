package com.quantum.edu.catalogue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateModuleRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    @Min(0)
    private Integer orderIndex;
}
