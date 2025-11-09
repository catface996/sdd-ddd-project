package com.catface.common.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑错误，如参数验证失败、业务规则违反等
 */
public class BusinessException extends BaseException {

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     */
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param cause     原始异常
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
