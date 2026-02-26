package com.quantum.edu.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quantum.edu.common.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private String code;
    private String message;

    public static ApiError of(ApiErrorCode errorCode) {
        return ApiError.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
}
