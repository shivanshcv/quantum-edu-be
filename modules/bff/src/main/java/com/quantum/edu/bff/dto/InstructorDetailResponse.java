package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorDetailResponse {
    private String name;
    private String role;
    private ImageResponse image;
    private String bio;
    private List<String> credentials;
}
