package com.demo.bootstrap.repository;

import com.demo.bootstrap.OrderCoreApplication;
import com.demo.common.dto.PageResult;
import com.demo.infrastructure.repository.api.NodeRepository;
import com.demo.infrastructure.repository.entity.NodeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NodeRepositoryImpl 集成测试类
 * <p>
 * 测试 NodeRepository 的所有功能，包括基础 CRUD、数据验证、异常处理和边界情况。
 * 使用 @Transactional 注解确保测试后自动回滚，不污染数据库。
 * </p>
 *
 * @author system
 * @since 1.0.0
 */
@SpringBootTest(classes = OrderCoreApplication.class)
@ActiveProfiles("local")
@Transactional
class NodeRepositoryImplTest {

    @Autowired
    private NodeRepository nodeRepository;

    /**
     * 测试数据准备方法
     * <p>
     * 创建一个标准的测试节点实体，用于各个测试方法。
     * </p>
     *
     * @param name 节点名称
     * @param type 节点类型
     * @return 测试节点实体
     */
    private NodeEntity createTestNode(String name, String type) {
        return NodeEntity.builder()
                .name(name)
                .type(type)
                .description("Test node description")
                .properties("{\"key\":\"value\"}")
                .build();
    }

    /**
     * 保存测试节点并返回
     * <p>
     * 创建并保存一个测试节点，减少重复代码。
     * </p>
     *
     * @param name     节点名称
     * @param type     节点类型
     * @param operator 操作人
     * @return 已保存的测试节点实体
     */
    private NodeEntity saveTestNode(String name, String type, String operator) {
        NodeEntity entity = createTestNode(name, type);
        nodeRepository.save(entity, operator);
        return entity;
    }

    /**
     * 断言异常消息包含指定关键字
     * <p>
     * 减少重复的异常消息验证代码。
     * </p>
     *
     * @param exception 异常对象
     * @param keywords  期望包含的关键字（至少包含一个）
     */
    private void assertExceptionMessageContains(Exception exception, String... keywords) {
        String message = exception.getMessage();
        assertNotNull(message, "异常信息不应该为 null");
        assertFalse(message.isEmpty(), "异常信息不应该为空");
        
        boolean containsAny = false;
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                containsAny = true;
                break;
            }
        }
        
        assertTrue(containsAny, 
                "异常信息应该包含以下关键字之一: " + String.join(", ", keywords) + 
                "，实际信息: " + message);
    }

    /**
     * 测试前准备
     * <p>
     * 每个测试方法执行前都会调用此方法，可以在这里进行通用的准备工作。
     * </p>
     */
    @BeforeEach
    void setUp() {
        // 可以在这里添加通用的测试准备逻辑
    }

    /**
     * 测试保存节点功能
     * <p>
     * 验证：
     * 1. ID 自动生成（不为 null）
     * 2. createTime 和 updateTime 自动填充
     * 3. createBy 和 updateBy 正确设置
     * 4. deleted 默认值为 0
     * 5. version 默认值为 0
     * </p>
     */
    @Test
    void testSave() {
        // 准备测试数据
        NodeEntity entity = createTestNode("test-save-node", "DATABASE");
        
        // 执行保存
        nodeRepository.save(entity, "test-user");
        
        // 验证结果
        assertNotNull(entity.getId(), "ID 应该自动生成");
        assertNotNull(entity.getCreateTime(), "createTime 应该自动填充");
        assertNotNull(entity.getUpdateTime(), "updateTime 应该自动填充");
        assertEquals("test-user", entity.getCreateBy(), "createBy 应该正确设置");
        assertEquals("test-user", entity.getUpdateBy(), "updateBy 应该正确设置");
        assertEquals(0, entity.getDeleted(), "deleted 默认值应该为 0");
        assertEquals(0, entity.getVersion(), "version 默认值应该为 0");
    }

    /**
     * 测试根据 ID 查询节点功能
     * <p>
     * 验证：
     * 1. 查询存在的节点成功
     * 2. 字段值正确
     * 3. 查询不存在的 ID 返回 null
     * </p>
     */
    @Test
    void testFindById() {
        // 准备测试数据
        NodeEntity entity = saveTestNode("test-findbyid-node", "APPLICATION", "test-user");
        Long id = entity.getId();
        
        // 执行查询
        NodeEntity found = nodeRepository.findById(id);
        
        // 验证结果
        assertNotNull(found, "应该查询到节点");
        assertEquals(id, found.getId());
        assertEquals("test-findbyid-node", found.getName());
        assertEquals("APPLICATION", found.getType());
        assertEquals("Test node description", found.getDescription());
        
        // 查询不存在的 ID
        NodeEntity notFound = nodeRepository.findById(999999L);
        assertNull(notFound, "查询不存在的 ID 应该返回 null");
    }

    /**
     * 测试根据名称查询节点功能
     * <p>
     * 验证：
     * 1. 查询存在的节点成功
     * 2. 字段值正确
     * 3. 查询不存在的名称返回 null
     * </p>
     */
    @Test
    void testFindByName() {
        // 准备测试数据
        NodeEntity entity = saveTestNode("test-findbyname-node", "API", "test-user");
        
        // 执行查询
        NodeEntity found = nodeRepository.findByName("test-findbyname-node");
        
        // 验证结果
        assertNotNull(found, "应该查询到节点");
        assertEquals("test-findbyname-node", found.getName());
        assertEquals("API", found.getType());
        
        // 查询不存在的名称
        NodeEntity notFound = nodeRepository.findByName("non-existent-node");
        assertNull(notFound, "查询不存在的名称应该返回 null");
    }

    /**
     * 测试根据类型查询节点列表功能
     * <p>
     * 验证：
     * 1. 查询成功
     * 2. 返回列表
     * 3. 按创建时间降序排序
     * </p>
     */
    @Test
    void testFindByType() {
        // 准备测试数据
        saveTestNode("test-findbytype-node-1", "REPORT", "test-user");
        // 稍微延迟以确保创建时间不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        saveTestNode("test-findbytype-node-2", "REPORT", "test-user");
        saveTestNode("test-findbytype-node-3", "DATABASE", "test-user");
        
        // 执行查询
        List<NodeEntity> found = nodeRepository.findByType("REPORT");
        
        // 验证结果
        assertNotNull(found, "应该返回列表");
        assertEquals(2, found.size(), "应该查询到 2 个节点");
        // 验证按创建时间降序排序（最新的在前面）
        assertTrue(found.get(0).getCreateTime().isAfter(found.get(1).getCreateTime()) ||
                   found.get(0).getCreateTime().isEqual(found.get(1).getCreateTime()),
                   "应该按创建时间降序排序");
    }

    /**
     * 测试分页查询节点功能
     * <p>
     * 验证：
     * 1. 分页参数正确
     * 2. 总记录数正确
     * 3. 数据列表正确
     * 4. 支持名称和类型过滤
     * </p>
     */
    @Test
    void testFindPage() {
        // 准备测试数据
        for (int i = 1; i <= 5; i++) {
            saveTestNode("test-page-node-" + i, "OTHER", "test-user");
        }
        
        // 测试基本分页
        PageResult<NodeEntity> page1 = nodeRepository.findPage(1, 2, null, null);
        assertNotNull(page1, "应该返回分页结果");
        assertEquals(1L, page1.getCurrent(), "当前页应该为 1");
        assertEquals(2L, page1.getSize(), "每页大小应该为 2");
        assertTrue(page1.getTotal() >= 5, "总记录数应该至少为 5");
        assertEquals(2, page1.getRecords().size(), "当前页应该有 2 条记录");
        
        // 测试名称过滤
        PageResult<NodeEntity> page2 = nodeRepository.findPage(1, 10, "test-page-node-1", null);
        assertNotNull(page2, "应该返回分页结果");
        assertTrue(page2.getTotal() >= 1, "应该至少查询到 1 条记录");
        
        // 测试类型过滤
        PageResult<NodeEntity> page3 = nodeRepository.findPage(1, 10, null, "OTHER");
        assertNotNull(page3, "应该返回分页结果");
        assertTrue(page3.getTotal() >= 5, "应该至少查询到 5 条记录");
    }

    /**
     * 测试更新节点功能
     * <p>
     * 验证：
     * 1. 更新成功
     * 2. updateTime 自动更新
     * 3. updateBy 正确设置
     * 4. version 自动增加
     * </p>
     */
    @Test
    void testUpdate() {
        // 准备测试数据
        NodeEntity entity = saveTestNode("test-update-node", "DATABASE", "test-user");
        
        Long originalId = entity.getId();
        LocalDateTime originalUpdateTime = entity.getUpdateTime();
        Integer originalVersion = entity.getVersion();
        
        // 稍微延迟以确保更新时间不同
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 修改节点信息
        entity.setDescription("Updated description");
        
        // 执行更新
        nodeRepository.update(entity, "test-user2");
        
        // 验证结果
        assertEquals(originalId, entity.getId(), "ID 不应该改变");
        assertTrue(entity.getUpdateTime().isAfter(originalUpdateTime), 
                   "updateTime 应该自动更新");
        assertEquals("test-user2", entity.getUpdateBy(), "updateBy 应该正确设置");
        assertEquals(originalVersion + 1, entity.getVersion(), "version 应该自动增加");
        
        // 从数据库重新查询验证
        NodeEntity updated = nodeRepository.findById(originalId);
        assertNotNull(updated);
        assertEquals("Updated description", updated.getDescription());
    }

    /**
     * 测试逻辑删除节点功能
     * <p>
     * 验证：
     * 1. deleted 设置为 1
     * 2. 查询时不返回已删除的节点
     * </p>
     */
    @Test
    void testDeleteById() {
        // 准备测试数据
        NodeEntity entity = saveTestNode("test-delete-node", "APPLICATION", "test-user");
        Long id = entity.getId();
        
        // 验证节点存在
        NodeEntity found = nodeRepository.findById(id);
        assertNotNull(found, "删除前应该能查询到节点");
        
        // 执行删除
        nodeRepository.deleteById(id, "test-user");
        
        // 验证节点已被逻辑删除
        NodeEntity deleted = nodeRepository.findById(id);
        assertNull(deleted, "删除后查询应该返回 null");
    }

    /**
     * 测试保存时的字段验证
     * <p>
     * 验证：
     * 1. 所有必填字段正确保存
     * 2. 可选字段(description、properties)可以为 null
     * 3. 保存后可以正确查询
     * </p>
     */
    @Test
    void testSaveWithOptionalFields() {
        // 准备测试数据 - 只设置必填字段
        NodeEntity entity = NodeEntity.builder()
                .name("test-minimal-node")
                .type("API")
                .build();
        
        // 执行保存
        nodeRepository.save(entity, "test-user");
        
        // 验证结果
        assertNotNull(entity.getId(), "ID 应该自动生成");
        assertNotNull(entity.getCreateTime(), "createTime 应该自动填充");
        assertNotNull(entity.getUpdateTime(), "updateTime 应该自动填充");
        assertEquals("test-user", entity.getCreateBy(), "createBy 应该正确设置");
        assertEquals("test-user", entity.getUpdateBy(), "updateBy 应该正确设置");
        
        // 从数据库重新查询验证
        NodeEntity found = nodeRepository.findById(entity.getId());
        assertNotNull(found, "应该能查询到保存的节点");
        assertEquals("test-minimal-node", found.getName());
        assertEquals("API", found.getType());
        assertNull(found.getDescription(), "description 应该为 null");
        assertNull(found.getProperties(), "properties 应该为 null");
    }

    // ==================== 数据验证测试 ====================

    /**
     * 测试必填字段验证 - 节点名称为空
     * <p>
     * 验证：
     * 1. 节点名称为 null 时抛出 IllegalArgumentException
     * 2. 节点名称为空字符串时抛出 IllegalArgumentException
     * 3. 异常信息清晰明确
     * </p>
     */
    @Test
    void testValidateNameRequired() {
        // 测试 name 为 null
        NodeEntity entity1 = NodeEntity.builder()
                .name(null)
                .type("DATABASE")
                .build();
        
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity1, "test-user"),
                "节点名称为 null 时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception1.getMessage().contains("节点名称不能为空"),
                "异常信息应该包含'节点名称不能为空'");
        
        // 测试 name 为空字符串
        NodeEntity entity2 = NodeEntity.builder()
                .name("")
                .type("DATABASE")
                .build();
        
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity2, "test-user"),
                "节点名称为空字符串时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception2.getMessage().contains("节点名称不能为空"),
                "异常信息应该包含'节点名称不能为空'");
        
        // 测试 name 为空白字符串
        NodeEntity entity3 = NodeEntity.builder()
                .name("   ")
                .type("DATABASE")
                .build();
        
        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity3, "test-user"),
                "节点名称为空白字符串时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception3.getMessage().contains("节点名称不能为空"),
                "异常信息应该包含'节点名称不能为空'");
    }

    /**
     * 测试必填字段验证 - 节点类型为空
     * <p>
     * 验证：
     * 1. 节点类型为 null 时抛出 IllegalArgumentException
     * 2. 节点类型为空字符串时抛出 IllegalArgumentException
     * 3. 异常信息清晰明确
     * </p>
     */
    @Test
    void testValidateTypeRequired() {
        // 测试 type 为 null
        NodeEntity entity1 = NodeEntity.builder()
                .name("test-node")
                .type(null)
                .build();
        
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity1, "test-user"),
                "节点类型为 null 时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception1.getMessage().contains("节点类型不能为空"),
                "异常信息应该包含'节点类型不能为空'");
        
        // 测试 type 为空字符串
        NodeEntity entity2 = NodeEntity.builder()
                .name("test-node")
                .type("")
                .build();
        
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity2, "test-user"),
                "节点类型为空字符串时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception2.getMessage().contains("节点类型不能为空"),
                "异常信息应该包含'节点类型不能为空'");
    }

    /**
     * 测试唯一性约束验证 - 重复节点名称
     * <p>
     * 验证：
     * 1. 创建名称重复的节点时抛出 BusinessException
     * 2. 异常信息包含"节点名称已存在"
     * 3. 第一个节点成功保存，第二个节点保存失败
     * </p>
     */
    @Test
    void testValidateNameUniqueness() {
        // 保存第一个节点
        NodeEntity entity1 = saveTestNode("test-unique-node", "DATABASE", "test-user");
        assertNotNull(entity1.getId(), "第一个节点应该保存成功");
        
        // 尝试保存名称重复的节点
        NodeEntity entity2 = createTestNode("test-unique-node", "APPLICATION");
        
        Exception exception = assertThrows(
                Exception.class,
                () -> nodeRepository.save(entity2, "test-user"),
                "创建名称重复的节点应该抛出异常"
        );
        
        // 验证异常信息
        assertExceptionMessageContains(exception, "节点名称已存在", "DUPLICATE_KEY");
    }

    /**
     * 测试字段长度验证 - 节点名称超长
     * <p>
     * 验证：
     * 1. 节点名称超过 100 字符时抛出 IllegalArgumentException
     * 2. 异常信息清晰明确
     * </p>
     */
    @Test
    void testValidateNameLength() {
        // 创建超过 100 字符的名称
        String longName = "a".repeat(101);
        
        NodeEntity entity = NodeEntity.builder()
                .name(longName)
                .type("DATABASE")
                .build();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity, "test-user"),
                "节点名称超过 100 字符时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception.getMessage().contains("节点名称长度不能超过 100 个字符"),
                "异常信息应该包含'节点名称长度不能超过 100 个字符'");
    }

    /**
     * 测试字段长度验证 - 节点类型超长
     * <p>
     * 验证：
     * 1. 节点类型超过 50 字符时抛出 IllegalArgumentException
     * 2. 异常信息清晰明确
     * </p>
     */
    @Test
    void testValidateTypeLength() {
        // 创建超过 50 字符的类型
        String longType = "a".repeat(51);
        
        NodeEntity entity = NodeEntity.builder()
                .name("test-node")
                .type(longType)
                .build();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity, "test-user"),
                "节点类型超过 50 字符时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception.getMessage().contains("节点类型长度不能超过 50 个字符"),
                "异常信息应该包含'节点类型长度不能超过 50 个字符'");
    }

    /**
     * 测试字段长度验证 - 节点描述超长
     * <p>
     * 验证：
     * 1. 节点描述超过 500 字符时抛出 IllegalArgumentException
     * 2. 异常信息清晰明确
     * </p>
     */
    @Test
    void testValidateDescriptionLength() {
        // 创建超过 500 字符的描述
        String longDescription = "a".repeat(501);
        
        NodeEntity entity = NodeEntity.builder()
                .name("test-node")
                .type("DATABASE")
                .description(longDescription)
                .build();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity, "test-user"),
                "节点描述超过 500 字符时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception.getMessage().contains("节点描述长度不能超过 500 个字符"),
                "异常信息应该包含'节点描述长度不能超过 500 个字符'");
    }

    /**
     * 测试 JSON 格式验证 - 无效的 JSON
     * <p>
     * 验证：
     * 1. properties 不是有效的 JSON 格式时抛出 IllegalArgumentException
     * 2. 异常信息清晰明确
     * </p>
     */
    @Test
    void testValidatePropertiesJsonFormat() {
        // 测试无效的 JSON 格式
        NodeEntity entity1 = NodeEntity.builder()
                .name("test-node")
                .type("DATABASE")
                .properties("invalid json")
                .build();
        
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity1, "test-user"),
                "properties 不是有效的 JSON 格式时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception1.getMessage().contains("节点属性必须是有效的 JSON 格式"),
                "异常信息应该包含'节点属性必须是有效的 JSON 格式'");
        
        // 测试不完整的 JSON
        NodeEntity entity2 = NodeEntity.builder()
                .name("test-node")
                .type("DATABASE")
                .properties("{\"key\":\"value\"")
                .build();
        
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity2, "test-user"),
                "properties 不是完整的 JSON 时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception2.getMessage().contains("节点属性必须是有效的 JSON 格式"),
                "异常信息应该包含'节点属性必须是有效的 JSON 格式'");
    }

    /**
     * 测试有效的 JSON 格式
     * <p>
     * 验证：
     * 1. 有效的 JSON 对象格式可以保存
     * 2. 有效的 JSON 数组格式可以保存
     * 3. 空的 properties 可以保存
     * </p>
     */
    @Test
    void testValidJsonFormats() {
        // 测试有效的 JSON 对象
        NodeEntity entity1 = NodeEntity.builder()
                .name("test-valid-json-object")
                .type("DATABASE")
                .properties("{\"key\":\"value\"}")
                .build();
        
        assertDoesNotThrow(() -> nodeRepository.save(entity1, "test-user"),
                "有效的 JSON 对象格式应该可以保存");
        assertNotNull(entity1.getId(), "节点应该保存成功");
        
        // 测试有效的 JSON 数组
        NodeEntity entity2 = NodeEntity.builder()
                .name("test-valid-json-array")
                .type("DATABASE")
                .properties("[\"value1\",\"value2\"]")
                .build();
        
        assertDoesNotThrow(() -> nodeRepository.save(entity2, "test-user"),
                "有效的 JSON 数组格式应该可以保存");
        assertNotNull(entity2.getId(), "节点应该保存成功");
        
        // 测试 properties 为 null
        NodeEntity entity3 = NodeEntity.builder()
                .name("test-null-properties")
                .type("DATABASE")
                .properties(null)
                .build();
        
        assertDoesNotThrow(() -> nodeRepository.save(entity3, "test-user"),
                "properties 为 null 应该可以保存");
        assertNotNull(entity3.getId(), "节点应该保存成功");
    }

    /**
     * 测试更新时的数据验证
     * <p>
     * 验证：
     * 1. 更新时也会进行数据验证
     * 2. 验证失败时抛出相应的异常
     * </p>
     */
    @Test
    void testValidationOnUpdate() {
        // 先保存一个节点
        NodeEntity entity = saveTestNode("test-update-validation", "DATABASE", "test-user");
        assertNotNull(entity.getId());
        
        // 尝试更新为空名称
        entity.setName("");
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.update(entity, "test-user"),
                "更新为空名称时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception1.getMessage().contains("节点名称不能为空"),
                "异常信息应该包含'节点名称不能为空'");
        
        // 恢复名称，尝试更新为超长描述
        entity.setName("test-update-validation");
        entity.setDescription("a".repeat(501));
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.update(entity, "test-user"),
                "更新为超长描述时应该抛出 IllegalArgumentException"
        );
        assertTrue(exception2.getMessage().contains("节点描述长度不能超过 500 个字符"),
                "异常信息应该包含'节点描述长度不能超过 500 个字符'");
    }

    // ==================== 异常处理测试 ====================

    /**
     * 测试数据库唯一约束冲突的异常转换和处理
     * <p>
     * 验证：
     * 1. 唯一约束冲突时抛出 BusinessException
     * 2. 异常信息包含"节点名称已存在"或"DUPLICATE_KEY"
     * 3. 异常信息清晰，便于问题定位
     * </p>
     */
    @Test
    void testHandleDuplicateKeyException() {
        // 保存第一个节点
        NodeEntity entity1 = saveTestNode("test-duplicate-exception", "DATABASE", "test-user");
        assertNotNull(entity1.getId(), "第一个节点应该保存成功");
        
        // 尝试保存名称重复的节点，验证异常处理
        NodeEntity entity2 = createTestNode("test-duplicate-exception", "APPLICATION");
        
        Exception exception = assertThrows(
                Exception.class,
                () -> nodeRepository.save(entity2, "test-user"),
                "创建名称重复的节点应该抛出异常"
        );
        
        // 验证异常信息清晰明确
        assertExceptionMessageContains(exception, "节点名称已存在", "DUPLICATE_KEY", "Duplicate");
        
        // 验证异常类型（应该是 BusinessException 或其父类）
        assertTrue(exception instanceof RuntimeException,
                "应该抛出 RuntimeException 或其子类");
    }

    /**
     * 测试乐观锁版本冲突的异常转换和处理
     * <p>
     * 验证：
     * 1. 乐观锁冲突时抛出 BusinessException
     * 2. 异常信息包含"数据已被其他用户修改"或"OPTIMISTIC_LOCK_ERROR"
     * 3. 异常信息清晰，便于问题定位
     * </p>
     */
    @Test
    void testHandleOptimisticLockException() {
        // 保存一个节点
        NodeEntity entity = saveTestNode("test-optimistic-lock", "DATABASE", "test-user");
        assertNotNull(entity.getId());
        
        // 查询两次，得到两个 Entity 对象（version 相同）
        NodeEntity entity1 = nodeRepository.findById(entity.getId());
        NodeEntity entity2 = nodeRepository.findById(entity.getId());
        
        assertNotNull(entity1, "第一次查询应该成功");
        assertNotNull(entity2, "第二次查询应该成功");
        assertEquals(entity1.getVersion(), entity2.getVersion(), "两次查询的 version 应该相同");
        
        // 更新第一个 Entity，成功
        entity1.setDescription("Updated by user1");
        nodeRepository.update(entity1, "user1");
        
        // 尝试更新第二个 Entity，应该失败（version 已经变化）
        entity2.setDescription("Updated by user2");
        
        Exception exception = assertThrows(
                Exception.class,
                () -> nodeRepository.update(entity2, "user2"),
                "并发更新同一节点时，后更新的操作应该抛出异常"
        );
        
        // 验证异常信息清晰明确
        assertExceptionMessageContains(exception, "数据已被其他用户修改", "OPTIMISTIC_LOCK_ERROR", 
                "version", "更新失败", "0 rows");
    }

    /**
     * 测试数据库连接异常的异常转换和处理
     * <p>
     * 注意：此测试无法在正常的集成测试环境中模拟真实的数据库连接失败，
     * 因为 Spring Boot 测试会确保数据库连接正常。
     * 这里主要验证异常处理的代码结构是否正确。
     * <p>
     * 验证：
     * 1. 异常处理机制存在
     * 2. 正常情况下不会抛出数据库连接异常
     * </p>
     */
    @Test
    void testDatabaseConnectionHandling() {
        // 在正常的测试环境中，数据库连接应该是正常的
        // 这里验证正常操作不会抛出数据库连接异常
        
        NodeEntity entity = createTestNode("test-db-connection", "DATABASE");
        
        // 正常保存应该成功
        assertDoesNotThrow(() -> nodeRepository.save(entity, "test-user"),
                "正常情况下保存操作不应该抛出异常");
        assertNotNull(entity.getId(), "节点应该保存成功");
        
        // 正常查询应该成功
        NodeEntity found = assertDoesNotThrow(() -> nodeRepository.findById(entity.getId()),
                "正常情况下查询操作不应该抛出异常");
        assertNotNull(found, "应该能查询到节点");
        
        // 正常更新应该成功
        found.setDescription("Updated");
        assertDoesNotThrow(() -> nodeRepository.update(found, "test-user"),
                "正常情况下更新操作不应该抛出异常");
        
        // 正常删除应该成功
        assertDoesNotThrow(() -> nodeRepository.deleteById(entity.getId(), "test-user"),
                "正常情况下删除操作不应该抛出异常");
    }

    /**
     * 测试异常信息的完整性和可读性
     * <p>
     * 验证：
     * 1. 所有异常都包含清晰的错误信息
     * 2. 异常信息便于问题定位
     * 3. 异常信息不泄露敏感信息
     * </p>
     */
    @Test
    void testExceptionMessageQuality() {
        // 测试验证异常的信息质量
        NodeEntity entity1 = NodeEntity.builder()
                .name(null)
                .type("DATABASE")
                .build();
        
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity1, "test-user")
        );
        
        String message1 = exception1.getMessage();
        assertNotNull(message1, "异常信息不应该为 null");
        assertFalse(message1.isEmpty(), "异常信息不应该为空");
        assertTrue(message1.length() > 5, "异常信息应该有足够的描述性");
        assertTrue(message1.contains("节点名称") || message1.contains("name"),
                "异常信息应该指出具体的字段");
        
        // 测试长度验证异常的信息质量
        NodeEntity entity2 = NodeEntity.builder()
                .name("a".repeat(101))
                .type("DATABASE")
                .build();
        
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity2, "test-user")
        );
        
        String message2 = exception2.getMessage();
        assertNotNull(message2, "异常信息不应该为 null");
        assertTrue(message2.contains("长度") || message2.contains("100"),
                "异常信息应该指出长度限制");
        
        // 测试 JSON 格式验证异常的信息质量
        NodeEntity entity3 = NodeEntity.builder()
                .name("test-node")
                .type("DATABASE")
                .properties("invalid json")
                .build();
        
        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> nodeRepository.save(entity3, "test-user")
        );
        
        String message3 = exception3.getMessage();
        assertNotNull(message3, "异常信息不应该为 null");
        assertTrue(message3.contains("JSON") || message3.contains("格式"),
                "异常信息应该指出 JSON 格式问题");
    }

    /**
     * 测试更新不存在的节点
     * <p>
     * 验证：
     * 1. 更新不存在的节点时抛出异常
     * 2. 异常信息清晰明确
     * </p>
     */
    @Test
    void testUpdateNonExistentNode() {
        // 创建一个不存在的节点（ID 不存在）
        NodeEntity entity = NodeEntity.builder()
                .id(999999L)
                .name("non-existent-node")
                .type("DATABASE")
                .version(0)
                .build();
        
        // 尝试更新不存在的节点
        Exception exception = assertThrows(
                Exception.class,
                () -> nodeRepository.update(entity, "test-user"),
                "更新不存在的节点应该抛出异常"
        );
        
        // 验证异常信息
        String message = exception.getMessage();
        assertNotNull(message, "异常信息不应该为 null");
        // 可能是 IllegalArgumentException（节点不存在）或其他异常
        assertTrue(exception instanceof RuntimeException,
                "应该抛出 RuntimeException 或其子类");
    }

    /**
     * 测试删除不存在的节点
     * <p>
     * 验证：
     * 1. 删除不存在的节点时抛出异常
     * 2. 异常信息清晰明确
     * </p>
     */
    @Test
    void testDeleteNonExistentNode() {
        // 尝试删除不存在的节点
        Exception exception = assertThrows(
                Exception.class,
                () -> nodeRepository.deleteById(999999L, "test-user"),
                "删除不存在的节点应该抛出异常"
        );
        
        // 验证异常信息
        String message = exception.getMessage();
        assertNotNull(message, "异常信息不应该为 null");
        assertTrue(exception instanceof RuntimeException,
                "应该抛出 RuntimeException 或其子类");
    }

    // ==================== 边界情况测试 ====================

    /**
     * 测试分页边界场景 - 第一页
     * <p>
     * 验证：
     * 1. 查询第一页数据正常
     * 2. 分页信息正确
     * 3. 数据列表不为空（如果有数据）
     * </p>
     */
    @Test
    void testPaginationFirstPage() {
        // 准备测试数据
        for (int i = 1; i <= 10; i++) {
            saveTestNode("test-pagination-first-" + i, "DATABASE", "test-user");
        }
        
        // 查询第一页
        PageResult<NodeEntity> page = nodeRepository.findPage(1, 5, null, null);
        
        // 验证结果
        assertNotNull(page, "应该返回分页结果");
        assertEquals(1L, page.getCurrent(), "当前页应该为 1");
        assertEquals(5L, page.getSize(), "每页大小应该为 5");
        assertTrue(page.getTotal() >= 10, "总记录数应该至少为 10");
        assertEquals(5, page.getRecords().size(), "第一页应该有 5 条记录");
        assertTrue(page.getPages() >= 2, "总页数应该至少为 2");
    }

    /**
     * 测试分页边界场景 - 最后一页
     * <p>
     * 验证：
     * 1. 查询最后一页数据正常
     * 2. 最后一页的记录数可能少于每页大小
     * 3. 分页信息正确
     * </p>
     */
    @Test
    void testPaginationLastPage() {
        // 准备测试数据（13 条记录）
        for (int i = 1; i <= 13; i++) {
            saveTestNode("test-pagination-last-" + i, "APPLICATION", "test-user");
        }
        
        // 先查询第一页获取总页数
        PageResult<NodeEntity> firstPage = nodeRepository.findPage(1, 5, null, "APPLICATION");
        long totalPages = firstPage.getPages();
        assertTrue(totalPages >= 3, "总页数应该至少为 3");
        
        // 查询最后一页
        PageResult<NodeEntity> lastPage = nodeRepository.findPage((int) totalPages, 5, null, "APPLICATION");
        
        // 验证结果
        assertNotNull(lastPage, "应该返回分页结果");
        assertEquals(totalPages, lastPage.getCurrent(), "当前页应该为最后一页");
        assertEquals(5L, lastPage.getSize(), "每页大小应该为 5");
        assertTrue(lastPage.getRecords().size() > 0, "最后一页应该有数据");
        assertTrue(lastPage.getRecords().size() <= 5, "最后一页的记录数不应该超过每页大小");
    }

    /**
     * 测试分页边界场景 - 超出范围
     * <p>
     * 验证：
     * 1. 查询超出范围的页码时返回空列表
     * 2. 分页信息正确
     * 3. 不抛出异常
     * </p>
     */
    @Test
    void testPaginationOutOfRange() {
        // 准备测试数据
        for (int i = 1; i <= 5; i++) {
            saveTestNode("test-pagination-out-" + i, "API", "test-user");
        }
        
        // 查询超出范围的页码（第 100 页）
        PageResult<NodeEntity> page = nodeRepository.findPage(100, 5, null, "API");
        
        // 验证结果
        assertNotNull(page, "应该返回分页结果");
        assertEquals(100L, page.getCurrent(), "当前页应该为 100");
        assertEquals(5L, page.getSize(), "每页大小应该为 5");
        assertTrue(page.getTotal() >= 5, "总记录数应该至少为 5");
        assertEquals(0, page.getRecords().size(), "超出范围的页码应该返回空列表");
    }

    /**
     * 测试分页边界场景 - 空结果集
     * <p>
     * 验证：
     * 1. 查询不存在的数据时返回空列表
     * 2. 分页信息正确
     * 3. 总记录数为 0
     * </p>
     */
    @Test
    void testPaginationEmptyResult() {
        // 查询不存在的类型
        PageResult<NodeEntity> page = nodeRepository.findPage(1, 10, null, "NON_EXISTENT_TYPE");
        
        // 验证结果
        assertNotNull(page, "应该返回分页结果");
        assertEquals(1L, page.getCurrent(), "当前页应该为 1");
        assertEquals(10L, page.getSize(), "每页大小应该为 10");
        assertEquals(0L, page.getTotal(), "总记录数应该为 0");
        assertEquals(0L, page.getPages(), "总页数应该为 0");
        assertEquals(0, page.getRecords().size(), "应该返回空列表");
    }

    /**
     * 测试分页边界场景 - 单条记录
     * <p>
     * 验证：
     * 1. 只有一条记录时分页正常
     * 2. 总页数为 1
     * 3. 数据正确返回
     * </p>
     */
    @Test
    void testPaginationSingleRecord() {
        // 准备测试数据（只有一条记录）
        saveTestNode("test-pagination-single", "REPORT", "test-user");
        
        // 查询第一页
        PageResult<NodeEntity> page = nodeRepository.findPage(1, 10, null, "REPORT");
        
        // 验证结果
        assertNotNull(page, "应该返回分页结果");
        assertEquals(1L, page.getCurrent(), "当前页应该为 1");
        assertEquals(10L, page.getSize(), "每页大小应该为 10");
        assertTrue(page.getTotal() >= 1, "总记录数应该至少为 1");
        assertEquals(1L, page.getPages(), "总页数应该为 1");
        assertEquals(1, page.getRecords().size(), "应该返回 1 条记录");
        assertEquals("test-pagination-single", page.getRecords().get(0).getName());
    }

    /**
     * 测试并发更新场景 - 模拟多线程同时更新同一记录
     * <p>
     * 验证：
     * 1. 乐观锁机制能够防止并发更新冲突
     * 2. 第一个更新操作成功
     * 3. 第二个更新操作因为版本号不匹配而失败
     * </p>
     */
    @Test
    void testConcurrentUpdate() {
        // 准备测试数据
        NodeEntity entity = saveTestNode("test-concurrent-update", "DATABASE", "test-user");
        Long nodeId = entity.getId();
        
        // 查询两次，得到两个 Entity 对象（version 相同）
        NodeEntity entity1 = nodeRepository.findById(nodeId);
        NodeEntity entity2 = nodeRepository.findById(nodeId);
        
        assertNotNull(entity1, "第一次查询应该成功");
        assertNotNull(entity2, "第二次查询应该成功");
        assertEquals(entity1.getVersion(), entity2.getVersion(), "两次查询的 version 应该相同");
        assertEquals(0, entity1.getVersion(), "初始版本号应该为 0");
        
        // 更新第一个 Entity，应该成功
        entity1.setDescription("Updated by user1");
        nodeRepository.update(entity1, "user1");
        assertEquals(1, entity1.getVersion(), "第一次更新后版本号应该为 1");
        
        // 尝试更新第二个 Entity，应该失败（version 已经变化）
        entity2.setDescription("Updated by user2");
        
        Exception exception = assertThrows(
                Exception.class,
                () -> nodeRepository.update(entity2, "user2"),
                "并发更新同一节点时，后更新的操作应该抛出异常"
        );
        
        // 验证异常信息
        assertExceptionMessageContains(exception, "数据已被其他用户修改", "OPTIMISTIC_LOCK_ERROR", 
                "version", "更新失败", "0 rows");
        
        // 验证最终的版本号
        NodeEntity finalNode = nodeRepository.findById(nodeId);
        assertNotNull(finalNode);
        assertEquals(1, finalNode.getVersion(), "版本号应该为 1（只更新了一次）");
        assertEquals("Updated by user1", finalNode.getDescription(), "应该保留第一次更新的内容");
    }

    /**
     * 测试大数据量场景 - 批量插入
     * <p>
     * 验证：
     * 1. 能够批量插入大量数据
     * 2. 所有数据都能正确保存
     * 3. 性能在合理范围内
     * </p>
     */
    @Test
    void testBatchInsertLargeData() {
        // 批量插入 100 条记录（降低数量以加快测试速度）
        int batchSize = 100;
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= batchSize; i++) {
            saveTestNode("test-batch-insert-" + i, "OTHER", "test-user");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证所有数据都已保存
        PageResult<NodeEntity> page = nodeRepository.findPage(1, batchSize, "test-batch-insert", "OTHER");
        assertNotNull(page, "应该返回分页结果");
        assertTrue(page.getTotal() >= batchSize, "应该至少有 " + batchSize + " 条记录");
        
        // 验证性能（批量插入 100 条记录应该在 10 秒内完成）
        assertTrue(duration < 10000, 
                "批量插入 " + batchSize + " 条记录应该在 10 秒内完成，实际耗时: " + duration + "ms");
        
        System.out.println("批量插入 " + batchSize + " 条记录耗时: " + duration + "ms");
    }

    /**
     * 测试大数据量场景 - 批量查询
     * <p>
     * 验证：
     * 1. 能够查询大量数据
     * 2. 查询性能在合理范围内
     * 3. 分页查询正常工作
     * </p>
     */
    @Test
    void testBatchQueryLargeData() {
        // 准备测试数据（50 条记录）
        int dataSize = 50;
        for (int i = 1; i <= dataSize; i++) {
            saveTestNode("test-batch-query-" + i, "DATABASE", "test-user");
        }
        
        // 测试分页查询性能
        long startTime = System.currentTimeMillis();
        
        PageResult<NodeEntity> page = nodeRepository.findPage(1, 50, "test-batch-query", "DATABASE");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证结果
        assertNotNull(page, "应该返回分页结果");
        assertTrue(page.getTotal() >= dataSize, "应该至少有 " + dataSize + " 条记录");
        assertTrue(page.getRecords().size() >= dataSize, "应该返回至少 " + dataSize + " 条记录");
        
        // 验证性能（查询 50 条记录应该在 1 秒内完成）
        assertTrue(duration < 1000, 
                "查询 " + dataSize + " 条记录应该在 1 秒内完成，实际耗时: " + duration + "ms");
        
        System.out.println("查询 " + dataSize + " 条记录耗时: " + duration + "ms");
    }

    /**
     * 测试特殊字符场景 - SQL 注入字符
     * <p>
     * 验证：
     * 1. 能够正确处理包含 SQL 注入字符的数据
     * 2. 参数化查询防止 SQL 注入
     * 3. 数据能够正确保存和查询
     * </p>
     */
    @Test
    void testSpecialCharactersSqlInjection() {
        // 测试包含 SQL 注入字符的节点名称
        String[] sqlInjectionStrings = {
            "test'; DROP TABLE t_node; --",
            "test' OR '1'='1",
            "test\" OR \"1\"=\"1",
            "test'; DELETE FROM t_node WHERE '1'='1",
            "test' UNION SELECT * FROM t_node --"
        };
        
        for (int i = 0; i < sqlInjectionStrings.length; i++) {
            String name = "test-sql-injection-" + i;
            NodeEntity entity = NodeEntity.builder()
                    .name(name)
                    .type("DATABASE")
                    .description(sqlInjectionStrings[i])
                    .build();
            
            // 保存应该成功（参数化查询防止 SQL 注入）
            assertDoesNotThrow(() -> nodeRepository.save(entity, "test-user"),
                    "包含 SQL 注入字符的数据应该能够正确保存");
            assertNotNull(entity.getId(), "节点应该保存成功");
            
            // 查询应该成功
            NodeEntity found = nodeRepository.findById(entity.getId());
            assertNotNull(found, "应该能查询到节点");
            assertEquals(sqlInjectionStrings[i], found.getDescription(), 
                    "描述字段应该完整保存，包括特殊字符");
        }
        
        // 验证数据库表仍然存在（没有被 SQL 注入破坏）
        PageResult<NodeEntity> page = nodeRepository.findPage(1, 10, null, null);
        assertNotNull(page, "数据库表应该仍然存在");
    }

    /**
     * 测试特殊字符场景 - Unicode 字符
     * <p>
     * 验证：
     * 1. 能够正确处理 Unicode 字符（中文、日文、韩文、表情符号等）
     * 2. 数据能够正确保存和查询
     * 3. 字符不会乱码
     * </p>
     */
    @Test
    void testSpecialCharactersUnicode() {
        // 测试各种 Unicode 字符
        String[] unicodeStrings = {
            "测试节点-中文",
            "テストノード-日本語",
            "테스트노드-한국어",
            "Test Node 😀😁😂🤣",
            "Тестовый узел-Русский",
            "عقدة الاختبار-العربية"
        };
        
        for (int i = 0; i < unicodeStrings.length; i++) {
            NodeEntity entity = NodeEntity.builder()
                    .name("test-unicode-" + i)
                    .type("DATABASE")
                    .description(unicodeStrings[i])
                    .build();
            
            // 保存应该成功
            assertDoesNotThrow(() -> nodeRepository.save(entity, "test-user"),
                    "包含 Unicode 字符的数据应该能够正确保存");
            assertNotNull(entity.getId(), "节点应该保存成功");
            
            // 查询应该成功
            NodeEntity found = nodeRepository.findById(entity.getId());
            assertNotNull(found, "应该能查询到节点");
            assertEquals(unicodeStrings[i], found.getDescription(), 
                    "Unicode 字符应该正确保存和查询，不应该乱码");
        }
    }

    /**
     * 测试特殊字符场景 - 特殊符号
     * <p>
     * 验证：
     * 1. 能够正确处理各种特殊符号
     * 2. 数据能够正确保存和查询
     * 3. 特殊符号不会导致解析错误
     * </p>
     */
    @Test
    void testSpecialCharactersSymbols() {
        // 测试各种特殊符号
        String[] specialSymbols = {
            "test<node>",
            "test&node",
            "test\"node\"",
            "test'node'",
            "test\\node",
            "test/node",
            "test|node",
            "test\nnode",
            "test\tnode",
            "test%node"
        };
        
        for (int i = 0; i < specialSymbols.length; i++) {
            NodeEntity entity = NodeEntity.builder()
                    .name("test-symbol-" + i)
                    .type("DATABASE")
                    .description(specialSymbols[i])
                    .build();
            
            // 保存应该成功
            assertDoesNotThrow(() -> nodeRepository.save(entity, "test-user"),
                    "包含特殊符号的数据应该能够正确保存");
            assertNotNull(entity.getId(), "节点应该保存成功");
            
            // 查询应该成功
            NodeEntity found = nodeRepository.findById(entity.getId());
            assertNotNull(found, "应该能查询到节点");
            assertEquals(specialSymbols[i], found.getDescription(), 
                    "特殊符号应该正确保存和查询");
        }
    }

    /**
     * 测试边界值 - 字段长度边界
     * <p>
     * 验证：
     * 1. 字段长度刚好等于最大长度时能够正确保存
     * 2. 边界值处理正确
     * </p>
     */
    @Test
    void testBoundaryFieldLength() {
        // 测试节点名称刚好 100 字符
        String name100 = "a".repeat(100);
        NodeEntity entity1 = NodeEntity.builder()
                .name(name100)
                .type("DATABASE")
                .build();
        
        assertDoesNotThrow(() -> nodeRepository.save(entity1, "test-user"),
                "节点名称刚好 100 字符应该能够保存");
        assertNotNull(entity1.getId(), "节点应该保存成功");
        
        NodeEntity found1 = nodeRepository.findById(entity1.getId());
        assertNotNull(found1);
        assertEquals(100, found1.getName().length(), "节点名称长度应该为 100");
        
        // 测试节点类型刚好 50 字符
        String type50 = "b".repeat(50);
        NodeEntity entity2 = NodeEntity.builder()
                .name("test-boundary-type")
                .type(type50)
                .build();
        
        assertDoesNotThrow(() -> nodeRepository.save(entity2, "test-user"),
                "节点类型刚好 50 字符应该能够保存");
        assertNotNull(entity2.getId(), "节点应该保存成功");
        
        NodeEntity found2 = nodeRepository.findById(entity2.getId());
        assertNotNull(found2);
        assertEquals(50, found2.getType().length(), "节点类型长度应该为 50");
        
        // 测试节点描述刚好 500 字符
        String description500 = "c".repeat(500);
        NodeEntity entity3 = NodeEntity.builder()
                .name("test-boundary-description")
                .type("DATABASE")
                .description(description500)
                .build();
        
        assertDoesNotThrow(() -> nodeRepository.save(entity3, "test-user"),
                "节点描述刚好 500 字符应该能够保存");
        assertNotNull(entity3.getId(), "节点应该保存成功");
        
        NodeEntity found3 = nodeRepository.findById(entity3.getId());
        assertNotNull(found3);
        assertEquals(500, found3.getDescription().length(), "节点描述长度应该为 500");
    }

    /**
     * 测试空字符串和空白字符串的区别
     * <p>
     * 验证：
     * 1. 空字符串和空白字符串都应该被视为无效
     * 2. null 值的处理正确
     * </p>
     */
    @Test
    void testEmptyAndBlankStrings() {
        // 测试空字符串
        NodeEntity entity1 = NodeEntity.builder()
                .name("")
                .type("DATABASE")
                .build();
        
        assertThrows(IllegalArgumentException.class,
                () -> nodeRepository.save(entity1, "test-user"),
                "空字符串应该被视为无效");
        
        // 测试空白字符串
        NodeEntity entity2 = NodeEntity.builder()
                .name("   ")
                .type("DATABASE")
                .build();
        
        assertThrows(IllegalArgumentException.class,
                () -> nodeRepository.save(entity2, "test-user"),
                "空白字符串应该被视为无效");
        
        // 测试 null 值
        NodeEntity entity3 = NodeEntity.builder()
                .name(null)
                .type("DATABASE")
                .build();
        
        assertThrows(IllegalArgumentException.class,
                () -> nodeRepository.save(entity3, "test-user"),
                "null 值应该被视为无效");
    }
}
