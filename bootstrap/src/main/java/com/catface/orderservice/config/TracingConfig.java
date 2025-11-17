package com.catface.orderservice.config;

import brave.sampler.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 链路追踪配置类
 * 配置 Brave 采样器以启用分布式追踪
 */
@Configuration
public class TracingConfig {
    
    /**
     * 配置采样器 - 100% 采样率用于测试
     * 生产环境应该降低采样率以减少性能开销
     */
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}
