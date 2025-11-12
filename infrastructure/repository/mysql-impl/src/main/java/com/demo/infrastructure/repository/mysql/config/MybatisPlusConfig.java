package com.demo.infrastructure.repository.mysql.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件、乐观锁插件、防全表更新删除插件
 */
@Configuration
@MapperScan("com.demo.infrastructure.repository.mysql.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * 按顺序注册：分页插件（第一位）、乐观锁插件、防全表更新删除插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 分页插件（必须放在第一位）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(100L); // 单页最大数量限制为 100 条
        paginationInnerInterceptor.setOverflow(false); // 溢出处理设置为 false
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 3. 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }
}
