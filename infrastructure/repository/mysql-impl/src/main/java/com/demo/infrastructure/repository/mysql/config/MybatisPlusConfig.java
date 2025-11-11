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
 * 配置分页插件、乐观锁插件、防全表更新删除插件、元数据自动填充
 */
@Configuration
@MapperScan("com.demo.infrastructure.repository.mysql.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 分页插件（必须放在第一位）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(100L); // 单页最大数量限制为 100
        paginationInnerInterceptor.setOverflow(false); // 溢出总页数后不进行处理
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 3. 防全表更新删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 元数据自动填充处理器
     * 注意：CustomMetaObjectHandler 已经使用 @Component 注解，
     * Spring 会自动扫描并注册，这里不需要再次声明 @Bean
     */
}
