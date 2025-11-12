package com.demo.infrastructure.repository.mysql.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.common.dto.PageResult;
import com.demo.infrastructure.repository.api.NodeRepository;
import com.demo.infrastructure.repository.api.entity.NodeEntity;
import com.demo.infrastructure.repository.mysql.mapper.NodeMapper;
import com.demo.infrastructure.repository.mysql.po.NodePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 节点仓储实现类
 * 负责 Entity 和 PO 之间的转换，以及数据访问操作
 */
@Repository
@RequiredArgsConstructor
public class NodeRepositoryImpl implements NodeRepository {

    private final NodeMapper nodeMapper;

    @Override
    public void save(NodeEntity entity, String operator) {
        NodePO po = toPO(entity);
        
        // 设置审计字段
        po.setCreateBy(operator);
        po.setUpdateBy(operator);
        
        // 设置默认值
        if (po.getDeleted() == null) {
            po.setDeleted(0);
        }
        if (po.getVersion() == null) {
            po.setVersion(0);
        }
        
        // 执行插入
        nodeMapper.insert(po);
        
        // 回填所有字段到 Entity（包括自动填充的时间字段和手动设置的审计字段）
        entity.setId(po.getId());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        entity.setCreateBy(operator);  // 直接使用传入的 operator
        entity.setUpdateBy(operator);  // 直接使用传入的 operator
        entity.setDeleted(0);  // 默认值
        entity.setVersion(0);  // 默认值
    }

    @Override
    public void update(NodeEntity entity, String operator) {
        NodePO po = toPO(entity);
        
        // 设置审计字段
        po.setUpdateBy(operator);
        
        // 清空 updateTime，让自动填充生效
        po.setUpdateTime(null);
        
        // 执行更新
        nodeMapper.updateById(po);
        
        // 回填更新后的字段到 Entity（不回填 version，因为乐观锁需要保持原始版本号）
        NodePO updated = nodeMapper.selectById(po.getId());
        if (updated != null) {
            entity.setUpdateTime(updated.getUpdateTime());
            entity.setUpdateBy(updated.getUpdateBy());
            // 注意：不回填 version，让调用者通过重新查询来获取最新版本
        }
    }

    @Override
    public NodeEntity findById(Long id) {
        NodePO po = nodeMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public NodeEntity findByName(String name) {
        NodePO po = nodeMapper.selectByName(name);
        return po == null ? null : toEntity(po);
    }

    @Override
    public List<NodeEntity> findByType(String type) {
        List<NodePO> poList = nodeMapper.selectByType(type);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<NodeEntity> findPage(Integer current, Integer size, String name, String type) {
        // 创建分页对象
        Page<NodePO> page = new Page<>(current, size);
        
        // 执行分页查询
        IPage<NodePO> poPage = nodeMapper.selectPageByCondition(page, name, type);
        
        // 转换为 PageResult<NodeEntity>
        List<NodeEntity> entities = poPage.getRecords().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                poPage.getCurrent(),
                poPage.getSize(),
                poPage.getTotal(),
                poPage.getPages(),
                entities
        );
    }

    @Override
    public void deleteById(Long id, String operator) {
        // 先查询 PO
        NodePO po = nodeMapper.selectById(id);
        
        if (po != null) {
            // 设置审计字段
            po.setUpdateBy(operator);
            
            // 执行逻辑删除
            nodeMapper.deleteById(id);
        }
    }

    /**
     * 将 PO 转换为 Entity
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    private NodeEntity toEntity(NodePO po) {
        if (po == null) {
            return null;
        }
        
        NodeEntity entity = new NodeEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setType(po.getType());
        entity.setDescription(po.getDescription());
        entity.setProperties(po.getProperties());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());
        entity.setDeleted(po.getDeleted());
        entity.setVersion(po.getVersion());
        
        return entity;
    }

    /**
     * 将 Entity 转换为 PO
     *
     * @param entity 领域实体
     * @return 持久化对象
     */
    private NodePO toPO(NodeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        NodePO po = new NodePO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setType(entity.getType());
        po.setDescription(entity.getDescription());
        po.setProperties(entity.getProperties());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        po.setCreateBy(entity.getCreateBy());
        po.setUpdateBy(entity.getUpdateBy());
        po.setDeleted(entity.getDeleted());
        po.setVersion(entity.getVersion());
        
        return po;
    }
}
