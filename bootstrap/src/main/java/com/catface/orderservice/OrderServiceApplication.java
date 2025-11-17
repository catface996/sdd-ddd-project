package com.catface.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Order Service Application
 * Main entry point for the Order Service application
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.catface.orderservice")
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
