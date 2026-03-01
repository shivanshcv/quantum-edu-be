package com.quantum.edu.bff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationResponse {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
}
