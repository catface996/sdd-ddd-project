package com.demo.infrastructure.repository.mysql.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点持久化对象
 * <p>
 * 映射数据库表 t_node，包含 MyBatis-Plus 注解用于 ORM 映射。
 * </p>
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_node")
public class NodePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     * <p>使用雪花算法自动生成</p>
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 节点名称
     * <p>唯一标识，最大 100 字符</p>
     */
    private String name;

    /**
     * 节点类型
     * <p>枚举值：DATABASE、APPLICATION、API、REPORT、OTHER</p>
     */
    private String type;

    /**
     * 节点描述
     * <p>可选，最大 500 字符</p>
     */
    private String description;

    /**
     * 节点属性
     * <p>可选，JSON 格式字符串</p>
     */
    private String properties;

    /**
     * 创建时间
     * <p>插入时自动填充</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>插入和更新时自动填充</p>
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
     * 逻辑删除标记
     * <p>0=未删除，1=已删除</p>
     */
    @TableLogic
    private Integer deleted;

    /**
     * 版本号
     * <p>乐观锁，默认 0</p>
     */
    @Version
    private Integer version;
}
