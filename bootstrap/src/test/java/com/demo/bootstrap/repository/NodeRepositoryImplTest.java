package com.demo.bootstrap.repository;

import com.demo.common.dto.PageResult;
import com.demo.infrastructure.repository.api.NodeRepository;
import com.demo.infrastructure.repository.api.entity.NodeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NodeRepository 集成测试类
 * 测试 NodeRepository 的所有功能
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class NodeRepositoryImplTest {

    @Autowired
    private NodeRepository nodeRepository;

    /**
     * 测试保存节点
     * 验证：ID 自动生成、时间自动填充、createBy/updateBy 正确、deleted 默认 0、version 默认 0
     */
    @Test
    public void testSave() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-save");
        entity.setType("database");
        entity.setDescription("Test node for save operation");
        entity.setProperties("{\"host\":\"localhost\",\"port\":3306}");

        // 执行保存
        nodeRepository.save(entity, "test-user");

        // 验证结果
        assertNotNull(entity.getId(), "ID should be auto-generated");
        assertNotNull(entity.getCreateTime(), "Create time should be auto-filled");
        assertNotNull(entity.getUpdateTime(), "Update time should be auto-filled");
        assertEquals("test-user", entity.getCreateBy(), "CreateBy should be set correctly");
        assertEquals("test-user", entity.getUpdateBy(), "UpdateBy should be set correctly");
        
        // 查询验证默认值
        NodeEntity saved = nodeRepository.findById(entity.getId());
        assertNotNull(saved);
        assertEquals(0, saved.getDeleted(), "Deleted should default to 0");
        assertEquals(0, saved.getVersion(), "Version should default to 0");
    }

    /**
     * 测试根据ID查询节点
     * 验证：查询成功、字段值正确；查询不存在的 ID 返回 null
     */
    @Test
    public void testFindById() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-findbyid");
        entity.setType("application");
        entity.setDescription("Test node for findById operation");
        nodeRepository.save(entity, "test-user");

        // 执行查询
        NodeEntity found = nodeRepository.findById(entity.getId());

        // 验证结果
        assertNotNull(found, "Node should be found");
        assertEquals(entity.getId(), found.getId());
        assertEquals("test-node-findbyid", found.getName());
        assertEquals("application", found.getType());
        assertEquals("Test node for findById operation", found.getDescription());
        assertEquals("test-user", found.getCreateBy());
        assertEquals("test-user", found.getUpdateBy());

        // 测试查询不存在的 ID
        NodeEntity notFound = nodeRepository.findById(999999L);
        assertNull(notFound, "Should return null for non-existent ID");
    }

    /**
     * 测试更新节点
     * 验证：更新成功、updateTime 自动更新、updateBy 正确、version 自动增加
     */
    @Test
    public void testUpdate() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-update");
        entity.setType("api");
        entity.setDescription("Original description");
        nodeRepository.save(entity, "test-user");

        // 记录原始值
        Long originalId = entity.getId();
        Integer originalVersion = entity.getVersion();
        
        // 等待一小段时间确保时间戳不同
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 修改并更新
        entity.setDescription("Updated description");
        entity.setType("api-v2");
        nodeRepository.update(entity, "update-user");

        // 查询验证
        NodeEntity updated = nodeRepository.findById(originalId);
        assertNotNull(updated);
        assertEquals("Updated description", updated.getDescription());
        assertEquals("api-v2", updated.getType());
        assertEquals("update-user", updated.getUpdateBy(), "UpdateBy should be updated");
        assertEquals(originalVersion + 1, updated.getVersion(), "Version should be incremented");
        assertNotNull(updated.getUpdateTime(), "UpdateTime should be auto-filled");
    }

    /**
     * 测试删除节点
     * 验证：deleted 设置为 1；再次查询不返回已删除的节点
     */
    @Test
    public void testDeleteById() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-delete");
        entity.setType("report");
        entity.setDescription("Test node for delete operation");
        nodeRepository.save(entity, "test-user");
        Long nodeId = entity.getId();

        // 执行删除
        nodeRepository.deleteById(nodeId, "delete-user");

        // 验证：逻辑删除后查询不到
        NodeEntity deleted = nodeRepository.findById(nodeId);
        assertNull(deleted, "Deleted node should not be returned by findById");
    }

    /**
     * 测试根据名称查询节点
     * 验证：查询成功、字段值正确；查询不存在的名称返回 null
     */
    @Test
    public void testFindByName() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-findbyname");
        entity.setType("database");
        entity.setDescription("Test node for findByName operation");
        nodeRepository.save(entity, "test-user");

        // 执行查询
        NodeEntity found = nodeRepository.findByName("test-node-findbyname");

        // 验证结果
        assertNotNull(found, "Node should be found by name");
        assertEquals("test-node-findbyname", found.getName());
        assertEquals("database", found.getType());
        assertEquals("Test node for findByName operation", found.getDescription());

        // 测试查询不存在的名称
        NodeEntity notFound = nodeRepository.findByName("non-existent-name");
        assertNull(notFound, "Should return null for non-existent name");
    }

    /**
     * 测试根据类型查询节点列表
     * 验证：查询成功、返回列表、按 createTime 降序排序
     */
    @Test
    public void testFindByType() {
        // 准备测试数据 - 创建多个不同类型的节点
        NodeEntity entity1 = new NodeEntity();
        entity1.setName("test-node-type1");
        entity1.setType("service");
        entity1.setDescription("First service node");
        nodeRepository.save(entity1, "test-user");

        // 等待一小段时间确保时间戳不同
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        NodeEntity entity2 = new NodeEntity();
        entity2.setName("test-node-type2");
        entity2.setType("service");
        entity2.setDescription("Second service node");
        nodeRepository.save(entity2, "test-user");

        NodeEntity entity3 = new NodeEntity();
        entity3.setName("test-node-type3");
        entity3.setType("database");
        entity3.setDescription("Database node");
        nodeRepository.save(entity3, "test-user");

        // 执行查询
        List<NodeEntity> serviceNodes = nodeRepository.findByType("service");

        // 验证结果
        assertNotNull(serviceNodes);
        assertEquals(2, serviceNodes.size(), "Should find 2 service nodes");
        
        // 验证按 createTime 降序排序（最新的在前面）
        assertEquals("test-node-type2", serviceNodes.get(0).getName(), 
                "Nodes should be ordered by createTime DESC");
        assertEquals("test-node-type1", serviceNodes.get(1).getName());

        // 测试查询其他类型
        List<NodeEntity> databaseNodes = nodeRepository.findByType("database");
        assertEquals(1, databaseNodes.size());
        assertEquals("test-node-type3", databaseNodes.get(0).getName());
    }

    /**
     * 测试分页查询节点
     * 验证：分页参数正确（current、size、total、pages）、数据列表正确；测试名称和类型过滤
     */
    @Test
    public void testFindPage() {
        // 准备测试数据 - 创建多个节点
        for (int i = 1; i <= 5; i++) {
            NodeEntity entity = new NodeEntity();
            entity.setName("page-node-" + i);
            entity.setType(i % 2 == 0 ? "api" : "service");
            entity.setDescription("Page test node " + i);
            nodeRepository.save(entity, "test-user");
            
            // 等待一小段时间确保时间戳不同
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 测试基本分页查询
        PageResult<NodeEntity> page1 = nodeRepository.findPage(1, 3, null, null);
        assertNotNull(page1);
        assertEquals(1L, page1.getCurrent(), "Current page should be 1");
        assertEquals(3L, page1.getSize(), "Page size should be 3");
        assertTrue(page1.getTotal() >= 5, "Total should be at least 5");
        assertEquals(3, page1.getRecords().size(), "Should return 3 records");

        // 测试第二页
        PageResult<NodeEntity> page2 = nodeRepository.findPage(2, 3, null, null);
        assertNotNull(page2);
        assertEquals(2L, page2.getCurrent(), "Current page should be 2");
        assertTrue(page2.getRecords().size() >= 2, "Should return at least 2 records");

        // 测试按类型过滤
        PageResult<NodeEntity> apiPage = nodeRepository.findPage(1, 10, null, "api");
        assertNotNull(apiPage);
        assertTrue(apiPage.getTotal() >= 2, "Should find at least 2 api nodes");
        for (NodeEntity entity : apiPage.getRecords()) {
            assertEquals("api", entity.getType(), "All nodes should be of type 'api'");
        }

        // 测试按名称模糊查询
        PageResult<NodeEntity> namePage = nodeRepository.findPage(1, 10, "page-node", null);
        assertNotNull(namePage);
        assertTrue(namePage.getTotal() >= 5, "Should find at least 5 nodes with name containing 'page-node'");

        // 测试同时按名称和类型过滤
        PageResult<NodeEntity> filteredPage = nodeRepository.findPage(1, 10, "page-node", "service");
        assertNotNull(filteredPage);
        assertTrue(filteredPage.getTotal() >= 3, "Should find at least 3 service nodes with name containing 'page-node'");
        for (NodeEntity entity : filteredPage.getRecords()) {
            assertTrue(entity.getName().contains("page-node"));
            assertEquals("service", entity.getType());
        }
    }

    /**
     * 测试唯一约束
     * 验证：创建名称重复的节点抛出异常；更新节点名称为已存在的名称抛出异常
     */
    @Test
    public void testUniqueConstraint() {
        // 准备测试数据
        NodeEntity entity1 = new NodeEntity();
        entity1.setName("unique-node-name");
        entity1.setType("database");
        entity1.setDescription("First node with unique name");
        nodeRepository.save(entity1, "test-user");

        // 测试创建名称重复的节点
        NodeEntity entity2 = new NodeEntity();
        entity2.setName("unique-node-name");
        entity2.setType("api");
        entity2.setDescription("Second node with duplicate name");
        
        assertThrows(DuplicateKeyException.class, () -> {
            nodeRepository.save(entity2, "test-user");
        }, "Should throw DuplicateKeyException when creating node with duplicate name");

        // 测试更新节点名称为已存在的名称
        NodeEntity entity3 = new NodeEntity();
        entity3.setName("another-unique-name");
        entity3.setType("service");
        entity3.setDescription("Third node");
        nodeRepository.save(entity3, "test-user");

        // 尝试将 entity3 的名称更新为 entity1 的名称
        entity3.setName("unique-node-name");
        
        assertThrows(DuplicateKeyException.class, () -> {
            nodeRepository.update(entity3, "test-user");
        }, "Should throw DuplicateKeyException when updating node name to existing name");
    }

    /**
     * 测试乐观锁
     * 验证：并发更新同一节点时，后更新的操作抛出异常（乐观锁冲突）
     */
    @Test
    public void testOptimisticLock() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("optimistic-lock-node");
        entity.setType("database");
        entity.setDescription("Node for optimistic lock test");
        nodeRepository.save(entity, "test-user");
        Long nodeId = entity.getId();

        // 查询两次得到两个对象（模拟两个并发请求）
        NodeEntity entity1 = nodeRepository.findById(nodeId);
        NodeEntity entity2 = nodeRepository.findById(nodeId);

        assertNotNull(entity1);
        assertNotNull(entity2);
        assertEquals(entity1.getVersion(), entity2.getVersion(), "Both entities should have same version");

        // 第一个对象更新成功
        entity1.setDescription("Updated by first request");
        nodeRepository.update(entity1, "user1");

        // 验证第一次更新成功，版本号增加
        NodeEntity updated1 = nodeRepository.findById(nodeId);
        assertNotNull(updated1);
        assertEquals("Updated by first request", updated1.getDescription());
        assertEquals(entity1.getVersion() + 1, updated1.getVersion(), "Version should be incremented after first update");

        // 第二个对象尝试更新（版本号已过期）
        entity2.setDescription("Updated by second request");
        
        // 由于 entity2 的版本号已经过期，更新应该失败
        // MyBatis-Plus 的乐观锁会导致 updateById 返回 0（没有更新任何记录）
        // 但不会抛出异常，所以我们需要验证更新后的数据没有改变
        nodeRepository.update(entity2, "user2");
        
        // 验证数据没有被第二次更新改变
        NodeEntity finalEntity = nodeRepository.findById(nodeId);
        assertNotNull(finalEntity);
        assertEquals("Updated by first request", finalEntity.getDescription(), 
                "Description should not be changed by second update due to optimistic lock");
        assertEquals(updated1.getVersion(), finalEntity.getVersion(), 
                "Version should remain same as no update occurred");
    }
}
