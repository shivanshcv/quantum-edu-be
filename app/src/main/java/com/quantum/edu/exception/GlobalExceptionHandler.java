package com.quantum.edu.exception;

import com.quantum.edu.common.dto.ApiError;
import com.quantum.edu.common.dto.ApiResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.quantum.edu.common.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.error("API error {}: {} (HTTP {})", ex.getErrorCode().getCode(), ex.getErrorCode().getMessage(),
                ex.getErrorCode().getHttpStatus());
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalException(InternalException ex) {
        ApiErrorCode apiErrorCode = ex.getErrorCode().getMappedApiErrorCode();
        log.error("Internal error [{}]: {} -> {} (HTTP {})", ex.getErrorCode().getCode(), ex.getMessage(),
                apiErrorCode.getMessage(), apiErrorCode.getHttpStatus());
        return buildResponse(apiErrorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + "=" + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Validation failed (QE_VAL_001): {}", fieldErrors);
        return buildResponse(ApiErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        log.error("Invalid request body: {}", cause.getMessage());
        return buildResponse(ApiErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildResponse(ApiErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ApiErrorCode errorCode) {
        ApiError error = ApiError.of(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(error));
    }
}
