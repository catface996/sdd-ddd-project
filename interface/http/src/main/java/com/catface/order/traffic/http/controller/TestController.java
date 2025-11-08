package com.catface.order.traffic.http.controller;

import com.catface.order.common.exception.BusinessException;
import com.catface.order.common.exception.SystemException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * 测试控制器
 * 用于验证异常处理机制
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 测试业务异常
     */
    @GetMapping("/business-exception")
    public void testBusinessException() {
        throw new BusinessException("TEST_ERROR", "测试业务异常");
    }
    
    /**
     * 测试系统异常
     */
    @GetMapping("/system-exception")
    public void testSystemException() {
        throw new SystemException("SYSTEM_ERROR", "测试系统异常");
    }
    
    /**
     * 测试参数校验异常
     */
    @PostMapping("/validation")
    public void testValidation(@Valid @RequestBody TestRequest request) {
        // 如果参数校验失败，会抛出 MethodArgumentNotValidException
    }
    
    /**
     * 测试未知异常
     */
    @GetMapping("/unknown-exception")
    public void testUnknownException() {
        throw new RuntimeException("测试未知异常");
    }
    
    /**
     * 测试 Consumer 异常处理
     * 通过反射调用 TestMessageListener 触发异常，并手动调用 Consumer 异常处理器记录日志
     */
    @GetMapping("/consumer")
    public String testConsumer(@RequestParam String type) {
        try {
            // 通过 ApplicationContext 获取 TestMessageListener bean
            Object listener = applicationContext.getBean("testMessageListener");
            // 通过反射调用 handleMessage 方法
            Method method = listener.getClass().getMethod("handleMessage", String.class);
            method.invoke(listener, type);
            return "Success";
        } catch (Exception e) {
            // 提取原始异常
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            
            // 获取 Consumer 异常处理器并手动调用
            try {
                Object consumerHandler = applicationContext.getBean("consumerExceptionHandler");
                
                if (cause instanceof BusinessException) {
                    Method handleMethod = consumerHandler.getClass().getMethod("handleBusinessException", BusinessException.class);
                    handleMethod.invoke(consumerHandler, cause);
                } else if (cause instanceof SystemException) {
                    Method handleMethod = consumerHandler.getClass().getMethod("handleSystemException", SystemException.class);
                    handleMethod.invoke(consumerHandler, cause);
                } else {
                    Method handleMethod = consumerHandler.getClass().getMethod("handleException", Exception.class);
                    handleMethod.invoke(consumerHandler, cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                }
            } catch (Exception handlerException) {
                // 忽略处理器调用异常
            }
            
            // 重新抛出异常让 HTTP 异常处理器处理
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Failed to invoke TestMessageListener", e);
        }
    }
    
    /**
     * 测试请求对象
     */
    @Data
    public static class TestRequest {
        @NotBlank(message = "name 不能为空")
        private String name;
    }
}
