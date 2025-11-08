package com.catface.com.orderservice.common.exceptions;

/**
 * Base exception class for all custom exceptions in the system
 */
public class BaseException extends RuntimeException {
    
    private final String errorCode;
    
    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
