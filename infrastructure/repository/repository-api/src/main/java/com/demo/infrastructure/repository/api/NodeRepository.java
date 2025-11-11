package com.demo.infrastructure.repository.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.demo.infrastructure.repository.entity.NodeEntity;

import java.util.List;

/**
 * 节点仓储接口
 * 定义节点数据访问的标准操作
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
     * 根据 ID 查询节点
     *
     * @param id 节点 ID
     * @return 节点实体，不存在则返回 null
     */
    NodeEntity findById(Long id);

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称
     * @return 节点实体，不存在则返回 null
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
     * @param type    节点类型（可选）
     * @return 分页结果
     */
    IPage<NodeEntity> findPage(Integer current, Integer size, String name, String type);

    /**
     * 根据 ID 删除节点（逻辑删除）
     *
     * @param id       节点 ID
     * @param operator 操作人
     */
    void deleteById(Long id, String operator);
}
