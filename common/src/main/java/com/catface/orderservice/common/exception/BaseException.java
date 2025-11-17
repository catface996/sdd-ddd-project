package com.catface.orderservice.common.exception;

/**
 * 基础异常类
 * 所有业务异常和系统异常的父类
 */
public abstract class BaseException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * 错误消息
     */
    private final String message;
    
    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     */
    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
    
    /**
     * 构造函数（带原因）
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param cause     原因
     */
    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }
}
