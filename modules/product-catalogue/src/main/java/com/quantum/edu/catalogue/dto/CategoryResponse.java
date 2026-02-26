package com.quantum.edu.catalogue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private int level;
    private boolean active;
    private List<CategoryResponse> children;
}
