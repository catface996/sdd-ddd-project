package com.catface.orderservice.common.exception;

/**
 * 业务异常类
 * 用于表示业务规则验证失败等场景
 */
public class BusinessException extends BaseException {

    /**
     * 构造函数
     *
     * @param errorCode    错误码
     * @param errorMessage 错误消息
     */
    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * 构造函数
     *
     * @param errorCode    错误码
     * @param errorMessage 错误消息
     * @param cause        原始异常
     */
    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
