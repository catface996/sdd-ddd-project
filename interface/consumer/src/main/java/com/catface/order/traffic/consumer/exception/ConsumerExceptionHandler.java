package com.catface.order.traffic.consumer.exception;

import com.catface.order.common.exception.BusinessException;
import com.catface.order.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Consumer 全局异常处理器
 * 统一处理消息消费过程中抛出的异常
 */
@ControllerAdvice
@Slf4j
public class ConsumerExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public void handleBusinessException(BusinessException e) {
        log.error("Consumer business exception: code={}, message={}", 
            e.getCode(), e.getMessage(), e);
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public void handleSystemException(SystemException e) {
        log.error("Consumer system exception: code={}, message={}", 
            e.getCode(), e.getMessage(), e);
    }
    
    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        log.error("Consumer unexpected exception", e);
    }
}
