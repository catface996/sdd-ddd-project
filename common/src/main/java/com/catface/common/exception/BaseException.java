package com.catface.common.exception;

import lombok.Getter;

/**
 * 基础异常类
 * 所有业务异常和系统异常的抽象基类
 */
@Getter
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
    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param cause     原始异常
     */
    protected BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }
}
