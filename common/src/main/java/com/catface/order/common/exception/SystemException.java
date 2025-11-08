package com.catface.order.common.exception;

/**
 * 系统异常类
 * 用于表示系统级错误（如数据库连接失败、外部服务调用失败等）
 */
public class SystemException extends BaseException {
    
    public SystemException(String code, String message) {
        super(code, message);
    }
}
