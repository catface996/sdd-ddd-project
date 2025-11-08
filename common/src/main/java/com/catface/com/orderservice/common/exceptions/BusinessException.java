package com.catface.com.orderservice.common.exceptions;

/**
 * Business exception for business logic errors
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
