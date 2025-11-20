package com.catface.orderservice.interfaces.http.handler;

import com.catface.orderservice.common.dto.Result;
import com.catface.orderservice.common.exception.BusinessException;
import com.catface.orderservice.common.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * HTTP 全局异常处理器
 * <p>
 * 统一处理 HTTP 接口中的异常，转换为标准的 Result 响应
 * </p>
 */
@RestControllerAdvice
public class HttpGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpGlobalExceptionHandler.class);

    /**
     * 处理业务异常
     * <p>
     * 业务异常返回 HTTP 200，Result.code 为业务错误码
     * </p>
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: errorCode={}, errorMessage={}", e.getErrorCode(), e.getErrorMessage(), e);
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }

    /**
     * 处理系统异常
     * <p>
     * 系统异常返回 HTTP 500，Result.code 为系统错误码
     * </p>
     *
     * @param e 系统异常
     * @return 错误响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystemException(SystemException e) {
        log.error("系统异常: errorCode={}, errorMessage={}", e.getErrorCode(), e.getErrorMessage(), e);
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }

    /**
     * 处理未知异常
     * <p>
     * 未知异常返回 HTTP 500，不暴露内部实现细节
     * </p>
     *
     * @param e 未知异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常", e);
        return Result.error("SYSTEM_ERROR", "系统内部错误，请稍后重试");
    }
}
