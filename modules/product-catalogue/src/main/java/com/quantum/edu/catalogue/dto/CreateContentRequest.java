package com.quantum.edu.catalogue.dto;

import com.quantum.edu.catalogue.domain.LessonType;
import com.quantum.edu.catalogue.domain.ProductContent;
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
public class CreateContentRequest {

    @NotNull
    private ProductContent.ContentType contentType;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    @Min(0)
    private Integer orderIndex;

    private Boolean mandatory;

    private LessonType lessonType;

    @Size(max = 500)
    private String videoUrl;

    @Size(max = 500)
    private String pdfUrl;

    @Min(0)
    private Integer durationSeconds;

    @Min(0)
    private Integer passPercentage;
}
