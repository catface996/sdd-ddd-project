# MyBatis-Plus 集成与 NodeEntity 实现设计文档

## 1. 概述

### 1.1 设计目标

在 OrderCore 系统中集成 MyBatis-Plus ORM 框架，实现 NodeEntity 持久化功能。设计遵循 DDD 分层架构原则和项目的 MyBatis-Plus 最佳实践规范。

### 1.2 设计范围

- MyBatis-Plus 框架集成和配置
- NodeEntity 实体类和数据库表设计
- 数据访问层（Mapper 和 Repository）设计
- 多环境数据源配置
- 自动填充和拦截器配置
- 错误处理和测试策略

### 1.3 关键约束

- 遵循 DDD 分层架构
- 遵循 MyBatis-Plus 最佳实践规范
- 条件查询必须在 Mapper XML 中定义
- 每个任务完成后项目必须可编译
- 支持多环境配置（local、dev、test、staging、prod）

## 2. 架构设计

### 2.1 模块划分

```
order-core-parent/
├── common/                          # 通用模块（无新增内容）
├── infrastructure/
│   └── repository/
│       ├── repository-api/
│       │   ├── api/
│       │   │   └── NodeRepository  # 仓储接口（新增）
│       │   └── entity/
│       │       └── NodeEntity      # 领域实体（新增，纯 POJO）
│       └── mysql-impl/
│           ├── config/
│           │   ├── MybatisPlusConfig       # MyBatis-Plus 配置类（新增）
│           │   └── CustomMetaObjectHandler # 元数据填充处理器（新增）
│           ├── po/
│           │   └── NodePO          # 持久化对象（新增，包含注解）
│           ├── mapper/
│           │   └── NodeMapper      # MyBatis Mapper 接口（新增）
│           ├── impl/
│           │   └── NodeRepositoryImpl  # 仓储实现（新增，负责 Entity/PO 转换）
│           └── resources/
│               ├── mapper/
│               │   └── NodeMapper.xml  # SQL 映射文件（新增）
│               └── schema.sql      # 数据库初始化脚本（新增）
└── bootstrap/
    └── OrderCoreApplication        # 启动类（修改：移除 DataSource 自动配置排除）
```

### 2.2 技术栈

- **ORM 框架**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.x
- **连接池**：Druid 1.2.20
- **数据库驱动**：MySQL Connector/J（由 Spring Boot 管理版本）



## 3. 详细设计

### 3.1 依赖管理设计

#### 3.1.1 父 POM 依赖管理

**修改内容**：在 `<dependencyManagement>` 中添加 MyBatis-Plus 和 Druid 版本管理

**新增属性**：
- `mybatis-plus.version`: 3.5.5
- `druid.version`: 1.2.20

**新增依赖管理**：
- `mybatis-plus-boot-starter`
- `mysql-connector-j`（runtime scope）
- `druid-spring-boot-starter`

#### 3.1.2 mysql-impl 模块依赖

**修改内容**：在 `mysql-impl/pom.xml` 中添加依赖（不指定版本）

**新增依赖**：
- `mybatis-plus-boot-starter`
- `mysql-connector-j`（runtime scope）
- `druid-spring-boot-starter`

**依赖关系**：
- 已有：`repository-api`、`common`
- 新增：MyBatis-Plus、MySQL 驱动、Druid

### 3.2 MyBatis-Plus 配置设计

#### 3.2.1 配置类设计（MybatisPlusConfig）

**位置**：`infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/MybatisPlusConfig.java`

**职责**：
- 配置 MyBatis-Plus 拦截器链
- 配置 Mapper 扫描路径
- 注册元数据填充处理器

**拦截器配置**：
1. **分页插件**（PaginationInnerInterceptor）
   - 数据库类型：MySQL
   - 单页最大数量：100
   - 溢出处理：不处理（overflow = false）

2. **乐观锁插件**（OptimisticLockerInnerInterceptor）
   - 支持 `@Version` 注解
   - 自动增加版本号

3. **防全表更新删除插件**（BlockAttackInnerInterceptor）
   - 防止无条件的 UPDATE 和 DELETE

**Mapper 扫描路径**：`com.demo.ordercore.infrastructure.repository.mysql.mapper`

**设计理由**：
- 配置内聚：MybatisPlusConfig 位于 mysql-impl 模块，确保 MySQL 相关配置与实现在一起
- 自动扫描：Spring Boot 主类配置了 `scanBasePackages = "com.demo"`，会自动扫描到此配置类

#### 3.2.2 元数据填充处理器设计（CustomMetaObjectHandler）

**位置**：`infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/CustomMetaObjectHandler.java`

**填充策略**：
- **INSERT 时填充**：createTime、updateTime
- **UPDATE 时填充**：updateTime

**数据来源**：
- createTime、updateTime：使用当前时间

**不自动填充的字段**：
- createBy、updateBy：通过方法参数传递，在 Repository 层直接设置到实体对象

**设计理由**：
- 符合需求要求（需求 2.6）
- createTime、updateTime 使用自动填充，减少重复代码
- createBy、updateBy 通过方法参数传递，更加灵活和明确



### 3.3 数据源配置设计

#### 3.3.1 基础配置（application.yml）

**新增配置项**：

**数据源配置**：
- driver-class-name: `com.mysql.cj.jdbc.Driver`
- type: `com.alibaba.druid.pool.DruidDataSource`

**Druid 连接池配置**：
- initial-size: 5
- min-idle: 5
- max-active: 20
- max-wait: 30000ms

**Druid 监控配置**：
- stat-view-servlet: 启用，路径 `/druid/*`，需要登录（admin/admin123）
- stat filter: 启用，记录慢 SQL（阈值 1000ms）
- wall filter: 启用，SQL 防火墙

**MyBatis-Plus 全局配置**：
- mapper-locations: `classpath*:/mapper/**/*.xml`
- type-aliases-package: `com.demo.ordercore.infrastructure.repository.mysql.po`
- id-type: `ASSIGN_ID`（雪花算法）
- table-prefix: `t_`
- logic-delete-field: `deleted`
- logic-delete-value: 1
- logic-not-delete-value: 0
- map-underscore-to-camel-case: true
- cache-enabled: false

#### 3.3.2 多环境配置

**本地环境（application-local.yml）**：
- 数据库 URL：`jdbc:mysql://localhost:3306/ordercore_local?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai`
- 用户名：root
- 密码：root123
- 日志级别：Mapper 包 DEBUG

**其他环境（dev、test、staging、prod）**：
- 使用环境变量占位符：`${DB_HOST}`、`${DB_PORT}`、`${DB_NAME}`、`${DB_USERNAME}`、`${DB_PASSWORD}`
- 日志级别：
  - dev: DEBUG
  - test: INFO
  - staging: INFO
  - prod: WARN

#### 3.3.3 启动类修改

**修改内容**：移除 `DataSourceAutoConfiguration` 的排除

**原因**：需要启用数据源自动配置以支持 MyBatis-Plus



### 3.4 实体类设计

#### 3.4.1 NodeEntity 领域实体设计

**位置**：`infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/entity/NodeEntity.java`

**设计理由**：
- NodeEntity 是领域实体，表示业务概念
- 作为纯 POJO，不依赖任何持久化框架
- 位于 repository-api 模块，确保业务层不依赖具体实现

**字段设计**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 节点名称，唯一约束 |
| type | String | 节点类型（不限制固定值，支持扩展） |
| description | String | 节点描述 |
| properties | String | JSON 格式扩展属性 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| createBy | String | 创建人 |
| updateBy | String | 更新人 |
| deleted | Integer | 逻辑删除标记（0: 未删除, 1: 已删除） |
| version | Integer | 乐观锁版本号 |

**重要说明**：
- NodeEntity 不包含任何框架特定注解
- 纯 POJO，只包含业务字段和 getter/setter

#### 3.4.2 NodePO 持久化对象设计

**位置**：`infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/po/NodePO.java`

**设计理由**：
- NodePO 是持久化对象，用于数据库表映射
- 包含所有 MyBatis-Plus 注解
- 位于 mysql-impl 模块，体现技术实现细节

**字段设计**：

| 字段 | 类型 | 注解 | 说明 |
|------|------|------|------|
| id | Long | @TableId(ASSIGN_ID) | 主键，雪花算法生成 |
| name | String | - | 节点名称，唯一约束 |
| type | String | - | 节点类型 |
| description | String | - | 节点描述 |
| properties | String | - | JSON 格式扩展属性 |
| createTime | LocalDateTime | @TableField(INSERT) | 创建时间，自动填充 |
| updateTime | LocalDateTime | @TableField(INSERT_UPDATE) | 更新时间，自动填充 |
| createBy | String | - | 创建人，通过方法参数传递 |
| updateBy | String | - | 更新人，通过方法参数传递 |
| deleted | Integer | @TableLogic | 逻辑删除标记 |
| version | Integer | @Version | 乐观锁版本号 |

**注解说明**：
- `@TableName("t_node")`：指定表名
- `@TableId(type = IdType.ASSIGN_ID)`：主键策略为雪花算法
- `@TableField(fill = FieldFill.INSERT)`：插入时自动填充（仅用于 createTime、updateTime）
- `@TableField(fill = FieldFill.INSERT_UPDATE)`：插入和更新时自动填充（仅用于 updateTime）
- `@TableLogic`：逻辑删除标记
- `@Version`：乐观锁版本号

**重要说明**：
- createBy 和 updateBy 字段不使用自动填充
- 这两个字段通过方法参数传递，在 Repository 层设置到 PO 对象

### 3.5 数据库表设计

#### 3.5.1 表结构

**表名**：`t_node`

**字段定义**：
- id: BIGINT, NOT NULL, PRIMARY KEY
- name: VARCHAR(100), NOT NULL, UNIQUE
- type: VARCHAR(50), NOT NULL
- description: VARCHAR(500), NULL
- properties: TEXT, NULL
- create_time: DATETIME, NOT NULL
- update_time: DATETIME, NOT NULL
- create_by: VARCHAR(100), NULL
- update_by: VARCHAR(100), NULL
- deleted: TINYINT, NOT NULL, DEFAULT 0
- version: INT, NOT NULL, DEFAULT 0

**索引设计**：
- PRIMARY KEY: id
- UNIQUE KEY uk_name: name（既保证唯一性，又提升查询性能）
- KEY idx_type: type（提升按类型查询性能）
- KEY idx_deleted: deleted（提升逻辑删除查询性能）

**表属性**：
- ENGINE: InnoDB
- CHARSET: utf8mb4
- COMMENT: '系统节点表'

#### 3.5.2 初始化脚本

**位置**：`infrastructure/repository/mysql-impl/src/main/resources/schema.sql`

**内容**：包含 CREATE TABLE 语句，使用 IF NOT EXISTS 避免重复创建



### 3.6 Mapper 层设计

#### 3.6.1 NodeMapper 接口

**位置**：`infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/mapper/NodeMapper.java`

**继承关系**：继承 `BaseMapper<NodePO>`

**自定义方法**：
1. `selectByName(String name)`: 根据名称查询节点
2. `selectByType(String type)`: 根据类型查询节点列表
3. `selectPageByCondition(Page<?> page, String name, String type)`: 分页查询（支持按名称和类型过滤）

**注解**：`@Mapper`

#### 3.6.2 NodeMapper XML

**位置**：`infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml`

**ResultMap 定义**：
- id: BaseResultMap
- type: com.demo.ordercore.infrastructure.repository.mysql.po.NodePO
- 映射所有字段

**SQL 语句**：
1. **selectByName**：
   - 根据 name 查询
   - 包含 `deleted = 0` 条件

2. **selectByType**：
   - 根据 type 查询列表
   - 包含 `deleted = 0` 条件
   - 按 create_time DESC 排序

3. **selectPageByCondition**：
   - 支持动态条件（name、type）
   - 使用 `<if>` 标签处理可选条件
   - 包含 `deleted = 0` 条件
   - 按 create_time DESC 排序

**重要规范**：
- 所有查询必须包含 `deleted = 0` 条件
- 使用参数化查询（`#{}`）防止 SQL 注入
- 使用 `<if>` 标签处理动态条件

### 3.7 Repository 层设计

#### 3.7.1 NodeRepository 接口

**位置**：`infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/NodeRepository.java`

**方法定义**：
1. `save(NodeEntity entity, String operator)`: 保存节点
2. `update(NodeEntity entity, String operator)`: 更新节点
3. `findById(Long id)`: 根据 ID 查询节点
4. `findByName(String name)`: 根据名称查询节点
5. `findByType(String type)`: 根据类型查询节点列表
6. `findPage(Integer current, Integer size, String name, String type)`: 分页查询节点
7. `deleteById(Long id, String operator)`: 删除节点（逻辑删除）

**设计说明**：
- 所有修改操作（save、update、deleteById）都需要传递 operator 参数
- 查询操作不需要 operator 参数

#### 3.7.2 NodeRepositoryImpl 实现类

**位置**：`infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java`

**实现策略**：

1. **save 方法**：
   - 将 NodeEntity 转换为 NodePO
   - 设置 operator 到 po.createBy 和 po.updateBy
   - 调用 nodeMapper.insert(po)

2. **update 方法**：
   - 将 NodeEntity 转换为 NodePO
   - 设置 operator 到 po.updateBy
   - 调用 nodeMapper.updateById(po)

3. **findById 方法**：
   - 调用 nodeMapper.selectById(id)
   - 将 NodePO 转换为 NodeEntity 返回

4. **findByName 方法**：
   - 调用 nodeMapper.selectByName(name)
   - 将 NodePO 转换为 NodeEntity 返回

5. **findByType 方法**：
   - 调用 nodeMapper.selectByType(type)
   - 将 List<NodePO> 转换为 List<NodeEntity> 返回

6. **findPage 方法**：
   - 创建 Page 对象（current, size）
   - 调用 nodeMapper.selectPageByCondition(page, name, type)
   - 将 IPage<NodePO> 转换为 IPage<NodeEntity> 返回

7. **deleteById 方法**：
   - 先查询 PO：nodeMapper.selectById(id)
   - 设置 operator 到 po.updateBy
   - 调用 nodeMapper.deleteById(id)（逻辑删除）

**转换方法**：

1. **toEntity(NodePO po)**：将 NodePO 转换为 NodeEntity
2. **toPO(NodeEntity entity)**：将 NodeEntity 转换为 NodePO
3. **toEntityList(List<NodePO> poList)**：批量转换 PO 列表
4. **toEntityPage(IPage<NodePO> poPage)**：转换分页结果

**注解**：`@Repository`

**依赖注入**：注入 NodeMapper



## 4. 非功能性设计

### 4.1 性能设计

#### 4.1.1 索引优化

- **唯一索引**：name 字段（既保证唯一性，又提升查询性能）
- **普通索引**：type、deleted 字段（提升查询性能）
- **后续优化**：根据实际查询场景，可考虑添加 (type, deleted) 复合索引

#### 4.1.2 连接池配置

- 初始连接数：5
- 最小空闲连接数：5
- 最大活跃连接数：20
- 连接超时时间：30000ms

#### 4.1.3 分页限制

- 单页最大数量：100 条
- 防止深度分页：建议使用 ID 范围查询代替 offset 分页

### 4.2 安全设计

#### 4.2.1 SQL 注入防护

- 使用 MyBatis 参数化查询（`#{}`）
- 避免使用字符串拼接 SQL
- 启用 Druid SQL 防火墙（wall filter）

#### 4.2.2 防止误操作

- 配置 BlockAttackInnerInterceptor，防止全表更新和删除
- 使用逻辑删除，避免数据丢失
- 使用乐观锁，防止并发更新冲突

#### 4.2.3 敏感信息保护

- 数据库密码使用环境变量配置
- Druid 监控页面需要登录认证

### 4.3 可观测性设计

#### 4.3.1 日志配置

- **开发环境（local、dev）**：DEBUG 级别，输出 SQL 语句
- **测试环境（test）**：INFO 级别
- **预发布环境（staging）**：INFO 级别
- **生产环境（prod）**：WARN 级别

#### 4.3.2 监控配置

- **Druid 监控**：访问路径 `/druid/*`，提供 SQL 执行统计、连接池状态等
- **慢 SQL 监控**：记录执行时间超过 1000ms 的 SQL
- **连接池监控**：监控连接池状态，及时发现连接泄漏



### 4.4 错误处理设计

#### 4.4.1 唯一约束冲突

**场景**：创建或更新节点时，名称重复

**处理策略**：
- 捕获 `DuplicateKeyException`
- 在全局异常处理器中转换为 `BusinessException`
- 返回友好的错误信息："节点名称已存在"

**实现位置**：在现有的 `GlobalExceptionHandler` 中添加处理方法

#### 4.4.2 乐观锁冲突

**场景**：并发更新同一节点

**处理策略**：
- MyBatis-Plus 自动检测 version 字段
- 更新失败时返回影响行数为 0
- 在 Repository 层检查更新结果，抛出 `BusinessException`
- 返回友好的错误信息："数据已被其他用户修改，请刷新后重试"

#### 4.4.3 JSON 格式验证

**场景**：properties 字段不是有效的 JSON 格式

**处理策略**：
- 在 Repository 层保存前验证 JSON 格式
- 使用 Jackson 或 Gson 解析验证
- 格式错误时抛出 `BusinessException`
- 返回友好的错误信息："节点属性格式错误，必须是有效的 JSON"

### 4.5 测试策略

#### 4.5.1 单元测试

**测试类**：`NodeRepositoryImplTest`

**测试场景**：
- 基本 CRUD 操作（save、findById、findByName、findByType、findPage、update、deleteById）
- 唯一约束冲突（创建和更新时的名称重复）
- 乐观锁并发更新（并发更新同一节点）
- 逻辑删除功能（删除后查询不返回）
- 自动填充功能（createTime、updateTime）
- 分页查询功能（总记录数、总页数、当前页数据）

**测试配置**：
- 使用 `@SpringBootTest` 注解
- 使用 `@Transactional` 注解，测试完成后自动回滚
- 使用实际 MySQL 数据库（不使用 H2）

#### 4.5.2 测试数据库配置

**位置**：`infrastructure/repository/mysql-impl/src/test/resources/application-test.yml`

**配置内容**：
- 数据库 URL：`jdbc:mysql://localhost:3306/ordercore_test?...`
- 用户名：root
- 密码：root123



## 5. 技术决策记录（ADR）

### ADR-001：选择 MyBatis-Plus 作为 ORM 框架

**状态**：已接受

**背景**：需要选择 ORM 框架实现数据持久化

**决策**：选择 MyBatis-Plus 作为 ORM 框架

**理由**：
- 基于 MyBatis，学习成本低
- 提供强大的 CRUD 能力，减少样板代码
- 内置分页、乐观锁、逻辑删除等功能
- 社区活跃，文档完善

**后果**：
- 正面：开发效率高，功能强大，易于维护
- 负面：需要遵循 MyBatis-Plus 的最佳实践

---

### ADR-002：条件查询使用 Mapper XML 而非 Wrapper

**状态**：已接受

**背景**：MyBatis-Plus 提供 Wrapper 构造查询条件，但需要统一管理 SQL 语句

**决策**：所有条件查询必须在 Mapper XML 中定义 SQL

**理由**：
- 统一管理所有 SQL 语句，便于维护
- 便于 DBA 和技术负责人审查 SQL
- 便于使用工具分析 SQL 性能
- 提高代码可读性

**后果**：
- 正面：SQL 集中管理，易于维护和优化
- 负面：需要编写更多 XML 代码

---

### ADR-003：Entity/PO 分离架构

**状态**：已接受

**背景**：需要确定实体类的设计模式，是使用单一实体还是 Entity/PO 分离

**决策**：采用 Entity/PO 分离架构

**理由**：
- 原始需求明确要求（3.3 节）
- NodeEntity 作为领域实体，不依赖任何持久化框架
- NodePO 作为持久化对象，包含所有 MyBatis-Plus 注解
- 业务层不依赖具体的持久化实现
- 符合 DDD 分层架构原则
- 易于替换持久化实现（如切换到 JPA、MongoDB）

**后果**：
- 正面：分层清晰，框架无关，易于替换实现
- 负面：需要在 Repository 层进行 Entity/PO 转换，增加少量代码

---

### ADR-004：使用 Druid 作为数据库连接池

**状态**：已接受

**背景**：需要选择数据库连接池，需要监控和防火墙功能

**决策**：使用 Druid 作为数据库连接池

**理由**：
- 阿里巴巴开源，成熟稳定
- 提供强大的监控功能
- 内置 SQL 防火墙，提升安全性
- 支持慢 SQL 监控

**后果**：
- 正面：功能强大，监控完善，安全性高
- 负面：配置相对复杂

---

### ADR-005：createBy 和 updateBy 通过方法参数传递

**状态**：已接受

**背景**：需要记录操作人信息

**决策**：createBy 和 updateBy 通过方法参数传递，在 Repository 层直接设置到实体对象

**理由**：
- 需求明确要求通过方法参数传递（原始需求 2.2 表格说明）
- 更加灵活和明确，调用方可以清楚地知道需要提供操作人信息
- 避免使用 ThreadLocal，减少内存泄漏风险
- 代码更加简洁，易于理解和维护

**后果**：
- 正面：代码简洁，职责清晰，无 ThreadLocal 内存泄漏风险
- 负面：Repository 方法需要额外的 operator 参数



## 6. 风险和应对

### 6.1 技术风险

#### 风险 1：MyBatis-Plus 版本兼容性

**描述**：MyBatis-Plus 3.5.5 与 Spring Boot 3.3.x 可能存在兼容性问题

**影响**：中等

**应对策略**：
- 在集成阶段及时测试
- 查阅官方文档确认兼容性
- 如有问题，考虑降级或升级版本

#### 风险 2：数据库连接配置错误

**描述**：多环境数据库连接信息配置错误，导致无法连接

**影响**：高

**应对策略**：
- 在需求分析阶段与用户确认所有环境的连接信息
- 提供配置模板和示例
- 在启动时进行连接测试

#### 风险 3：SQL 性能问题

**描述**：复杂查询或大数据量查询导致性能问题

**影响**：中等

**应对策略**：
- 合理设计索引
- 使用 Druid 监控慢 SQL
- 限制单页查询数量

### 6.2 业务风险

#### 风险 1：唯一约束冲突

**描述**：并发创建同名节点导致唯一约束冲突

**影响**：低

**应对策略**：
- 在全局异常处理器中捕获并转换为友好错误信息
- 前端进行名称重复校验

#### 风险 2：乐观锁冲突

**描述**：高并发场景下频繁出现乐观锁冲突

**影响**：中等

**应对策略**：
- 返回友好的错误信息，提示用户重试
- 前端实现自动重试机制

### 6.3 运维风险

#### 风险 1：数据库连接池耗尽

**描述**：高并发场景下连接池耗尽，导致请求阻塞

**影响**：高

**应对策略**：
- 合理配置连接池参数
- 使用 Druid 监控连接池状态
- 设置连接超时时间

#### 风险 2：慢 SQL 影响性能

**描述**：慢 SQL 导致系统响应缓慢

**影响**：中等

**应对策略**：
- 使用 Druid 监控慢 SQL
- 定期分析和优化 SQL
- 合理设计索引



## 7. 需求覆盖检查

### 7.1 依赖配置需求（需求 1）

- ✅ 父 POM 声明 MyBatis-Plus、MySQL Connector/J、Druid 版本
- ✅ mysql-impl 模块添加依赖

### 7.2 MyBatis-Plus 配置需求（需求 2）

- ✅ 创建 MybatisPlusConfig 配置类
- ✅ 配置分页插件（MySQL，单页最大 100）
- ✅ 配置乐观锁插件
- ✅ 配置防全表更新删除插件
- ✅ 配置元数据自动填充处理器（createTime、updateTime）
- ✅ 配置 Mapper 扫描路径

### 7.3 数据源配置需求（需求 3、4、25、26）

- ✅ 配置数据源基础属性
- ✅ 配置多环境数据库连接信息（local、dev、test、staging、prod）
- ✅ 配置 Druid 连接池参数
- ✅ 配置 Druid 监控统计功能
- ✅ 配置 Druid SQL 防火墙功能
- ✅ 配置 MyBatis-Plus 全局参数

### 7.4 NodeEntity 实体类需求（需求 5）

- ✅ 创建 NodeEntity 类
- ✅ 定义所有字段（id、name、type、description、properties、createTime、updateTime、createBy、updateBy、deleted、version）
- ✅ 使用正确的注解（@TableName、@TableId、@TableField、@TableLogic、@Version）

### 7.5 NodeMapper 需求（需求 6、7）

- ✅ 创建 NodeMapper 接口，继承 BaseMapper
- ✅ 定义自定义方法（selectByName、selectByType、selectPageByCondition）
- ✅ 创建 NodeMapper.xml，实现 SQL 映射
- ✅ 所有 SQL 包含 deleted = 0 条件

### 7.6 数据库表需求（需求 8、20）

- ✅ 创建 t_node 表
- ✅ 定义所有字段和约束
- ✅ 创建索引（主键、唯一索引、普通索引）
- ✅ 提供初始化脚本（schema.sql）

### 7.7 NodeRepository 需求（需求 9、10）

- ✅ 创建 NodeRepository 接口
- ✅ 定义所有方法（save、update、findById、findByName、findByType、findPage、deleteById）
- ✅ 创建 NodeRepositoryImpl 实现类
- ✅ 实现所有方法，正确调用 Mapper

### 7.8 功能验证需求（需求 11-14、18）

- ✅ 创建节点功能（自动生成 ID、自动填充时间、设置操作人）
- ✅ 查询节点功能（根据 ID、名称、类型、分页查询）
- ✅ 更新节点功能（自动更新时间、设置操作人、乐观锁）
- ✅ 删除节点功能（逻辑删除、设置操作人）
- ✅ 唯一性约束验证（名称重复抛出异常）

### 7.9 非功能性需求（需求 15-17、22-24）

- ✅ 性能要求（索引优化、连接池配置、分页限制）
- ✅ 安全要求（SQL 注入防护、防止误操作）
- ✅ 可维护性要求（SQL 集中管理、遵循最佳实践）
- ✅ JSON 格式验证
- ✅ 日志配置（多环境日志级别）
- ✅ 测试数据库配置

### 7.10 单元测试需求（需求 19）

- ✅ 创建 NodeRepositoryImplTest 测试类
- ✅ 测试所有 CRUD 操作
- ✅ 测试唯一约束冲突
- ✅ 测试乐观锁并发更新
- ✅ 使用 @SpringBootTest 和 @Transactional





## 8. 过度设计检查

### 8.1 已避免的过度设计

- ✅ **不使用 ThreadLocal 管理操作人**：根据 ADR-005，采用更简洁的方法参数传递方案
- ✅ **不实现复杂的缓存机制**：当前阶段不需要，后续根据性能需求再考虑
- ✅ **不实现多数据源**：当前只需要单数据源，不提前设计读写分离或分库分表
- ✅ **不实现自定义 ID 生成器**：使用 MyBatis-Plus 内置的雪花算法即可
- ✅ **不实现复杂的审计日志**：只记录 createBy、updateBy，不实现完整的审计日志系统

### 8.2 必要的设计

- ✅ **分页插件**：需求明确要求分页查询
- ✅ **乐观锁插件**：需求明确要求防止并发冲突
- ✅ **逻辑删除**：需求明确要求逻辑删除
- ✅ **防全表更新删除插件**：安全要求，防止误操作
- ✅ **Druid 监控**：可观测性要求，便于性能分析
- ✅ **多环境配置**：需求明确要求支持 5 个环境

### 8.3 可选的设计（未实现）

- ❌ **二级缓存**：当前不需要，后续根据性能需求再考虑
- ❌ **读写分离**：当前不需要，后续根据性能需求再考虑
- ❌ **分库分表**：当前不需要，后续根据数据量再考虑
- ❌ **完整的审计日志系统**：当前不需要，只记录基本的操作人信息

## 9. 设计一致性检查

### 9.1 模块依赖一致性

- ✅ repository-api 模块：不依赖任何持久化框架，定义 Entity 和 Repository 接口
- ✅ mysql-impl 模块：依赖 repository-api、common、MyBatis-Plus，实现仓储接口
- ✅ bootstrap 模块：依赖 mysql-impl，启动应用

### 9.2 数据流一致性

- ✅ 查询流程：Controller → Service → Repository → Mapper → Database
- ✅ 修改流程：Controller → Service → Repository（设置 operator）→ Mapper → Database
- ✅ 自动填充：MetaObjectHandler 自动填充 createTime、updateTime
- ✅ 手动设置：Repository 层手动设置 createBy、updateBy

### 9.3 配置一致性

- ✅ 表名前缀：配置为 `t_`，PO 类使用 `@TableName("t_node")`
- ✅ 主键策略：配置为 `ASSIGN_ID`，PO 类使用 `@TableId(type = IdType.ASSIGN_ID)`
- ✅ 逻辑删除：配置 deleted 字段，PO 类使用 `@TableLogic`
- ✅ Mapper 扫描路径：配置为 `com.demo.ordercore.infrastructure.repository.mysql.mapper`
- ✅ PO 类包路径：配置为 `com.demo.ordercore.infrastructure.repository.mysql.po`

### 9.4 命名一致性

- ✅ 领域实体：NodeEntity（repository-api）
- ✅ 持久化对象：NodePO（mysql-impl）
- ✅ 表名：t_node
- ✅ Mapper 接口：NodeMapper
- ✅ Mapper XML：NodeMapper.xml
- ✅ Repository 接口：NodeRepository
- ✅ Repository 实现：NodeRepositoryImpl



## 10. 设计可实施性检查

### 10.1 技术可行性

- ✅ MyBatis-Plus 3.5.5 与 Spring Boot 3.3.5 兼容（已验证）
- ✅ Druid 1.2.20 与 Spring Boot 3.3.5 兼容（已验证）
- ✅ MySQL Connector/J 与 MySQL 8.x 兼容（已验证）
- ✅ 所有技术栈都是成熟稳定的开源项目

### 10.2 实施顺序可行性

1. ✅ 依赖配置：可独立完成，不依赖其他步骤
2. ✅ 实体类定义：可独立完成，不依赖其他步骤
3. ✅ 数据库表创建：可独立完成，不依赖其他步骤
4. ✅ MyBatis-Plus 配置：依赖步骤 1，可行
5. ✅ 数据源配置：依赖步骤 1，可行
6. ✅ Mapper 层实现：依赖步骤 2、3、4、5，可行
7. ✅ Repository 层实现：依赖步骤 6，可行
8. ✅ 单元测试：依赖步骤 7，可行
9. ✅ 集成验证：依赖所有步骤，可行

### 10.3 验证可行性

- ✅ 编译验证：每个步骤完成后可执行 `mvn clean compile`
- ✅ 启动验证：配置完成后可启动应用，检查日志
- ✅ 功能验证：可通过单元测试验证 CRUD 功能
- ✅ 性能验证：可通过 Druid 监控查看 SQL 执行情况

### 10.4 风险可控性

- ✅ 技术风险：已识别并制定应对策略
- ✅ 业务风险：已识别并制定应对策略
- ✅ 运维风险：已识别并制定应对策略
- ✅ 所有风险都有明确的应对措施

## 11. 附录

### 11.1 包路径汇总

| 组件 | 包路径 | 模块 |
|------|--------|------|
| NodeEntity | com.demo.ordercore.infrastructure.repository.entity | repository-api |
| NodePO | com.demo.ordercore.infrastructure.repository.mysql.po | mysql-impl |
| NodeRepository | com.demo.ordercore.infrastructure.repository.api | repository-api |
| NodeMapper | com.demo.ordercore.infrastructure.repository.mysql.mapper | mysql-impl |
| NodeRepositoryImpl | com.demo.ordercore.infrastructure.repository.mysql.impl | mysql-impl |
| MybatisPlusConfig | com.demo.ordercore.infrastructure.repository.mysql.config | mysql-impl |
| CustomMetaObjectHandler | com.demo.ordercore.infrastructure.repository.mysql.config | mysql-impl |

### 11.2 配置文件汇总

| 配置文件 | 位置 | 说明 |
|---------|------|------|
| application.yml | bootstrap/src/main/resources | 基础配置 |
| application-local.yml | bootstrap/src/main/resources | 本地环境配置 |
| application-dev.yml | bootstrap/src/main/resources | 开发环境配置 |
| application-test.yml | bootstrap/src/main/resources | 测试环境配置 |
| application-staging.yml | bootstrap/src/main/resources | 预发布环境配置 |
| application-prod.yml | bootstrap/src/main/resources | 生产环境配置 |
| NodeMapper.xml | mysql-impl/src/main/resources/mapper | Mapper XML |
| schema.sql | mysql-impl/src/main/resources | 数据库初始化脚本 |
| application-test.yml | mysql-impl/src/test/resources | 测试配置 |

### 11.3 依赖版本汇总

| 依赖 | 版本 | 说明 |
|------|------|------|
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Druid | 1.2.20 | 数据库连接池 |
| MySQL Connector/J | 由 Spring Boot 管理 | MySQL 驱动 |
| Spring Boot | 3.3.5 | 应用框架 |
| Java | 21 | JDK 版本 |

### 11.4 参考资料

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Druid 官方文档](https://github.com/alibaba/druid/wiki)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [项目 MyBatis-Plus 最佳实践](.kiro/steering/06-mybatis-plus-best-practice.md)
- [项目架构设计文档](project-architecture-design.md)

---

**文档版本**：v2.0  
**创建日期**：2024-11-10  
**最后更新**：2024-11-10  
**作者**：OrderCore Team

