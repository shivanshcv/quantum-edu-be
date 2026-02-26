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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalException(InternalException ex) {
        log.warn("Internal error [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());
        ApiErrorCode apiErrorCode = ex.getErrorCode().getMappedApiErrorCode();
        return buildResponse(apiErrorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        return buildResponse(ApiErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.debug("Invalid request body", ex);
        return buildResponse(ApiErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(ApiErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ApiErrorCode errorCode) {
        ApiError error = ApiError.of(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(error));
    }
}
