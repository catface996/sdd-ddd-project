package com.demo.infrastructure.repository.api;

import com.demo.common.dto.PageResult;
import com.demo.infrastructure.repository.entity.NodeEntity;

import java.util.List;

/**
 * 节点仓储接口
 * <p>
 * 定义节点数据访问契约，提供节点的 CRUD 操作和查询方法。
 * </p>
 *
 * @author system
 * @since 1.0.0
 */
public interface NodeRepository {

    /**
     * 保存节点
     * <p>
     * 保存新的节点实体到数据库，自动生成 ID 和填充审计字段。
     * </p>
     *
     * @param entity   节点实体，不能为 null
     * @param operator 操作人，用于填充 createBy 和 updateBy 字段，不能为 null
     * @throws IllegalArgumentException 如果 entity 或 operator 为 null
     * @throws com.demo.common.exception.BusinessException 如果节点名称已存在
     */
    void save(NodeEntity entity, String operator);

    /**
     * 更新节点
     * <p>
     * 根据节点 ID 更新节点信息，自动更新 updateTime 和 version 字段。
     * 使用乐观锁机制，如果 version 不匹配则更新失败。
     * </p>
     *
     * @param entity   节点实体，必须包含有效的 ID 和 version，不能为 null
     * @param operator 操作人，用于填充 updateBy 字段，不能为 null
     * @throws IllegalArgumentException 如果 entity 或 operator 为 null，或 entity.id 为 null
     * @throws com.demo.common.exception.BusinessException 如果节点不存在或乐观锁冲突
     */
    void update(NodeEntity entity, String operator);

    /**
     * 根据 ID 查询节点
     * <p>
     * 根据主键 ID 查询节点，只返回未删除的节点。
     * </p>
     *
     * @param id 节点 ID，不能为 null
     * @return 节点实体，如果不存在或已删除则返回 null
     * @throws IllegalArgumentException 如果 id 为 null
     */
    NodeEntity findById(Long id);

    /**
     * 根据名称查询节点
     * <p>
     * 根据节点名称精确查询节点，只返回未删除的节点。
     * </p>
     *
     * @param name 节点名称，不能为 null 或空字符串
     * @return 节点实体，如果不存在或已删除则返回 null
     * @throws IllegalArgumentException 如果 name 为 null 或空字符串
     */
    NodeEntity findByName(String name);

    /**
     * 根据类型查询节点列表
     * <p>
     * 根据节点类型查询所有匹配的节点，只返回未删除的节点。
     * 结果按创建时间降序排序。
     * </p>
     *
     * @param type 节点类型，不能为 null 或空字符串
     * @return 节点列表，如果没有匹配的节点则返回空列表（不会返回 null）
     * @throws IllegalArgumentException 如果 type 为 null 或空字符串
     */
    List<NodeEntity> findByType(String type);

    /**
     * 分页查询节点
     * <p>
     * 支持按名称模糊查询和类型精确查询，只返回未删除的节点。
     * 结果按创建时间降序排序。
     * </p>
     *
     * @param current 当前页码，从 1 开始，不能为 null 或小于 1
     * @param size    每页大小，不能为 null 或小于 1，最大不超过 100
     * @param name    节点名称（模糊查询），可选，为 null 或空字符串时不过滤
     * @param type    节点类型（精确查询），可选，为 null 或空字符串时不过滤
     * @return 分页结果，包含当前页数据和分页信息，不会返回 null
     * @throws IllegalArgumentException 如果 current 或 size 为 null 或不符合要求
     */
    PageResult<NodeEntity> findPage(Integer current, Integer size, String name, String type);

    /**
     * 逻辑删除节点
     * <p>
     * 根据节点 ID 进行逻辑删除，将 deleted 字段设置为 1。
     * 逻辑删除后的节点不会在查询中返回。
     * </p>
     *
     * @param id       节点 ID，不能为 null
     * @param operator 操作人，用于填充 updateBy 字段，不能为 null
     * @throws IllegalArgumentException 如果 id 或 operator 为 null
     * @throws com.demo.common.exception.BusinessException 如果节点不存在
     */
    void deleteById(Long id, String operator);
}
