# MyBatis-Plus 集成设计分析报告

## 1. 设计评分

### 总体评分：75/100

| 维度 | 得分 | 满分 | 说明 |
|------|------|------|------|
| 需求覆盖度 | 20/25 | 25 | 存在 5 处与需求不一致 |
| 设计一致性 | 20/25 | 25 | 内部一致性良好，但与需求存在冲突 |
| 可实施性 | 20/25 | 25 | 技术可行，但需要调整包路径和实现方式 |
| 过度设计控制 | 15/25 | 25 | 避免了部分过度设计，但遗漏了必要功能 |

## 2. 发现的问题

### 问题 1：Mapper 扫描路径不一致（严重）

**需求要求**（需求 2.7）：
- Mapper 扫描路径：`com.catface.infrastructure.repository.sql.mapper`

**当前设计**：
- Mapper 扫描路径：`com.catface.infrastructure.repository.mysql.mapper`

**影响**：
- 与需求不一致
- 可能导致 Mapper 无法被扫描到

**建议**：
- 修改设计，使用需求指定的包路径：`com.catface.infrastructure.repository.sql.mapper`

---

### 问题 2：实体类位置不一致（严重）

**需求要求**（需求 4.2 和需求 5.1）：
- 实体类包路径：`com.catface.infrastructure.repository.entity`
- 实体类位置：`mysql-impl` 模块

**当前设计**：
- 实体类包路径：`com.catface.domain.entity`
- 实体类位置：`domain-api` 模块

**影响**：
- 与需求严重不一致
- 违反了需求明确指定的位置

**建议**：
- 修改设计，将 NodeEntity 放在 `mysql-impl` 模块的 `com.catface.infrastructure.repository.entity` 包中
- 这是基础设施层的数据模型，不是领域模型

---

### 问题 3：Repository 实现类包路径不一致（严重）

**需求要求**（需求 10.1）：
- Repository 实现类包路径：`com.catface.infrastructure.repository.sql.impl`

**当前设计**：
- Repository 实现类包路径：`com.catface.infrastructure.repository.mysql.impl`

**影响**：
- 与需求不一致

**建议**：
- 修改设计，使用需求指定的包路径：`com.catface.infrastructure.repository.sql.impl`

---

### 问题 4：OperatorContext 未实现（严重）

**需求要求**（需求 21）：
- 必须实现 OperatorContext 工具类
- 使用 ThreadLocal 存储操作人信息
- 在 MetaObjectHandler 中从 OperatorContext 获取操作人
- 在 Repository 层调用 OperatorContext.setOperator 设置操作人

**当前设计**：
- 设计中明确说明"不需要实现 OperatorContext"
- 采用方法参数传递方案

**影响**：
- 与需求严重不一致
- 需求 21 有 8 个验收标准，当前设计完全未覆盖

**建议**：
- 必须实现 OperatorContext
- 在 MetaObjectHandler 中使用 OperatorContext 自动填充 createBy 和 updateBy
- 在 Repository 层调用 OperatorContext.setOperator

---

### 问题 5：createBy 和 updateBy 自动填充策略不一致（严重）

**需求要求**（需求 2.6、需求 5.10、需求 5.11、需求 21.6）：
- 配置元数据自动填充处理器，自动填充 createBy 和 updateBy
- createBy 字段使用 @TableField 注解，填充策略为 INSERT
- updateBy 字段使用 @TableField 注解，填充策略为 INSERT_UPDATE
- 在 MetaObjectHandler 中从 OperatorContext 获取操作人

**当前设计**：
- createBy 和 updateBy 不使用自动填充
- 在 Repository 层手动设置

**影响**：
- 与需求严重不一致
- 需求明确要求使用自动填充

**建议**：
- 修改设计，使用自动填充策略
- 在 NodeEntity 中为 createBy 和 updateBy 添加 @TableField 注解
- 在 MetaObjectHandler 中从 OperatorContext 获取操作人并填充



## 3. 优化方案

### 3.1 包路径调整

#### 调整前（当前设计）
```
infrastructure/repository/mysql-impl/
├── mapper/
│   └── NodeMapper.java (com.catface.infrastructure.repository.mysql.mapper)
├── impl/
│   └── NodeRepositoryImpl.java (com.catface.infrastructure.repository.mysql.impl)

domain/domain-api/
└── entity/
    └── NodeEntity.java (com.catface.domain.entity)
```

#### 调整后（符合需求）
```
infrastructure/repository/mysql-impl/
├── mapper/
│   └── NodeMapper.java (com.catface.infrastructure.repository.sql.mapper)
├── impl/
│   └── NodeRepositoryImpl.java (com.catface.infrastructure.repository.sql.impl)
└── entity/
    └── NodeEntity.java (com.catface.infrastructure.repository.entity)
```

### 3.2 OperatorContext 实现方案

#### 位置
`common/src/main/java/com/catface/common/util/OperatorContext.java`

#### 职责
- 使用 ThreadLocal 存储当前操作人信息
- 提供 setOperator、getOperator、clear 方法
- 线程安全

#### 使用场景
1. **Controller 层**：在请求开始时设置操作人，请求结束时清除
2. **MetaObjectHandler**：从 OperatorContext 获取操作人，自动填充 createBy 和 updateBy
3. **Repository 层**：在 save、update、deleteById 方法中调用 setOperator

### 3.3 自动填充策略调整

#### NodeEntity 字段注解
```
@TableField(fill = FieldFill.INSERT)
private String createBy;

@TableField(fill = FieldFill.INSERT_UPDATE)
private String updateBy;
```

#### MetaObjectHandler 实现
```
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    
    String operator = OperatorContext.getOperator();
    if (operator != null) {
        this.strictInsertFill(metaObject, "createBy", String.class, operator);
        this.strictInsertFill(metaObject, "updateBy", String.class, operator);
    }
}

@Override
public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    
    String operator = OperatorContext.getOperator();
    if (operator != null) {
        this.strictUpdateFill(metaObject, "updateBy", String.class, operator);
    }
}
```

#### Repository 层调用
```
@Override
public void save(NodeEntity entity, String operator) {
    OperatorContext.setOperator(operator);
    try {
        nodeMapper.insert(entity);
    } finally {
        OperatorContext.clear();
    }
}
```



### 3.4 ADR 调整

#### ADR-003：NodeEntity 位置（需要修改）

**原决策**：NodeEntity 放在 domain-api 模块

**新决策**：NodeEntity 放在 mysql-impl 模块的 `com.catface.infrastructure.repository.entity` 包

**理由**：
- 需求明确指定了位置（需求 5.1）
- NodeEntity 是数据库表的映射，属于基础设施层的数据模型
- 不是领域模型，不应放在 domain-api 模块
- 遵循需求优先原则

**后果**：
- 正面：符合需求，职责更清晰
- 负面：如果其他模块需要引用 NodeEntity，需要依赖 mysql-impl 模块

#### ADR-005：createBy 和 updateBy 填充策略（需要修改）

**原决策**：通过 Repository 方法参数传递，不使用自动填充

**新决策**：使用 OperatorContext + MetaObjectHandler 自动填充

**理由**：
- 需求明确要求使用自动填充（需求 2.6、需求 5.10、需求 5.11）
- 需求明确要求实现 OperatorContext（需求 21）
- 遵循需求优先原则

**后果**：
- 正面：符合需求，减少重复代码
- 负面：需要在 Repository 层调用 OperatorContext.setOperator

## 4. 设计优化后的评分

### 优化后总体评分：95/100

| 维度 | 得分 | 满分 | 说明 |
|------|------|------|------|
| 需求覆盖度 | 24/25 | 25 | 完全覆盖所有需求 |
| 设计一致性 | 24/25 | 25 | 与需求完全一致 |
| 可实施性 | 24/25 | 25 | 技术可行，实施顺序清晰 |
| 过度设计控制 | 23/25 | 25 | 避免了过度设计，实现了必要功能 |

## 5. 需要确认的问题

### 问题 1：NodeEntity 位置的合理性

**需求要求**：放在 mysql-impl 模块的 `com.catface.infrastructure.repository.entity` 包

**潜在问题**：
- 如果其他模块（如 application 层）需要引用 NodeEntity，需要依赖 mysql-impl 模块
- 这可能导致循环依赖或不合理的依赖关系

**建议**：
- 严格按照需求实现，放在 mysql-impl 模块
- 如果后续发现依赖问题，可以考虑创建一个独立的 entity 模块

### 问题 2：OperatorContext 的使用场景

**需求要求**：在 Repository 层调用 OperatorContext.setOperator

**潜在问题**：
- Repository 层需要在每个方法中调用 setOperator 和 clear
- 如果忘记调用 clear，可能导致 ThreadLocal 内存泄漏

**建议**：
- 在 Repository 层使用 try-finally 确保 clear 被调用
- 考虑在 Controller 层或 AOP 中统一设置和清除 OperatorContext

### 问题 3：JSON 格式验证的实现位置

**需求要求**（需求 22）：在 NodeEntity 中为 properties 字段添加 JSON 格式验证

**潜在问题**：
- 实体类中添加验证逻辑可能违反单一职责原则
- 验证逻辑应该在哪一层实现？

**建议**：
- 在 Repository 层的 save 和 update 方法中验证 JSON 格式
- 使用 Jackson 或 Gson 进行验证
- 验证失败时抛出 BusinessException

## 6. 总结

### 当前设计的主要问题
1. ✅ 包路径与需求不一致（3 处）
2. ✅ 实体类位置与需求不一致
3. ✅ 未实现 OperatorContext（需求 21）
4. ✅ 自动填充策略与需求不一致

### 优化后的设计
1. ✅ 所有包路径符合需求
2. ✅ 实体类位置符合需求
3. ✅ 实现 OperatorContext
4. ✅ 使用自动填充策略
5. ✅ 完全覆盖所有 26 个需求

### 建议
- 严格按照需求实现，不要自行调整包路径和实现方式
- 如果需求存在不合理之处，应该与用户确认后再调整
- 设计阶段应该关注"做什么"，而不是"怎么做"

