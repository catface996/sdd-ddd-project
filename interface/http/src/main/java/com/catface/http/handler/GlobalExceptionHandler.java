package com.catface.http.handler;

import com.catface.common.dto.Result;
import com.catface.common.exception.BusinessException;
import com.catface.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理 HTTP 请求中的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 业务异常通常是由于业务规则验证失败导致的，返回 400 状态码
     *
     * @param ex 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: errorCode={}, message={}", ex.getErrorCode(), ex.getMessage());
        return Result.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * 处理系统异常
     * 系统异常通常是由于技术问题导致的，返回 500 状态码
     *
     * @param ex 系统异常
     * @return 错误响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystemException(SystemException ex) {
        log.error("系统异常: errorCode={}, message={}", ex.getErrorCode(), ex.getMessage(), ex);
        return Result.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * 处理未知异常
     * 捕获所有未被其他处理器处理的异常，返回 500 状态码
     * 不暴露具体的异常信息，返回通用错误提示
     *
     * @param ex 未知异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("未知异常: ", ex);
        return Result.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
