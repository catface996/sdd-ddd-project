package com.catface.com.orderservice.common.exceptions;

/**
 * System exception for system-level errors
 */
public class SystemException extends BaseException {
    
    public SystemException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public SystemException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
