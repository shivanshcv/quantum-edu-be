package com.quantum.edu.common.exception;

public class InternalException extends RuntimeException {

    private final InternalErrorCode errorCode;

    public InternalException(InternalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public InternalException(InternalErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public InternalErrorCode getErrorCode() {
        return errorCode;
    }
}
