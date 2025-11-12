package com.demo.infrastructure.repository.api.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点实体
 * 纯 POJO 类，不包含任何框架特定注解
 */
@Data
public class NodeEntity implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 节点属性JSON
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
     * 版本号（乐观锁）
     */
    private Integer version;
}
