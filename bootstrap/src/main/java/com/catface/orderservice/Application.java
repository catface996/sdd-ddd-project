package com.catface.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 订单服务启动类
 * 
 * @author Order Service Team
 */
@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        
        String activeProfile = context.getEnvironment().getProperty("spring.profiles.active", "local");
        log.info("订单服务启动成功，当前激活的 profile: {}", activeProfile);
        log.debug("这是一条 DEBUG 级别的日志");
        log.info("这是一条 INFO 级别的日志");
        log.warn("这是一条 WARN 级别的日志");
        log.error("这是一条 ERROR 级别的日志");
    }

}
