package com.demo.infrastructure.repository.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统节点实体（纯 Java 领域对象）
 * 用于记录数据库、业务应用、API 接口、报表系统等节点及其依赖关系
 * 不包含任何框架特定的注解
 */
@Data
public class NodeEntity implements Serializable {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 节点名称（唯一约束）
     */
    private String name;

    /**
     * 节点类型（支持扩展，不限制固定值）
     */
    private String type;

    /**
     * 节点描述
     */
    private String description;

    /**
     * JSON 格式的扩展属性
     */
    private String properties;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 逻辑删除标记（0: 未删除, 1: 已删除）
     */
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
