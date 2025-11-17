package com.catface.orderservice.http.controller;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 链路追踪测试控制器
 * 用于验证分布式链路追踪功能
 */
@RestController
@RequestMapping("/api/test")
public class TracingTestController {
    
    private static final Logger log = LoggerFactory.getLogger(TracingTestController.class);
    
    @Autowired(required = false)
    private Tracer tracer;
    
    /**
     * 测试链路追踪端点
     * 验证日志中是否包含 traceId 和 spanId
     * 验证响应头中是否包含 X-B3-TraceId
     */
    @GetMapping(value = "/tracing", produces = "text/plain")
    public String testTracing() {
        String traceId = "N/A";
        String spanId = "N/A";
        
        if (tracer != null && tracer.currentSpan() != null) {
            traceId = tracer.currentSpan().context().traceId();
            spanId = tracer.currentSpan().context().spanId();
        }
        
        // Also check MDC
        String mdcTraceId = MDC.get("traceId");
        String mdcSpanId = MDC.get("spanId");
        
        log.info("Processing tracing test request - start. TraceId from Tracer: {}, SpanId from Tracer: {}", traceId, spanId);
        log.info("MDC TraceId: {}, MDC SpanId: {}", mdcTraceId, mdcSpanId);
        log.debug("This is a debug log for tracing test");
        log.info("Processing tracing test request - end");
        
        return String.format("TraceId: %s, SpanId: %s, MDC TraceId: %s, MDC SpanId: %s", 
                traceId, spanId, mdcTraceId, mdcSpanId);
    }
    
    /**
     * 测试多层调用的链路追踪
     */
    @GetMapping(value = "/tracing/nested", produces = "text/plain")
    public String testNestedTracing() {
        log.info("Processing nested tracing test - level 1");
        String result = processLevel2();
        log.info("Completed nested tracing test - level 1");
        
        return result;
    }
    
    private String processLevel2() {
        log.info("Processing nested tracing test - level 2");
        String result = processLevel3();
        log.info("Completed nested tracing test - level 2");
        return result;
    }
    
    private String processLevel3() {
        log.info("Processing nested tracing test - level 3");
        log.info("All logs should have the same traceId");
        return "Nested call completed at " + LocalDateTime.now();
    }
}
