package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorProfileDetails {
    private String title;
    private InstructorDetailResponse instructor;
}
