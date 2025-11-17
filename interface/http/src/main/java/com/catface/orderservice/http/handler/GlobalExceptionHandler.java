package com.catface.orderservice.http.handler;

import com.catface.orderservice.common.dto.Result;
import com.catface.orderservice.common.exception.BusinessException;
import com.catface.orderservice.common.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中的所有异常，返回标准格式的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     * 业务异常返回 HTTP 状态码 200，错误信息在响应体中
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
        return Result.failure(e.getErrorCode(), e.getMessage());
    }
    
    /**
     * 处理系统异常
     * 系统异常返回 HTTP 状态码 500
     *
     * @param e 系统异常
     * @return 错误响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystemException(SystemException e) {
        log.error("System exception occurred: errorCode={}, message={}", e.getErrorCode(), e.getMessage(), e);
        return Result.failure(e.getErrorCode(), e.getMessage());
    }
    
    /**
     * 处理参数验证异常
     * 参数验证失败返回 HTTP 状态码 400
     *
     * @param e 参数验证异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("Validation exception occurred: {}", errorMessage);
        return Result.failure("VALIDATION_ERROR", errorMessage);
    }
    
    /**
     * 处理未知异常
     * 未知异常返回 HTTP 状态码 500，不暴露内部错误详情
     *
     * @param e 未知异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        return Result.failure("INTERNAL_ERROR", "Internal server error");
    }
}
