package com.catface.common.exception;

/**
 * 业务异常
 * 用于表示业务逻辑错误，如参数验证失败、业务规则违反等
 * HTTP 接口层捕获后返回 400 状态码
 */
public class BusinessException extends BaseException {

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
