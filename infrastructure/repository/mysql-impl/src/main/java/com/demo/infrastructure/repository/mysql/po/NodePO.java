package com.demo.infrastructure.repository.mysql.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统节点持久化对象
 * 用于 MyBatis-Plus 数据库映射
 */
@Data
@TableName("t_node")
public class NodePO implements Serializable {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 节点名称（唯一约束）
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
     * JSON 格式的扩展属性
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
     * 创建人（通过方法参数传递）
     */
    private String createBy;

    /**
     * 更新人（通过方法参数传递）
     */
    private String updateBy;

    /**
     * 逻辑删除标记（0: 未删除, 1: 已删除）
     * 自动填充默认值 0
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 乐观锁版本号
     * 自动填充默认值 0
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
