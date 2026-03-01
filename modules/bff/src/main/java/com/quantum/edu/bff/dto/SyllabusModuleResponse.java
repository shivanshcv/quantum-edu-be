package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusModuleResponse {
    private Long id;
    private String title;
    private String duration;
    private List<SyllabusLessonResponse> lessons;
}
