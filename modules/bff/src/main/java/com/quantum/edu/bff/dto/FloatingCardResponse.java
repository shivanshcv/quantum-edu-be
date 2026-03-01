package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloatingCardResponse {
    private String id;
    private String title;
    private String subtitle;
    private String position;
}
