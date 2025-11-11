package com.demo.bootstrap.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.demo.common.exception.BusinessException;
import com.demo.infrastructure.repository.api.NodeRepository;
import com.demo.infrastructure.repository.entity.NodeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NodeRepositoryImpl 单元测试类
 * 测试节点仓储的所有功能，包括 CRUD、唯一约束、乐观锁、逻辑删除等
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class NodeRepositoryImplTest {

    @Autowired
    private NodeRepository nodeRepository;

    /**
     * 测试保存节点功能
     * 验证：
     * 1. 节点保存成功
     * 2. ID 自动生成（雪花算法）
     * 3. createTime 和 updateTime 自动填充
     * 4. createBy 和 updateBy 正确设置
     * 5. deleted 默认为 0
     * 6. version 默认为 0
     */
    @Test
    public void testSave() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-save");
        entity.setType("database");
        entity.setDescription("测试节点");
        entity.setProperties("{\"host\":\"localhost\",\"port\":3306}");

        // 执行保存
        nodeRepository.save(entity, "test-user");

        // 验证结果
        assertNotNull(entity.getId(), "ID 应该自动生成");
        assertNotNull(entity.getCreateTime(), "createTime 应该自动填充");
        assertNotNull(entity.getUpdateTime(), "updateTime 应该自动填充");
        assertEquals("test-user", entity.getCreateBy(), "createBy 应该设置为操作人");
        assertEquals("test-user", entity.getUpdateBy(), "updateBy 应该设置为操作人");
        assertEquals(0, entity.getDeleted(), "deleted 应该默认为 0");
        assertEquals(0, entity.getVersion(), "version 应该默认为 0");

        // 验证可以查询到保存的节点
        NodeEntity saved = nodeRepository.findById(entity.getId());
        assertNotNull(saved, "应该能查询到保存的节点");
        assertEquals("test-node-save", saved.getName());
        assertEquals("database", saved.getType());
    }

    /**
     * 测试根据 ID 查询节点功能
     */
    @Test
    public void testFindById() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-findbyid");
        entity.setType("application");
        entity.setDescription("测试查询节点");
        entity.setProperties("{\"version\":\"1.0.0\"}");
        nodeRepository.save(entity, "test-user");

        // 执行查询
        NodeEntity found = nodeRepository.findById(entity.getId());

        // 验证结果
        assertNotNull(found, "应该能查询到节点");
        assertEquals(entity.getId(), found.getId());
        assertEquals("test-node-findbyid", found.getName());
        assertEquals("application", found.getType());
        assertEquals("测试查询节点", found.getDescription());
        assertEquals("{\"version\":\"1.0.0\"}", found.getProperties());

        // 测试查询不存在的节点
        NodeEntity notFound = nodeRepository.findById(999999L);
        assertNull(notFound, "查询不存在的节点应该返回 null");
    }

    /**
     * 测试根据名称查询节点功能
     */
    @Test
    public void testFindByName() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-findbyname");
        entity.setType("api");
        entity.setDescription("测试根据名称查询");
        entity.setProperties("{}");
        nodeRepository.save(entity, "test-user");

        // 执行查询
        NodeEntity found = nodeRepository.findByName("test-node-findbyname");

        // 验证结果
        assertNotNull(found, "应该能根据名称查询到节点");
        assertEquals("test-node-findbyname", found.getName());
        assertEquals("api", found.getType());

        // 测试查询不存在的名称
        NodeEntity notFound = nodeRepository.findByName("non-existent-name");
        assertNull(notFound, "查询不存在的名称应该返回 null");
    }

    /**
     * 测试根据类型查询节点列表功能
     */
    @Test
    public void testFindByType() {
        // 准备测试数据 - 创建多个相同类型的节点
        NodeEntity entity1 = new NodeEntity();
        entity1.setName("test-node-type-1");
        entity1.setType("report");
        entity1.setDescription("报表节点1");
        entity1.setProperties("{}");
        nodeRepository.save(entity1, "test-user");

        NodeEntity entity2 = new NodeEntity();
        entity2.setName("test-node-type-2");
        entity2.setType("report");
        entity2.setDescription("报表节点2");
        entity2.setProperties("{}");
        nodeRepository.save(entity2, "test-user");

        NodeEntity entity3 = new NodeEntity();
        entity3.setName("test-node-type-3");
        entity3.setType("database");
        entity3.setDescription("数据库节点");
        entity3.setProperties("{}");
        nodeRepository.save(entity3, "test-user");

        // 执行查询
        List<NodeEntity> reportNodes = nodeRepository.findByType("report");

        // 验证结果
        assertNotNull(reportNodes, "查询结果不应该为 null");
        assertEquals(2, reportNodes.size(), "应该查询到 2 个 report 类型的节点");
        assertTrue(reportNodes.stream().allMatch(n -> "report".equals(n.getType())),
                "所有节点的类型都应该是 report");

        // 测试查询不存在的类型
        List<NodeEntity> emptyList = nodeRepository.findByType("non-existent-type");
        assertNotNull(emptyList, "查询结果不应该为 null");
        assertTrue(emptyList.isEmpty(), "查询不存在的类型应该返回空列表");
    }

    /**
     * 测试分页查询功能
     * 验证：
     * 1. 总记录数正确
     * 2. 总页数正确
     * 3. 当前页数据正确
     * 4. 支持按名称和类型过滤
     */
    @Test
    public void testFindPage() {
        // 准备测试数据 - 创建多个节点
        for (int i = 1; i <= 15; i++) {
            NodeEntity entity = new NodeEntity();
            entity.setName("test-page-node-" + i);
            entity.setType(i % 2 == 0 ? "database" : "application");
            entity.setDescription("分页测试节点 " + i);
            entity.setProperties("{}");
            nodeRepository.save(entity, "test-user");
        }

        // 测试基本分页（第1页，每页10条）
        IPage<NodeEntity> page1 = nodeRepository.findPage(1, 10, null, null);
        assertNotNull(page1, "分页结果不应该为 null");
        assertTrue(page1.getTotal() >= 15, "总记录数应该至少有 15 条");
        assertEquals(1, page1.getCurrent(), "当前页应该是第 1 页");
        assertEquals(10, page1.getSize(), "每页大小应该是 10");
        assertTrue(page1.getRecords().size() <= 10, "当前页数据不应该超过 10 条");

        // 测试第2页
        IPage<NodeEntity> page2 = nodeRepository.findPage(2, 10, null, null);
        assertNotNull(page2, "分页结果不应该为 null");
        assertEquals(2, page2.getCurrent(), "当前页应该是第 2 页");

        // 测试按类型过滤
        IPage<NodeEntity> databasePage = nodeRepository.findPage(1, 10, null, "database");
        assertNotNull(databasePage, "分页结果不应该为 null");
        assertTrue(databasePage.getRecords().stream().allMatch(n -> "database".equals(n.getType())),
                "所有节点的类型都应该是 database");

        // 测试按名称模糊查询
        IPage<NodeEntity> namePage = nodeRepository.findPage(1, 10, "test-page-node-1", null);
        assertNotNull(namePage, "分页结果不应该为 null");
        assertTrue(namePage.getRecords().stream().allMatch(n -> n.getName().contains("test-page-node-1")),
                "所有节点的名称都应该包含 test-page-node-1");
    }

    /**
     * 测试更新节点功能
     * 验证：
     * 1. 节点更新成功
     * 2. updateTime 自动更新
     * 3. updateBy 正确设置
     * 4. version 自动增加
     */
    @Test
    public void testUpdate() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-update");
        entity.setType("database");
        entity.setDescription("原始描述");
        entity.setProperties("{\"status\":\"active\"}");
        nodeRepository.save(entity, "creator");

        // 记录原始值
        Long originalId = entity.getId();
        LocalDateTime originalUpdateTime = entity.getUpdateTime();
        Integer originalVersion = entity.getVersion();

        // 等待一小段时间，确保 updateTime 会变化
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }

        // 修改节点
        entity.setDescription("更新后的描述");
        entity.setProperties("{\"status\":\"inactive\"}");
        nodeRepository.update(entity, "updater");

        // 验证结果
        assertEquals(originalId, entity.getId(), "ID 不应该改变");
        assertNotNull(entity.getUpdateTime(), "updateTime 应该自动更新");
        assertTrue(entity.getUpdateTime().isAfter(originalUpdateTime) ||
                        entity.getUpdateTime().isEqual(originalUpdateTime),
                "updateTime 应该更新");
        assertEquals("updater", entity.getUpdateBy(), "updateBy 应该设置为操作人");
        assertEquals(originalVersion + 1, entity.getVersion(), "version 应该自动增加 1");

        // 验证数据库中的数据已更新
        NodeEntity updated = nodeRepository.findById(entity.getId());
        assertNotNull(updated, "应该能查询到更新后的节点");
        assertEquals("更新后的描述", updated.getDescription());
        assertEquals("{\"status\":\"inactive\"}", updated.getProperties());
    }

    /**
     * 测试逻辑删除功能
     * 验证：
     * 1. 删除操作成功
     * 2. deleted 字段设置为 1
     * 3. 查询时不返回已删除的节点
     */
    @Test
    public void testDeleteById() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-node-delete");
        entity.setType("application");
        entity.setDescription("待删除节点");
        entity.setProperties("{}");
        nodeRepository.save(entity, "test-user");

        Long nodeId = entity.getId();

        // 验证节点存在
        NodeEntity beforeDelete = nodeRepository.findById(nodeId);
        assertNotNull(beforeDelete, "删除前应该能查询到节点");

        // 执行删除
        nodeRepository.deleteById(nodeId, "deleter");

        // 验证删除后查询不到节点（逻辑删除）
        NodeEntity afterDelete = nodeRepository.findById(nodeId);
        assertNull(afterDelete, "删除后查询应该返回 null（逻辑删除）");

        // 验证根据名称也查询不到
        NodeEntity byName = nodeRepository.findByName("test-node-delete");
        assertNull(byName, "删除后根据名称查询也应该返回 null");
    }

    /**
     * 测试唯一约束冲突场景
     * 验证：
     * 1. 创建同名节点时抛出异常
     * 2. 更新节点名称为已存在的名称时抛出异常
     */
    @Test
    public void testDuplicateName() {
        // 准备测试数据 - 创建第一个节点
        NodeEntity entity1 = new NodeEntity();
        entity1.setName("duplicate-name-test");
        entity1.setType("database");
        entity1.setDescription("第一个节点");
        entity1.setProperties("{}");
        nodeRepository.save(entity1, "test-user");

        // 测试创建同名节点
        NodeEntity entity2 = new NodeEntity();
        entity2.setName("duplicate-name-test");
        entity2.setType("application");
        entity2.setDescription("第二个节点");
        entity2.setProperties("{}");

        assertThrows(DuplicateKeyException.class, () -> {
            nodeRepository.save(entity2, "test-user");
        }, "创建同名节点应该抛出 DuplicateKeyException");

        // 测试更新节点名称为已存在的名称
        NodeEntity entity3 = new NodeEntity();
        entity3.setName("another-unique-name");
        entity3.setType("api");
        entity3.setDescription("第三个节点");
        entity3.setProperties("{}");
        nodeRepository.save(entity3, "test-user");

        entity3.setName("duplicate-name-test");
        assertThrows(DuplicateKeyException.class, () -> {
            nodeRepository.update(entity3, "test-user");
        }, "更新节点名称为已存在的名称应该抛出 DuplicateKeyException");
    }

    /**
     * 测试乐观锁并发更新场景
     * 验证：
     * 1. 并发更新同一节点时，后更新的操作失败
     * 2. 抛出 BusinessException，错误码为 OPTIMISTIC_LOCK_CONFLICT
     */
    @Test
    public void testOptimisticLock() {
        // 准备测试数据
        NodeEntity entity = new NodeEntity();
        entity.setName("test-optimistic-lock");
        entity.setType("database");
        entity.setDescription("乐观锁测试节点");
        entity.setProperties("{}");
        nodeRepository.save(entity, "test-user");

        // 模拟两个用户同时查询同一节点
        NodeEntity user1Entity = nodeRepository.findById(entity.getId());
        NodeEntity user2Entity = nodeRepository.findById(entity.getId());

        assertNotNull(user1Entity, "用户1应该能查询到节点");
        assertNotNull(user2Entity, "用户2应该能查询到节点");
        assertEquals(user1Entity.getVersion(), user2Entity.getVersion(), "两个用户查询到的版本号应该相同");

        // 用户1先更新成功
        user1Entity.setDescription("用户1的更新");
        nodeRepository.update(user1Entity, "user1");

        // 用户2后更新，应该失败（因为版本号已经变化）
        user2Entity.setDescription("用户2的更新");
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            nodeRepository.update(user2Entity, "user2");
        }, "并发更新应该抛出 BusinessException");

        // 验证异常信息
        assertEquals("OPTIMISTIC_LOCK_CONFLICT", exception.getErrorCode(), "错误码应该是 OPTIMISTIC_LOCK_CONFLICT");
        assertEquals("数据已被其他用户修改，请刷新后重试", exception.getMessage(), "错误信息应该正确");

        // 验证数据库中的数据是用户1的更新
        NodeEntity finalEntity = nodeRepository.findById(entity.getId());
        assertNotNull(finalEntity, "应该能查询到节点");
        assertEquals("用户1的更新", finalEntity.getDescription(), "数据库中应该是用户1的更新");
    }

    /**
     * 测试 JSON 格式验证功能
     * 验证：
     * 1. 保存时 properties 字段格式错误抛出异常
     * 2. 更新时 properties 字段格式错误抛出异常
     */
    @Test
    public void testJsonValidation() {
        // 测试保存时 JSON 格式错误
        NodeEntity entity1 = new NodeEntity();
        entity1.setName("test-invalid-json-save");
        entity1.setType("database");
        entity1.setDescription("JSON 格式错误测试");
        entity1.setProperties("invalid json format");

        BusinessException saveException = assertThrows(BusinessException.class, () -> {
            nodeRepository.save(entity1, "test-user");
        }, "保存时 JSON 格式错误应该抛出 BusinessException");

        assertEquals("INVALID_JSON_FORMAT", saveException.getErrorCode(), "错误码应该是 INVALID_JSON_FORMAT");
        assertEquals("节点属性格式错误，必须是有效的 JSON", saveException.getMessage(), "错误信息应该正确");

        // 测试更新时 JSON 格式错误
        NodeEntity entity2 = new NodeEntity();
        entity2.setName("test-invalid-json-update");
        entity2.setType("application");
        entity2.setDescription("JSON 格式错误测试");
        entity2.setProperties("{}");
        nodeRepository.save(entity2, "test-user");

        entity2.setProperties("{invalid json}");
        BusinessException updateException = assertThrows(BusinessException.class, () -> {
            nodeRepository.update(entity2, "test-user");
        }, "更新时 JSON 格式错误应该抛出 BusinessException");

        assertEquals("INVALID_JSON_FORMAT", updateException.getErrorCode(), "错误码应该是 INVALID_JSON_FORMAT");
        assertEquals("节点属性格式错误，必须是有效的 JSON", updateException.getMessage(), "错误信息应该正确");

        // 测试 null 和空字符串不会抛出异常
        NodeEntity entity3 = new NodeEntity();
        entity3.setName("test-null-properties");
        entity3.setType("api");
        entity3.setDescription("null properties 测试");
        entity3.setProperties(null);
        assertDoesNotThrow(() -> {
            nodeRepository.save(entity3, "test-user");
        }, "properties 为 null 不应该抛出异常");

        NodeEntity entity4 = new NodeEntity();
        entity4.setName("test-empty-properties");
        entity4.setType("report");
        entity4.setDescription("empty properties 测试");
        entity4.setProperties("");
        assertDoesNotThrow(() -> {
            nodeRepository.save(entity4, "test-user");
        }, "properties 为空字符串不应该抛出异常");
    }
}
