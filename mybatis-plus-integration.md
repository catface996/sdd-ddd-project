# MyBatis-Plus 集成与 NodeEntity 实现原始需求

## 一、项目背景

当前项目是一个基于 DDD 分层架构的多模块 Maven 工程，采用 Spring Boot 3.3.x 和 Spring Cloud 2024.0.x 技术栈。项目已经完成了基础架构搭建，包括：
- 多模块结构（common、bootstrap、interface、application、domain、infrastructure）
- 日志与追踪体系（Logback + JSON + Micrometer Tracing）
- 异常处理机制（统一异常体系和全局异常处理器）
- 多环境配置（local、dev、test、staging、prod）

现在需要集成 MyBatis-Plus 作为 ORM 框架，并实现第一个业务实体 NodeEntity 的持久化功能。

## 二、业务需求

### 2.1 NodeEntity 业务背景

NodeEntity 是系统中用于建立和管理系统节点关系的核心实体。在企业级应用中，通常需要管理多个系统节点（如数据库、业务应用、API 接口、报表系统等），并记录它们之间的依赖关系和属性信息。

**业务场景示例**：
- 记录系统中所有的数据库节点（MySQL、PostgreSQL、Oracle 等）
- 记录业务应用节点（订单服务、用户服务、支付服务等）
- 记录 API 接口节点（REST API、GraphQL API 等）
- 记录报表系统节点（BI 报表、数据分析平台等）
- 记录节点之间的依赖关系（如订单服务依赖用户服务和支付服务）

### 2.2 NodeEntity 数据模型

NodeEntity 需要包含以下属性：

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | Long | 主键 ID | 自动生成（雪花算法 ASSIGN_ID） |
| name | String | 节点名称 | 必填，最大长度 100 字符，**唯一约束** |
| type | String | 节点类型 | 必填，可选值：DATABASE、APPLICATION、API、REPORT、OTHER（固定 5 种类型） |
| description | String | 节点描述 | 可选，最大长度 500 字符 |
| properties | String | 节点属性 | 可选，JSON 格式存储，用于记录节点的扩展属性（如主机地址、端口号等） |
| createTime | LocalDateTime | 创建时间 | 系统自动填充（INSERT 时） |
| updateTime | LocalDateTime | 更新时间 | 系统自动填充（INSERT 和 UPDATE 时） |
| createBy | String | 创建人 | 系统自动填充（INSERT 时） |
| updateBy | String | 更新人 | 系统自动填充（INSERT 和 UPDATE 时） |
| deleted | Integer | 逻辑删除标记 | 0 表示未删除，1 表示已删除，默认值 0 |
| version | Integer | 版本号 | 用于乐观锁并发控制，防止数据冲突，默认值 0 |

**数据库表要求**：
- 表名：t_node
- 字符集：UTF8MB4
- 存储引擎：InnoDB
- 索引设计：
  - **name 字段**：唯一索引（既保证唯一性，又提升查询性能）
  - **type 字段**：普通索引（提升查询性能）
  - **deleted 字段**：普通索引（提升查询性能）

### 2.3 功能需求

需要实现 NodeEntity 的基本 CRUD 功能：

1. **创建节点**：新增一个系统节点记录
2. **查询节点**：
   - 根据 ID 查询单个节点
   - 根据名称查询节点
   - 根据类型查询节点列表
   - 分页查询节点列表
3. **更新节点**：更新节点的基本信息和属性
4. **删除节点**：逻辑删除节点（不物理删除）

## 三、技术需求

### 3.1 ORM 框架集成

系统需要集成 MyBatis-Plus 作为 ORM 框架，用于实现数据持久化功能。集成应遵循项目的 MyBatis-Plus 最佳实践指南（`.kiro/steering/06-mybatis-plus-best-practice.md`）。

**集成目标**：
- 在 infrastructure/repository/mysql-impl 模块中集成 MyBatis-Plus
- 配置必要的插件和功能：
  - **分页插件**：支持 MySQL 数据库类型，单页最大数量限制
  - **乐观锁插件**：支持版本号并发控制
  - **防全表更新删除插件**：防止误操作
  - **元数据自动填充处理器**：自动填充创建时间、更新时间、创建人、更新人
- 支持多环境数据源配置（local、dev、test、staging、prod）
- 遵循统一的数据操作规范
- 配置 Mapper 扫描路径和 XML 文件位置

### 3.2 数据操作规范

系统应遵循以下数据操作规范：

**简单操作**：
- 插入、更新、根据主键查询等简单操作可以使用 ORM 框架提供的 API

**复杂操作**：
- 条件查询、条件更新、条件删除、复杂查询等操作必须通过 SQL 文件管理
- 所有 SQL 语句应集中管理，便于维护和性能优化

### 3.3 多环境支持

数据源配置需要支持以下环境：

| 环境 | 说明 |
|------|------|
| local | 本地开发环境 |
| dev | 开发环境 |
| test | 测试环境 |
| staging | 预发布环境 |
| prod | 生产环境 |

每个环境应有独立的数据库连接配置和连接池配置。

### 3.4 Repository 层实现

需要实现 NodeEntity 的仓储层，提供以下数据访问能力：

**模块划分**：
- **repository-api 模块**：定义 NodeRepository 接口（仓储接口）
- **mysql-impl 模块**：
  - 实现 NodeMapper 接口（继承 BaseMapper<NodeEntity>）
  - 实现 NodeRepositoryImpl 类（实现 NodeRepository 接口）
  - 创建 NodeMapper.xml（SQL 语句管理）

**基本操作**（使用 MyBatis-Plus API）：
- 保存节点（save 方法，调用 NodeMapper.insert）
- 更新节点（update 方法，调用 NodeMapper.updateById）
- 根据 ID 查询节点（findById 方法，调用 NodeMapper.selectById）
- 逻辑删除节点（deleteById 方法，调用 NodeMapper.deleteById）

**查询操作**（在 Mapper XML 中定义 SQL）：
- 根据名称查询节点（selectByName 方法）
- 根据类型查询节点列表（selectByType 方法）
- 分页查询节点列表（selectPageByCondition 方法，支持按名称和类型过滤）

**重要规范**：
- 所有条件查询必须在 Mapper XML 中定义 SQL 语句
- 所有 SQL 语句必须包含 `deleted = 0` 条件，只查询未删除的数据
- 不使用 Wrapper 构造查询条件

## 四、非功能性需求

### 4.1 性能要求

- 单表查询响应时间 < 100ms
- 分页查询响应时间 < 200ms
- 支持并发插入和更新（通过乐观锁）

### 4.2 安全要求

- 防止 SQL 注入（使用参数化查询）
- 防止全表更新和删除（使用 BlockAttackInnerInterceptor）
- 敏感字段不输出到日志（如密码字段）

### 4.3 可维护性要求

- 所有 SQL 语句集中在 Mapper XML 中管理
- 代码符合 MyBatis-Plus 最佳实践
- 添加必要的注释和文档
- 遵循项目的编码规范

### 4.4 可测试性要求

- 为 Repository 实现类编写单元测试
- 使用实际数据库进行测试（不使用 H2 内存数据库）
- 测试覆盖以下场景：
  - 基本的 CRUD 操作（save、findById、findByName、findByType、findPage、update、deleteById）
  - 唯一约束冲突场景（创建和更新时的名称重复）
  - 乐观锁并发更新场景（并发更新同一节点）
  - 逻辑删除功能（删除后查询不返回）
  - 自动填充功能（创建时间、更新时间、创建人、更新人）

## 五、验收标准

### 5.1 集成验证

- [ ] 项目可以成功编译（`mvn clean compile`）
- [ ] 项目可以成功打包（`mvn clean package`）
- [ ] 启动应用不报错，日志中显示 MyBatis-Plus 初始化成功
- [ ] 数据库连接成功，连接池正常工作

### 5.2 配置验证

- [ ] 父 POM 的 `<dependencyManagement>` 中包含 MyBatis-Plus 版本管理
- [ ] mysql-impl 模块的 POM 中正确声明了依赖（不指定版本号）
- [ ] Mapper XML 文件可以正确打包到 JAR 中
- [ ] 多环境配置文件存在且格式正确

### 5.3 功能验证

- [ ] 可以成功创建节点记录
  - [ ] ID 自动生成（雪花算法）
  - [ ] createTime、updateTime 自动填充为当前时间
  - [ ] createBy、updateBy 自动填充为当前用户
  - [ ] deleted 默认值为 0
  - [ ] version 默认值为 0
- [ ] 可以根据 ID 查询节点
- [ ] 可以根据名称查询节点
- [ ] 可以根据类型查询节点列表
- [ ] 可以分页查询节点列表（支持按名称和类型过滤）
  - [ ] 返回总记录数、总页数、当前页数据
- [ ] 可以更新节点信息
  - [ ] updateTime 自动更新为当前时间
  - [ ] updateBy 自动更新为当前用户
  - [ ] version 自动增加
- [ ] 可以逻辑删除节点
  - [ ] 设置 deleted = 1（不物理删除）
  - [ ] 查询时不返回已删除的节点
- [ ] 唯一性约束验证
  - [ ] 创建名称重复的节点时抛出异常
  - [ ] 更新节点名称为已存在的名称时抛出异常
  - [ ] 异常处理器返回友好的错误信息
- [ ] 乐观锁功能正常工作
  - [ ] 并发更新同一节点时，后更新的操作失败

### 5.4 代码质量验证

- [ ] 代码符合项目的 MyBatis-Plus 最佳实践规范
- [ ] 所有条件查询都通过 SQL 文件管理
- [ ] 代码有适当的注释和文档
- [ ] 遵循项目的命名规范和编码规范

## 六、已确认的设计决策

以下问题已经与用户确认：

1. **数据库连接信息**：
   - 数据库名：tiang
   - 用户名：tiang_user
   - 密码：tiang123
   - 主机：localhost
   - 端口：3306
   - 不需要配置多数据源

2. **实体类位置**：
   - NodeEntity 放在 domain-api 模块（作为领域实体）
   - 不需要区分领域实体和数据库实体

3. **节点类型**：
   - 节点类型固定为 5 种：DATABASE、APPLICATION、API、REPORT、OTHER
   - 不支持自定义节点类型扩展

4. **属性字段**：
   - properties 字段存储 JSON 格式的扩展属性
   - 无特定的结构要求
   - 不需要验证 JSON 格式

5. **测试要求**：
   - **需要编写单元测试**，覆盖所有 CRUD 操作
   - 测试场景包括：
     - 基本的增删改查功能
     - 唯一约束冲突场景
     - 乐观锁并发更新场景
   - 使用实际数据库进行测试（不使用 H2 内存数据库）

6. **初始化数据**：
   - **需要提供数据库初始化脚本**（schema.sql）
   - 脚本包含：
     - 创建 t_node 表的 DDL 语句
     - 创建唯一索引（name 字段）
     - 创建普通索引（type、deleted 字段）
     - 添加 IF NOT EXISTS 判断，避免重复创建
     - 添加清晰的注释说明

## 七、实现约束

1. **必须遵循项目架构设计**：严格按照 DDD 分层架构实现
2. **必须遵循 MyBatis-Plus 最佳实践**：参考 `.kiro/steering/06-mybatis-plus-best-practice.md`
3. **必须保证项目可编译**：每完成一个任务，项目都必须可以成功编译
4. **必须使用多环境配置**：支持 local、dev、test、staging、prod 五个环境
5. **必须使用统一的异常处理**：集成项目已有的异常处理机制
6. **必须输出结构化日志**：日志中包含 traceId 和 spanId

## 八、参考资料

- 项目架构设计文档：`project-architecture-design.md`
- MyBatis-Plus 最佳实践：`.kiro/steering/06-mybatis-plus-best-practice.md`
- MyBatis-Plus 官方文档：https://baomidou.com/
- Spring Boot 官方文档：https://spring.io/projects/spring-boot
