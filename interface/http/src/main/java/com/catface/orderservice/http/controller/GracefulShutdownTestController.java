package com.catface.orderservice.http.controller;

import com.catface.orderservice.common.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Graceful Shutdown Test Controller
 * Used to test graceful shutdown behavior
 */
@RestController
@RequestMapping("/test")
public class GracefulShutdownTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownTestController.class);
    
    /**
     * Delayed response endpoint (5 seconds)
     * Used to test graceful shutdown
     */
    @GetMapping("/delayed")
    public Result<String> delayedResponse() {
        logger.info("Received delayed request, will respond after 5 seconds");
        
        try {
            // Simulate long-running request
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("Request interrupted", e);
            Thread.currentThread().interrupt();
            return Result.failure("REQUEST_INTERRUPTED", "Request was interrupted");
        }
        
        logger.info("Delayed request completed successfully");
        return Result.success("Request completed after 5 seconds delay");
    }
}
