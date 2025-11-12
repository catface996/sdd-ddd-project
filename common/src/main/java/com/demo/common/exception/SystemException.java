package com.demo.common.exception;

/**
 * 系统异常类
 * 用于表示系统技术错误，如数据库连接失败、外部服务调用失败等
 */
public class SystemException extends BaseException {

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     */
    public SystemException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param cause     原始异常
     */
    public SystemException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
