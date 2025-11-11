package com.demo.http.controller;

import com.demo.common.exception.BusinessException;
import com.demo.common.exception.SystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于测试全局异常处理器的功能
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试业务异常处理
     * 触发 BusinessException，验证全局异常处理器返回 400 状态码
     *
     * @throws BusinessException 业务异常
     */
    @GetMapping("/business-exception")
    public void testBusinessException() {
        throw new BusinessException("BUSINESS_ERROR", "这是一个业务异常测试");
    }

    /**
     * 测试系统异常处理
     * 触发 SystemException，验证全局异常处理器返回 500 状态码
     *
     * @throws SystemException 系统异常
     */
    @GetMapping("/system-exception")
    public void testSystemException() {
        throw new SystemException("SYSTEM_ERROR", "这是一个系统异常测试");
    }

    /**
     * 测试未知异常处理
     * 触发 RuntimeException，验证全局异常处理器返回 500 状态码和通用错误信息
     *
     * @throws RuntimeException 运行时异常
     */
    @GetMapping("/unknown-exception")
    public void testUnknownException() {
        throw new RuntimeException("这是一个未知异常测试");
    }
}
