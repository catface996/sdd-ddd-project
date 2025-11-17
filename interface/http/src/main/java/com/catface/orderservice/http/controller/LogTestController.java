package com.catface.orderservice.http.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志测试控制器
 * 用于验证不同环境下的日志级别配置
 */
@RestController
@RequestMapping("/test")
public class LogTestController {

    private static final Logger log = LoggerFactory.getLogger(LogTestController.class);

    @GetMapping("/log-levels")
    public String testLogLevels() {
        log.trace("This is a TRACE log");
        log.debug("This is a DEBUG log");
        log.info("This is an INFO log");
        log.warn("This is a WARN log");
        log.error("This is an ERROR log");
        
        return "Log test completed. Check console/log file for output.";
    }
}
