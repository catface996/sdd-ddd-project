package com.catface.order.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Order Service Application
 * Main entry point for the Spring Boot application
 */
@SpringBootApplication(
    scanBasePackages = "com.catface.order",
    exclude = {DataSourceAutoConfiguration.class}
)
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
