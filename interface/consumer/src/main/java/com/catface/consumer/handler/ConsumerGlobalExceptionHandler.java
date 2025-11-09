package com.catface.consumer.handler;

import com.catface.common.exception.BusinessException;
import com.catface.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 消费者全局异常处理器
 * 统一处理消息队列消费过程中的异常
 */
@Slf4j
@ControllerAdvice
public class ConsumerGlobalExceptionHandler {

    /**
     * 处理业务异常
     * 业务异常通常是由于业务规则验证失败导致的，不进行重试，记录日志后丢弃消息
     *
     * @param ex 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public void handleBusinessException(BusinessException ex) {
        log.error("消息消费业务异常，不重试: errorCode={}, message={}", 
                ex.getErrorCode(), ex.getMessage(), ex);
        // 业务异常不重试，消息将被确认并丢弃
    }

    /**
     * 处理系统异常
     * 系统异常通常是由于技术问题导致的，可以重试
     *
     * @param ex 系统异常
     */
    @ExceptionHandler(SystemException.class)
    public void handleSystemException(SystemException ex) {
        log.error("消息消费系统异常，可重试: errorCode={}, message={}", 
                ex.getErrorCode(), ex.getMessage(), ex);
        // 系统异常可重试，抛出异常让消息队列框架处理重试逻辑
        throw ex;
    }

    /**
     * 处理未知异常
     * 捕获所有未被其他处理器处理的异常，记录详细日志
     *
     * @param ex 未知异常
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception ex) {
        log.error("消息消费未知异常: ", ex);
        // 未知异常可能是系统问题，抛出异常让消息队列框架处理重试逻辑
        throw new RuntimeException("消息消费失败", ex);
    }
}
