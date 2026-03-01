package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeadlineResponse {
    private String line1;
    private String line2;
    private String highlightWord;
}
