package com.demo.infrastructure.repository.api;

import com.demo.common.dto.PageResult;
import com.demo.infrastructure.repository.api.entity.NodeEntity;

import java.util.List;

/**
 * 节点仓储接口
 * 定义节点数据访问的契约，不依赖任何持久化框架
 */
public interface NodeRepository {

    /**
     * 保存节点
     *
     * @param entity   节点实体
     * @param operator 操作人
     */
    void save(NodeEntity entity, String operator);

    /**
     * 更新节点
     *
     * @param entity   节点实体
     * @param operator 操作人
     */
    void update(NodeEntity entity, String operator);

    /**
     * 根据ID查询节点
     *
     * @param id 节点ID
     * @return 节点实体，不存在则返回null
     */
    NodeEntity findById(Long id);

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称
     * @return 节点实体，不存在则返回null
     */
    NodeEntity findByName(String name);

    /**
     * 根据类型查询节点列表
     *
     * @param type 节点类型
     * @return 节点列表
     */
    List<NodeEntity> findByType(String type);

    /**
     * 分页查询节点
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param name    节点名称（可选，支持模糊查询）
     * @param type    节点类型（可选，精确查询）
     * @return 分页结果
     */
    PageResult<NodeEntity> findPage(Integer current, Integer size, String name, String type);

    /**
     * 根据ID删除节点（逻辑删除）
     *
     * @param id       节点ID
     * @param operator 操作人
     */
    void deleteById(Long id, String operator);
}
