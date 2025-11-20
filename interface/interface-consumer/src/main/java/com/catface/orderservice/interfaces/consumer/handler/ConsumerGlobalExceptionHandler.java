package com.catface.orderservice.interfaces.consumer.handler;

import com.catface.orderservice.common.exception.BaseException;
import com.catface.orderservice.common.exception.BusinessException;
import com.catface.orderservice.common.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 消息消费者全局异常处理器
 * <p>
 * 捕获消息消费过程中的所有异常，记录日志并进行适当处理
 * </p>
 *
 * @author Order Service Team
 * @since 1.0.0
 */
@ControllerAdvice
public class ConsumerGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ConsumerGlobalExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public void handleBusinessException(BusinessException e) {
        log.warn("消息消费业务异常: errorCode={}, errorMessage={}", 
                e.getErrorCode(), e.getErrorMessage(), e);
    }

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     */
    @ExceptionHandler(SystemException.class)
    public void handleSystemException(SystemException e) {
        log.error("消息消费系统异常: errorCode={}, errorMessage={}", 
                e.getErrorCode(), e.getErrorMessage(), e);
    }

    /**
     * 处理未知异常
     *
     * @param e 未知异常
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        log.error("消息消费未知异常: {}", e.getMessage(), e);
    }
}
