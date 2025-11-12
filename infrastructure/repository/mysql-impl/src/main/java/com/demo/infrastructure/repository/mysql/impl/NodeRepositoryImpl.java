package com.demo.infrastructure.repository.mysql.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.common.dto.PageResult;
import com.demo.common.exception.BusinessException;
import com.demo.common.exception.SystemException;
import com.demo.infrastructure.repository.api.NodeRepository;
import com.demo.infrastructure.repository.entity.NodeEntity;
import com.demo.infrastructure.repository.mysql.mapper.NodeMapper;
import com.demo.infrastructure.repository.mysql.po.NodePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 节点仓储实现类
 * <p>
 * 实现 NodeRepository 接口，提供节点数据访问的具体实现。
 * 负责领域实体（NodeEntity）和持久化对象（NodePO）之间的转换。
 * </p>
 *
 * @author system
 * @since 1.0.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NodeRepositoryImpl implements NodeRepository {

    private final NodeMapper nodeMapper;

    // 常量定义
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TYPE_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 保存节点
     *
     * @param entity   节点实体，不能为 null
     * @param operator 操作人，用于填充 createBy 和 updateBy 字段，不能为 null
     * @throws IllegalArgumentException 如果 entity 或 operator 为 null
     * @throws BusinessException        如果节点名称已存在
     */
    @Override
    public void save(NodeEntity entity, String operator) {
        if (entity == null) {
            throw new IllegalArgumentException("节点实体不能为 null");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("操作人不能为 null 或空字符串");
        }

        // 数据验证
        validateNodeEntity(entity);

        try {
            // 转换 Entity 为 PO
            NodePO po = toPO(entity);
            
            // 设置操作人信息
            po.setCreateBy(operator);
            po.setUpdateBy(operator);
            
            // 设置默认值
            if (po.getDeleted() == null) {
                po.setDeleted(0);
            }
            if (po.getVersion() == null) {
                po.setVersion(0);
            }
            
            // 插入数据
            int rows = nodeMapper.insert(po);
            
            if (rows > 0) {
                // 回填生成的 ID 和时间戳到 Entity
                entity.setId(po.getId());
                entity.setCreateTime(po.getCreateTime());
                entity.setUpdateTime(po.getUpdateTime());
                entity.setCreateBy(po.getCreateBy());
                entity.setUpdateBy(po.getUpdateBy());
                entity.setDeleted(po.getDeleted());
                entity.setVersion(po.getVersion());
                
                log.info("节点保存成功，ID: {}, 名称: {}, 操作人: {}", po.getId(), po.getName(), operator);
            }
        } catch (DuplicateKeyException e) {
            log.warn("节点名称已存在: {}", entity.getName(), e);
            throw new BusinessException("DUPLICATE_KEY", "节点名称已存在: " + entity.getName(), e);
        } catch (DataAccessException e) {
            log.error("保存节点失败，名称: {}", entity.getName(), e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 更新节点
     *
     * @param entity   节点实体，必须包含有效的 ID 和 version，不能为 null
     * @param operator 操作人，用于填充 updateBy 字段，不能为 null
     * @throws IllegalArgumentException 如果 entity 或 operator 为 null，或 entity.id 为 null
     * @throws BusinessException        如果节点不存在或乐观锁冲突
     */
    @Override
    public void update(NodeEntity entity, String operator) {
        if (entity == null) {
            throw new IllegalArgumentException("节点实体不能为 null");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("节点 ID 不能为 null");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("操作人不能为 null 或空字符串");
        }

        // 数据验证
        validateNodeEntity(entity);

        try {
            // 转换 Entity 为 PO
            NodePO po = toPO(entity);
            
            // 设置操作人信息
            po.setUpdateBy(operator);
            
            // 将 updateTime 设置为 null，让自动填充生效
            po.setUpdateTime(null);
            
            // 更新数据
            int rows = nodeMapper.updateById(po);
            
            if (rows == 0) {
                log.warn("节点更新失败，可能是乐观锁冲突或节点不存在，ID: {}, version: {}", entity.getId(), entity.getVersion());
                throw new BusinessException("OPTIMISTIC_LOCK_ERROR", "数据已被其他用户修改，请刷新后重试");
            }
            
            // 查询更新后的数据，回填时间戳和版本号
            NodePO updated = nodeMapper.selectById(po.getId());
            if (updated != null) {
                entity.setUpdateTime(updated.getUpdateTime());
                entity.setVersion(updated.getVersion());
                entity.setUpdateBy(updated.getUpdateBy());
            }
            
            log.info("节点更新成功，ID: {}, 名称: {}, 操作人: {}", po.getId(), po.getName(), operator);
        } catch (DuplicateKeyException e) {
            log.warn("节点名称已存在: {}", entity.getName(), e);
            throw new BusinessException("DUPLICATE_KEY", "节点名称已存在: " + entity.getName(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("更新节点失败，ID: {}", entity.getId(), e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 根据 ID 查询节点
     *
     * @param id 节点 ID，不能为 null
     * @return 节点实体，如果不存在或已删除则返回 null
     * @throws IllegalArgumentException 如果 id 为 null
     */
    @Override
    public NodeEntity findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("节点 ID 不能为 null");
        }

        try {
            log.debug("查询节点，ID: {}", id);
            NodePO po = nodeMapper.selectById(id);
            NodeEntity entity = toEntity(po);
            if (entity != null) {
                log.debug("查询节点成功，ID: {}, 名称: {}", id, entity.getName());
            } else {
                log.debug("节点不存在，ID: {}", id);
            }
            return entity;
        } catch (DataAccessException e) {
            log.error("查询节点失败，ID: {}", id, e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称，不能为 null 或空字符串
     * @return 节点实体，如果不存在或已删除则返回 null
     * @throws IllegalArgumentException 如果 name 为 null 或空字符串
     */
    @Override
    public NodeEntity findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("节点名称不能为 null 或空字符串");
        }

        try {
            log.debug("根据名称查询节点，名称: {}", name);
            NodePO po = nodeMapper.selectByName(name);
            NodeEntity entity = toEntity(po);
            if (entity != null) {
                log.debug("根据名称查询节点成功，名称: {}, ID: {}", name, entity.getId());
            } else {
                log.debug("节点不存在，名称: {}", name);
            }
            return entity;
        } catch (DataAccessException e) {
            log.error("根据名称查询节点失败，名称: {}", name, e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 根据类型查询节点列表
     *
     * @param type 节点类型，不能为 null 或空字符串
     * @return 节点列表，如果没有匹配的节点则返回空列表（不会返回 null）
     * @throws IllegalArgumentException 如果 type 为 null 或空字符串
     */
    @Override
    public List<NodeEntity> findByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("节点类型不能为 null 或空字符串");
        }

        try {
            log.debug("根据类型查询节点列表，类型: {}", type);
            List<NodePO> poList = nodeMapper.selectByType(type);
            List<NodeEntity> entities = poList.stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            log.debug("根据类型查询节点成功，类型: {}, 数量: {}", type, entities.size());
            return entities;
        } catch (DataAccessException e) {
            log.error("根据类型查询节点失败，类型: {}", type, e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 分页查询节点
     *
     * @param current 当前页码，从 1 开始，不能为 null 或小于 1
     * @param size    每页大小，不能为 null 或小于 1，最大不超过 100
     * @param name    节点名称（模糊查询），可选，为 null 或空字符串时不过滤
     * @param type    节点类型（精确查询），可选，为 null 或空字符串时不过滤
     * @return 分页结果，包含当前页数据和分页信息，不会返回 null
     * @throws IllegalArgumentException 如果 current 或 size 为 null 或不符合要求
     */
    @Override
    public PageResult<NodeEntity> findPage(Integer current, Integer size, String name, String type) {
        if (current == null || current < 1) {
            throw new IllegalArgumentException("当前页码不能为 null 且必须大于等于 1");
        }
        if (size == null || size < 1) {
            throw new IllegalArgumentException("每页大小不能为 null 且必须大于等于 1");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("每页大小不能超过 " + MAX_PAGE_SIZE);
        }

        try {
            log.debug("分页查询节点，current: {}, size: {}, name: {}, type: {}", current, size, name, type);
            
            // 创建分页对象
            Page<NodePO> page = new Page<>(current, size);
            
            // 执行分页查询
            IPage<NodePO> poPage = nodeMapper.selectPageByCondition(page, name, type);
            
            // 转换为 PageResult<NodeEntity>
            List<NodeEntity> entities = poPage.getRecords().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            
            log.debug("分页查询节点成功，总记录数: {}, 当前页记录数: {}", poPage.getTotal(), entities.size());
            
            return PageResult.<NodeEntity>builder()
                    .current(poPage.getCurrent())
                    .size(poPage.getSize())
                    .total(poPage.getTotal())
                    .pages(poPage.getPages())
                    .records(entities)
                    .build();
        } catch (DataAccessException e) {
            log.error("分页查询节点失败，current: {}, size: {}, name: {}, type: {}", 
                    current, size, name, type, e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 逻辑删除节点
     *
     * @param id       节点 ID，不能为 null
     * @param operator 操作人，用于填充 updateBy 字段，不能为 null
     * @throws IllegalArgumentException 如果 id 或 operator 为 null
     * @throws BusinessException        如果节点不存在
     */
    @Override
    public void deleteById(Long id, String operator) {
        if (id == null) {
            throw new IllegalArgumentException("节点 ID 不能为 null");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("操作人不能为 null 或空字符串");
        }

        try {
            // 先查询节点是否存在
            NodePO po = nodeMapper.selectById(id);
            if (po == null) {
                log.warn("节点不存在，无法删除，ID: {}", id);
                throw new BusinessException("NODE_NOT_FOUND", "节点不存在，ID: " + id);
            }
            
            // 设置操作人信息
            po.setUpdateBy(operator);
            
            // 执行逻辑删除
            int rows = nodeMapper.deleteById(id);
            
            if (rows > 0) {
                log.info("节点删除成功，ID: {}, 操作人: {}", id, operator);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("删除节点失败，ID: {}", id, e);
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        }
    }

    /**
     * 验证节点实体数据
     * <p>
     * 验证规则：
     * <ul>
     *   <li>name: 必填，长度 1-100 字符</li>
     *   <li>type: 必填，长度 1-50 字符</li>
     *   <li>description: 可选，最大 500 字符</li>
     *   <li>properties: 可选，必须是有效的 JSON 格式</li>
     * </ul>
     * </p>
     *
     * @param entity 节点实体
     * @throws IllegalArgumentException 如果数据验证失败
     */
    private void validateNodeEntity(NodeEntity entity) {
        // 验证必填字段 - name
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("节点名称不能为空");
        }
        
        // 验证 name 长度
        if (entity.getName().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("节点名称长度不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }
        
        // 验证必填字段 - type
        if (entity.getType() == null || entity.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("节点类型不能为空");
        }
        
        // 验证 type 长度
        if (entity.getType().length() > MAX_TYPE_LENGTH) {
            throw new IllegalArgumentException("节点类型长度不能超过 " + MAX_TYPE_LENGTH + " 个字符");
        }
        
        // 验证 description 长度
        if (entity.getDescription() != null && entity.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("节点描述长度不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符");
        }
        
        // 验证 properties 是否为有效的 JSON 格式（如果不为空）
        if (entity.getProperties() != null && !entity.getProperties().trim().isEmpty()) {
            try {
                // 简单验证 JSON 格式：检查是否以 { 或 [ 开头，以 } 或 ] 结尾
                String props = entity.getProperties().trim();
                if (!((props.startsWith("{") && props.endsWith("}")) || 
                      (props.startsWith("[") && props.endsWith("]")))) {
                    throw new IllegalArgumentException("节点属性必须是有效的 JSON 格式");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("节点属性必须是有效的 JSON 格式: " + e.getMessage());
            }
        }
    }

    /**
     * 将持久化对象转换为领域实体
     * <p>
     * 执行 PO 到 Entity 的字段映射，保持领域层的纯粹性。
     * </p>
     *
     * @param po 持久化对象
     * @return 领域实体，如果 po 为 null 则返回 null
     */
    private NodeEntity toEntity(NodePO po) {
        if (po == null) {
            return null;
        }
        
        return NodeEntity.builder()
                .id(po.getId())
                .name(po.getName())
                .type(po.getType())
                .description(po.getDescription())
                .properties(po.getProperties())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .createBy(po.getCreateBy())
                .updateBy(po.getUpdateBy())
                .deleted(po.getDeleted())
                .version(po.getVersion())
                .build();
    }

    /**
     * 将领域实体转换为持久化对象
     * <p>
     * 执行 Entity 到 PO 的字段映射，为数据库操作做准备。
     * </p>
     *
     * @param entity 领域实体
     * @return 持久化对象，如果 entity 为 null 则返回 null
     */
    private NodePO toPO(NodeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return NodePO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .description(entity.getDescription())
                .properties(entity.getProperties())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .createBy(entity.getCreateBy())
                .updateBy(entity.getUpdateBy())
                .deleted(entity.getDeleted())
                .version(entity.getVersion())
                .build();
    }
}
