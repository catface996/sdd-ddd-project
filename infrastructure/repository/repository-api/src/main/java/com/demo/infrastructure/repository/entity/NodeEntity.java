package com.demo.infrastructure.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点领域实体
 * <p>
 * 用于管理企业级应用中的系统节点（数据库、业务应用、API 接口、报表系统等）
 * 及其依赖关系和属性信息，为系统架构管理和依赖分析提供数据支持。
 * </p>
 * <p>
 * 注意：这是纯 POJO 领域实体，不包含任何持久化框架注解。
 * </p>
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     * <p>使用雪花算法自动生成</p>
     */
    private Long id;

    /**
     * 节点名称
     * <p>唯一标识，最大 100 字符</p>
     */
    private String name;

    /**
     * 节点类型
     * <p>枚举值：DATABASE（数据库）、APPLICATION（业务应用）、API（API 接口）、REPORT（报表系统）、OTHER（其他）</p>
     */
    private String type;

    /**
     * 节点描述
     * <p>可选，最大 500 字符</p>
     */
    private String description;

    /**
     * 节点属性
     * <p>可选，JSON 格式字符串，用于存储节点的扩展属性</p>
     */
    private String properties;

    /**
     * 创建时间
     * <p>自动填充</p>
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>自动填充</p>
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     * <p>通过方法参数传递</p>
     */
    private String createBy;

    /**
     * 更新人
     * <p>通过方法参数传递</p>
     */
    private String updateBy;

    /**
     * 逻辑删除标记
     * <p>0=未删除，1=已删除</p>
     */
    private Integer deleted;

    /**
     * 版本号
     * <p>乐观锁，默认 0，每次更新自动增加</p>
     */
    private Integer version;
}
