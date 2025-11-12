package com.demo.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OrderCore 应用启动类
 *
 * @author OrderCore Team
 */
@SpringBootApplication(
        scanBasePackages = "com.demo",
        exclude = {
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
        }
)
public class OrderCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderCoreApplication.class, args);
    }

}
