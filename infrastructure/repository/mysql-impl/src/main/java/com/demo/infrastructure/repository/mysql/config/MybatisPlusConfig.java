package com.demo.infrastructure.repository.mysql.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置拦截器、插件和 Mapper 扫描
 */
@Slf4j
@Configuration
@MapperScan("com.demo.infrastructure.repository.mysql.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * 包含分页插件、乐观锁插件、防全表更新删除插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("初始化 MyBatis-Plus 拦截器");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件（必须放在第一位）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L); // 单页最大数量限制为 500 条
        paginationInnerInterceptor.setOverflow(false); // 溢出总页数后不进行处理
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        log.info("已添加分页插件，单页最大限制: 500 条");

        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        log.info("已添加乐观锁插件");

        // 3. 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        log.info("已添加防全表更新删除插件");

        return interceptor;
    }
}
