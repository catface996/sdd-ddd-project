package com.catface.orderservice.http.controller;

import com.catface.orderservice.common.dto.Result;
import com.catface.orderservice.common.exception.BusinessException;
import com.catface.orderservice.common.exception.SystemException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

/**
 * 异常测试控制器
 * 用于测试全局异常处理器
 */
@RestController
@RequestMapping("/api/test/exception")
public class ExceptionTestController {
    
    /**
     * 测试业务异常
     */
    @GetMapping("/business")
    public Result<Void> testBusinessException() {
        throw new BusinessException("BIZ_001", "This is a business exception");
    }
    
    /**
     * 测试系统异常
     */
    @GetMapping("/system")
    public Result<Void> testSystemException() {
        throw new SystemException("SYS_001", "This is a system exception");
    }
    
    /**
     * 测试未知异常
     */
    @GetMapping("/unknown")
    public Result<Void> testUnknownException() {
        throw new RuntimeException("This is an unknown exception");
    }
    
    /**
     * 测试参数验证异常
     */
    @PostMapping("/validation")
    public Result<Void> testValidationException(@Valid @RequestBody TestRequest request) {
        return Result.success();
    }
    
    /**
     * 测试请求对象
     */
    @Data
    public static class TestRequest {
        @NotBlank(message = "Name cannot be blank")
        private String name;
        
        @NotBlank(message = "Email cannot be blank")
        private String email;
    }
}
