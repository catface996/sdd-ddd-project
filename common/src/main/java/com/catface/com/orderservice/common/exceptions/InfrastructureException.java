package com.catface.com.orderservice.common.exceptions;

/**
 * Infrastructure exception for infrastructure-level errors
 */
public class InfrastructureException extends BaseException {
    
    public InfrastructureException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public InfrastructureException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
