package com.catface.com.orderservice.common.exceptions;

/**
 * Validation exception for parameter validation errors
 */
public class ValidationException extends BaseException {
    
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
