package com.demo.infrastructure.repository.mysql.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点持久化对象
 * 用于与数据库表 t_node 进行映射
 */
@Data
@TableName("t_node")
public class NodePO implements Serializable {

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
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
    @TableLogic
    private Integer deleted;

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;
}
