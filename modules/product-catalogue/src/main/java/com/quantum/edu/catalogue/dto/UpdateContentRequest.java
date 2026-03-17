package com.quantum.edu.catalogue.dto;

import com.quantum.edu.catalogue.domain.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateContentRequest {

    @Size(max = 255)
    private String title;

    private Boolean mandatory;

    private LessonType lessonType;

    @Size(max = 500)
    private String videoUrl;

    @Size(max = 500)
    private String pdfUrl;

    @Size(max = 500)
    private String pptUrl;

    @Min(0)
    private Integer durationSeconds;

    @Min(0)
    private Integer passPercentage;
}
