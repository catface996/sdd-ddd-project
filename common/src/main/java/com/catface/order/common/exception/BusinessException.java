package com.catface.order.common.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑错误
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String code, String message) {
        super(code, message);
    }
}
