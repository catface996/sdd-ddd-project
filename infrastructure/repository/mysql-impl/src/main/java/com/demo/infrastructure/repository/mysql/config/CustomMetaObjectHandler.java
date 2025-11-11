package com.demo.infrastructure.repository.mysql.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元数据自动填充处理器
 * 自动填充创建时间、更新时间、逻辑删除标记、乐观锁版本号
 */
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间（只有当字段为 null 时才填充）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间（只有当字段为 null 时才填充）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充逻辑删除标记（默认为 0，未删除）- 使用 fillStrategy 确保一定会填充
        this.strictInsertFill(metaObject, "deleted", () -> 0, Integer.class);
        // 自动填充乐观锁版本号（默认为 0）- 使用 fillStrategy 确保一定会填充
        this.strictInsertFill(metaObject, "version", () -> 0, Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
