package com.demo.infrastructure.repository.mysql.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.common.exception.BusinessException;
import com.demo.infrastructure.repository.api.NodeRepository;
import com.demo.infrastructure.repository.entity.NodeEntity;
import com.demo.infrastructure.repository.mysql.mapper.NodeMapper;
import com.demo.infrastructure.repository.mysql.po.NodePO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 节点仓储实现类
 * 负责 NodeEntity 和 NodePO 之间的转换，以及数据访问操作
 */
@Repository
public class NodeRepositoryImpl implements NodeRepository {

    @Autowired
    private NodeMapper nodeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void save(NodeEntity entity, String operator) {
        // 验证 properties 字段的 JSON 格式
        validateJsonFormat(entity.getProperties());
        
        NodePO po = toPO(entity);
        po.setCreateBy(operator);
        po.setUpdateBy(operator);
        // 确保 deleted 和 version 字段有默认值（如果为 null）
        if (po.getDeleted() == null) {
            po.setDeleted(0);
        }
        if (po.getVersion() == null) {
            po.setVersion(0);
        }
        nodeMapper.insert(po);
        // 将生成的 ID 和时间戳回填到 entity
        entity.setId(po.getId());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());
        entity.setDeleted(po.getDeleted());
        entity.setVersion(po.getVersion());
    }

    @Override
    public void update(NodeEntity entity, String operator) {
        // 验证 properties 字段的 JSON 格式
        validateJsonFormat(entity.getProperties());
        
        NodePO po = toPO(entity);
        po.setUpdateBy(operator);
        // 将 updateTime 设置为 null，让 MyBatis-Plus 自动填充
        po.setUpdateTime(null);
        int rows = nodeMapper.updateById(po);
        
        // 检查更新结果，如果影响行数为 0，说明发生了乐观锁冲突
        if (rows == 0) {
            throw new BusinessException("OPTIMISTIC_LOCK_CONFLICT", "数据已被其他用户修改，请刷新后重试");
        }
        
        // 将更新后的时间戳和版本号回填到 entity
        NodePO updated = nodeMapper.selectById(entity.getId());
        if (updated != null) {
            entity.setUpdateTime(updated.getUpdateTime());
            entity.setUpdateBy(updated.getUpdateBy());
            entity.setVersion(updated.getVersion());
        }
    }

    @Override
    public NodeEntity findById(Long id) {
        NodePO po = nodeMapper.selectById(id);
        return toEntity(po);
    }

    @Override
    public NodeEntity findByName(String name) {
        NodePO po = nodeMapper.selectByName(name);
        return toEntity(po);
    }

    @Override
    public List<NodeEntity> findByType(String type) {
        List<NodePO> poList = nodeMapper.selectByType(type);
        return toEntityList(poList);
    }

    @Override
    public IPage<NodeEntity> findPage(Integer current, Integer size, String name, String type) {
        Page<NodePO> page = new Page<>(current, size);
        IPage<NodePO> poPage = nodeMapper.selectPageByCondition(page, name, type);
        return toEntityPage(poPage);
    }

    @Override
    public void deleteById(Long id, String operator) {
        // 先查询 PO，设置 operator 到 updateBy
        NodePO po = nodeMapper.selectById(id);
        if (po != null) {
            po.setUpdateBy(operator);
            // MyBatis-Plus 的 deleteById 会执行逻辑删除
            nodeMapper.deleteById(id);
        }
    }

    /**
     * 将 NodePO 转换为 NodeEntity
     *
     * @param po 持久化对象
     * @return 领域实体，如果 po 为 null 则返回 null
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
     * 将 NodeEntity 转换为 NodePO
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

    /**
     * 批量转换 NodePO 列表为 NodeEntity 列表
     *
     * @param poList 持久化对象列表
     * @return 领域实体列表
     */
    private List<NodeEntity> toEntityList(List<NodePO> poList) {
        if (poList == null) {
            return null;
        }

        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 转换分页结果
     *
     * @param poPage 持久化对象分页结果
     * @return 领域实体分页结果
     */
    private IPage<NodeEntity> toEntityPage(IPage<NodePO> poPage) {
        if (poPage == null) {
            return null;
        }

        Page<NodeEntity> entityPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        entityPage.setRecords(toEntityList(poPage.getRecords()));

        return entityPage;
    }

    /**
     * 验证 JSON 格式
     * 如果 properties 字段不为空，验证其是否为有效的 JSON 格式
     *
     * @param properties JSON 字符串
     * @throws BusinessException 如果 JSON 格式无效
     */
    private void validateJsonFormat(String properties) {
        if (properties != null && !properties.trim().isEmpty()) {
            try {
                objectMapper.readTree(properties);
            } catch (Exception e) {
                throw new BusinessException("INVALID_JSON_FORMAT", "节点属性格式错误，必须是有效的 JSON");
            }
        }
    }
}
