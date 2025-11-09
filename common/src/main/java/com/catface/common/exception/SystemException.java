package com.catface.common.exception;

/**
 * 系统异常
 * 用于表示系统技术错误，如数据库连接失败、外部服务调用失败等
 * HTTP 接口层捕获后返回 500 状态码
 */
public class SystemException extends BaseException {

    public SystemException(String errorCode, String message) {
        super(errorCode, message);
    }

    public SystemException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
