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
| createBy | String | 创建人 | 通过方法参数传递，由调用方提供 |
| updateBy | String | 更新人 | 通过方法参数传递，由调用方提供 |
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
  - **分页插件**：支持 MySQL 数据库类型，单页最大数量限制为 100 条
  - **乐观锁插件**：支持版本号并发控制
  - **防全表更新删除插件**：防止误操作
  - **元数据自动填充处理器**：自动填充创建时间、更新时间；创建人和更新人通过方法参数传递
- 支持多环境数据源配置（local、dev、test、staging、prod）
- 遵循统一的数据操作规范
- 配置 Mapper 扫描路径和 XML 文件位置

**包路径设计**：
- **Mapper 接口扫描路径**：`com.catface.infrastructure.repository.mysql.mapper`
  - 位置：`infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/mapper/`
- **实体类包路径**：`com.catface.domain.entity`
  - 位置：`domain/domain-api/src/main/java/com/catface/domain/entity/`
- **Mapper XML 文件位置**：`classpath*:/mapper/**/*.xml`
  - 位置：`infrastructure/repository/mysql-impl/src/main/resources/mapper/`
- **Repository 接口包路径**：`com.catface.infrastructure.repository.api`
  - 位置：`infrastructure/repository/repository-api/src/main/java/com/catface/infrastructure/repository/api/`
- **Repository 实现类包路径**：`com.catface.infrastructure.repository.mysql.impl`
  - 位置：`infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/impl/`

### 3.2 通用分页结果类设计

#### 3.2.1 PageResult 位置决策

**决策**：将 `PageResult` 类放在 `common/dto/` 目录下，作为通用的分页结果封装类。

**决策理由**：

1. **分页是通用概念**：
   - 分页的结构（当前页、每页大小、总记录数、总页数、数据列表）在所有层都是一致的
   - 不需要在各层之间进行"翻译"，只是泛型参数不同
   - 与 `common/dto/Result<T>` 的设计理念一致，都是通用的"容器"类

2. **避免过度设计**：
   - 如果每层都定义自己的 Page 模型，会导致大量重复代码
   - 需要在各层之间频繁进行无意义的转换（结构完全相同，只是泛型参数不同）
   - 维护成本高，修改分页逻辑需要同步修改多个类

3. **符合实用主义原则**：
   - 业界主流做法：Spring Data 的 `Page<T>`、MyBatis-Plus 的 `IPage<T>` 都是跨层共享的
   - 大多数项目都采用一个通用的 `PageResult<T>`，通过泛型参数适配不同层的数据类型
   - 简单实用，减少不必要的复杂度

4. **与项目现有设计一致**：
   - 项目中的 `common/dto/Result<T>` 也是泛型类，可以是 `Result<Entity>`、`Result<DTO>`、`Result<VO>`
   - `PageResult<T>` 采用相同的设计模式，保持一致性

5. **便于使用**：
   - 各层使用不同的泛型参数：`PageResult<NodeEntity>`、`PageResult<NodeDTO>`、`PageResult<NodeVO>`
   - 提供 `convert()` 方法支持便捷的类型转换
   - 所有模块都可以直接使用，无需重复定义

**文件位置**：
- `common/src/main/java/com/catface/common/dto/PageResult.java`

**使用示例**：
```java
// Repository 层返回
PageResult<NodeEntity> entityPage = repository.findPage(...);

// Application 层转换
PageResult<NodeDTO> dtoPage = entityPage.convert(this::toDTO);

// HTTP 层转换
PageResult<NodeVO> voPage = dtoPage.convert(this::toVO);
```

**注意事项**：
- ⚠️ PageResult 只是"容器"，不包含业务逻辑
- ⚠️ 各层的数据对象（Entity、DTO、VO）仍然需要分层定义
- ⚠️ 只有当模型结构本身不同时，才需要分层定义；结构相同只是泛型参数不同时，共享通用类

### 3.3 Repository 模块 Package 结构规范

#### 3.3.1 repository-api 模块（纯 Java API 层）

**模块职责**：定义仓储接口和领域实体，不依赖任何基础设施框架

**Package 结构**：

```
com.catface.infrastructure.repository
├── api/                          # 仓储接口包
│   ├── NodeRepository.java       # 节点仓储接口
│   └── package-info.java          # 包说明文档
│
└── entity/                        # 领域实体包
    └── NodeEntity.java            # 节点实体（纯 POJO，无框架注解）
```

**文件位置**：
- `infrastructure/repository/repository-api/src/main/java/com/catface/infrastructure/repository/api/`
- `infrastructure/repository/repository-api/src/main/java/com/catface/infrastructure/repository/entity/`

**关键原则**：
- ✅ 只包含接口定义和纯 Java 实体
- ✅ 不依赖任何持久化框架（MyBatis-Plus、JPA 等）
- ✅ 实体类不包含任何框架特定注解
- ✅ 分页结果使用 `common/dto/PageResult`，不在此模块定义

**核心类说明**：

| 类名 | 职责 | 框架依赖 |
|------|------|---------|
| **NodeEntity** | 领域实体，表示业务概念，纯 POJO | 无 |
| **NodeRepository** | 仓储接口，定义数据访问契约 | 无 |

#### 3.3.2 mysql-impl 模块（MyBatis-Plus 实现层）

**模块职责**：基于 MyBatis-Plus 实现仓储接口，包含所有框架特定的实现细节

**Package 结构**：

```
com.catface.infrastructure.repository
└── mysql/                         # MySQL 实现包（体现技术选型）
    ├── config/                    # MySQL 配置包
    │   └── MybatisPlusConfig.java # MyBatis-Plus 配置类
    │
    ├── impl/                      # 仓储实现类
    │   └── NodeRepositoryImpl.java    # 节点仓储实现
    │
    ├── mapper/                    # MyBatis Mapper 接口
    │   └── NodeMapper.java        # 节点 Mapper
    │
    ├── po/                        # 持久化对象（Persistent Object）
    │   └── NodePO.java            # 节点 PO（包含 MyBatis-Plus 注解）
    │
    └── package-info.java          # 包说明文档
```

**文件位置**：
- `infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/config/`
- `infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/impl/`
- `infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/mapper/`
- `infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/po/`

**资源文件位置**：
```
src/main/resources
└── mapper/                        # MyBatis XML 映射文件
    └── NodeMapper.xml             # 节点 Mapper XML
```

**关键原则**：
- ✅ 使用 `mysql` 作为 package 名，明确表示这是 MySQL 的实现
- ✅ **配置内聚**：MybatisPlusConfig 配置类位于 mysql-impl 模块，确保 MySQL 相关配置与实现在一起
- ✅ 实现 repository-api 中定义的接口
- ✅ PO 类包含所有 MyBatis-Plus 注解（@TableName、@TableId、@TableField 等）
- ✅ Mapper 接口继承 BaseMapper<NodePO>
- ✅ RepositoryImpl 负责 Entity 和 PO 之间的转换
- ✅ 所有框架特定的代码都在此模块中

**核心类说明**：

| 类名 | Package | 职责 | 框架依赖 |
|------|---------|------|---------|
| **MybatisPlusConfig** | `...repository.mysql.config` | MyBatis-Plus 配置，插件注册 | MyBatis-Plus |
| **NodePO** | `...repository.mysql.po` | 持久化对象，数据库表映射 | MyBatis-Plus |
| **NodeMapper** | `...repository.mysql.mapper` | MyBatis Mapper 接口 | MyBatis-Plus |
| **NodeRepositoryImpl** | `...repository.mysql.impl` | 仓储实现，Entity/PO 转换 | MyBatis-Plus |

#### 3.3.3 数据流转示意

```
业务层 (Application/Domain)
    ↓ 使用
NodeEntity + NodeRepository (repository-api)
    ↓ 实现
NodeRepositoryImpl (mysql-impl: mysql.impl)
    ↓ 转换
NodePO ←→ NodeEntity
    ↓ 映射
NodeMapper (mysql-impl: mysql.mapper)
    ↓ 操作
数据库表 (t_node)
```

#### 3.3.4 Package 命名规范说明

**为什么使用 `mysql` 而不是 `sql`？**

1. **明确技术选型**：`mysql` 清楚表明这是 MySQL 数据库的实现
2. **便于扩展**：未来如果需要支持其他数据库（PostgreSQL、Oracle），可以创建对应的 package：
   - `com.catface.infrastructure.repository.postgresql.*`
   - `com.catface.infrastructure.repository.oracle.*`
3. **符合模块命名**：模块名是 `mysql-impl`，package 名也应该体现 MySQL
4. **避免歧义**：`sql` 太泛化，不能明确表示具体的数据库实现

#### 3.3.5 架构优势

1. **分层清晰**：API 层和实现层完全解耦
2. **框架无关**：业务层不依赖任何持久化框架
3. **易于替换**：可以轻松切换到其他持久化实现（JPA、MongoDB 等）
4. **符合 DDD**：Entity 是纯领域对象，PO 是技术实现细节
5. **职责单一**：每个类都有明确的职责边界
6. **技术明确**：package 名明确表示技术选型，便于理解和维护

### 3.4 MyBatis-Plus 配置路径验证

**重要**：必须确保 MyBatis-Plus 的扫描配置与实际的 package 结构完全一致。

#### 3.4.1 Mapper 扫描配置

**配置位置**：`infrastructure/repository/mysql-impl/src/main/java/com/catface/infrastructure/repository/mysql/config/MybatisPlusConfig.java`

**配置原则**：
- ✅ **配置内聚**：MybatisPlusConfig 位于 mysql-impl 模块，确保 MySQL 相关配置与实现在一起
- ✅ **自动扫描**：Spring Boot 主类配置了 `scanBasePackages = "com.catface"`，会自动扫描到此配置类
- ✅ **Mapper 扫描路径**：`@MapperScan` 注解必须指向 `com.catface.infrastructure.repository.mysql.mapper`
- ✅ **插件配置**：配置分页插件（单页最大 100 条）、乐观锁插件、防全表更新删除插件

**验证要点**：
- Mapper 扫描路径与 Mapper 接口的实际 package 路径一致
- 配置类位于 mysql-impl 模块的 config 包下
- 插件按正确顺序注册（分页插件必须在第一位）

#### 3.4.2 持久化对象（PO）类型别名配置

**配置位置**：`bootstrap/src/main/resources/application.yml`

**配置原则**：
- ✅ `type-aliases-package` 必须指向 PO 类的包路径：`com.catface.infrastructure.repository.mysql.po`
- ⚠️ **注意**：配置的是 PO 类路径，不是 Entity 类路径
- ⚠️ **原因**：MyBatis-Plus 直接操作 PO（包含框架注解），而不是 Entity（纯 POJO）

**验证要点**：
- 类型别名路径与 PO 类的实际 package 路径一致
- PO 类位于 mysql-impl 模块的 po 包下

#### 3.4.3 Mapper XML 文件位置配置

**配置位置**：`bootstrap/src/main/resources/application.yml`

**配置原则**：
- ✅ `mapper-locations` 配置为 `classpath*:/mapper/**/*.xml`
- ✅ XML 文件的 namespace 必须与 Mapper 接口的全限定名一致
- ✅ resultMap 的 type 属性必须与 PO 类的全限定名一致

**验证要点**：
- Mapper XML 文件位于 mysql-impl 模块的 resources/mapper 目录下
- XML namespace 与 Mapper 接口完全匹配
- resultMap type 与 PO 类完全匹配

#### 3.4.4 配置一致性检查清单

在集成 MyBatis-Plus 时，必须检查以下配置的一致性：

- [ ] **Mapper 扫描路径**：`@MapperScan` 注解的值与 Mapper 接口的实际 package 一致
- [ ] **PO 类型别名路径**：`type-aliases-package` 的值与 PO 类的实际 package 一致
- [ ] **Mapper XML namespace**：XML 文件的 namespace 与 Mapper 接口的全限定名一致
- [ ] **Mapper XML resultMap type**：resultMap 的 type 属性与 PO 类的全限定名一致
- [ ] **项目编译成功**：执行 `mvn clean compile` 无错误
- [ ] **配置文件格式正确**：YAML 文件缩进和格式正确

#### 3.4.5 常见配置错误

**错误类型 1：Package 路径不一致**
- Mapper 扫描路径使用了错误的 package 名（如 sql.mapper 而不是 mysql.mapper）
- type-aliases-package 配置了 Entity 路径而不是 PO 路径
- Mapper XML namespace 与 Mapper 接口不匹配

**错误类型 2：配置位置错误**
- MybatisPlusConfig 放在了 bootstrap 模块而不是 mysql-impl 模块
- Mapper XML 文件放在了错误的资源目录

**错误类型 3：插件配置错误**
- 分页插件没有放在第一位
- 缺少必要的插件配置（乐观锁、防全表更新删除）
- 分页插件的数据库类型配置错误

### 3.5 数据操作规范

系统应遵循以下数据操作规范：

**简单操作**：
- 插入、更新、根据主键查询等简单操作可以使用 ORM 框架提供的 API

**复杂操作**：
- 条件查询、条件更新、条件删除、复杂查询等操作必须通过 SQL 文件管理
- 所有 SQL 语句应集中管理，便于维护和性能优化

### 3.6 多环境支持

数据源配置需要支持以下环境：

| 环境 | 说明 |
|------|------|
| local | 本地开发环境 |
| dev | 开发环境 |
| test | 测试环境 |
| staging | 预发布环境 |
| prod | 生产环境 |

每个环境应有独立的数据库连接配置和连接池配置。

**数据库连接信息**：
- 在需求分析阶段，需要用户提供各环境的数据库连接信息，包括：
  - 数据库名（database name）
  - 用户名（username）
  - 密码（password）
  - 主机地址（host）
  - 端口号（port）
  - 其他连接参数（如字符集、时区等）

### 3.7 Repository 层实现

需要实现 NodeEntity 的仓储层，提供以下数据访问能力：

**模块划分**：
- **repository-api 模块**：定义 NodeRepository 接口（仓储接口）
- **mysql-impl 模块**：
  - 实现 NodeMapper 接口（继承 BaseMapper<NodePO>）
  - 实现 NodeRepositoryImpl 类（实现 NodeRepository 接口）
  - 创建 NodeMapper.xml（SQL 语句管理）

**基本操作**（使用 MyBatis-Plus API）：
- **保存节点**：接收 NodeEntity 和 operator 参数，转换为 NodePO 后调用 Mapper.insert
- **更新节点**：接收 NodeEntity 和 operator 参数，转换为 NodePO 后调用 Mapper.updateById
- **根据 ID 查询**：调用 Mapper.selectById，将 NodePO 转换为 NodeEntity 返回
- **逻辑删除**：接收 id 和 operator 参数，调用 Mapper.deleteById

**操作人参数说明**：
- `operator` 参数表示操作人，用于填充 createBy 和 updateBy 字段
- Repository 层方法接收 operator 参数，在调用 Mapper 前设置到 PO 对象中
- 创建时设置 createBy 和 updateBy，更新时只设置 updateBy

**查询操作**（在 Mapper XML 中定义 SQL）：
- **根据名称查询**：selectByName 方法，返回单个节点
- **根据类型查询**：selectByType 方法，返回节点列表
- **分页查询**：selectPageByCondition 方法，支持按名称和类型过滤

**数据操作规范**：
- ✅ 简单操作（插入、更新、根据主键查询）使用 MyBatis-Plus API
- ✅ 条件查询必须在 Mapper XML 中定义 SQL 语句
- ✅ 所有 SQL 必须包含 `deleted = 0` 条件，只查询未删除的数据
- ❌ 不使用 Wrapper 构造查询条件

**Entity 和 PO 转换**：
- RepositoryImpl 负责 NodeEntity 和 NodePO 之间的转换
- 转换逻辑封装在 RepositoryImpl 内部，对外只暴露 Entity
- 确保业务层不依赖持久化框架的 PO 类

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

## 六、待确认的设计决策

以下问题需要在需求分析阶段与用户确认：

1. **数据库连接信息**（必须确认）：
   - 各环境的数据库名称
   - 各环境的数据库用户名和密码
   - 各环境的数据库主机地址和端口
   - 是否需要配置多数据源（读写分离、分库分表等）

2. **实体类位置**（建议确认）：
   - NodeEntity 放在哪个模块（domain-api 或 infrastructure）
   - 是否需要区分领域实体和数据库实体

3. **节点类型**（建议确认）：
   - 节点类型是否固定为 5 种（DATABASE、APPLICATION、API、REPORT、OTHER）
   - 是否需要支持自定义节点类型扩展

4. **属性字段**（建议确认）：
   - properties 字段是否存储 JSON 格式
   - 是否有特定的结构要求
   - 是否需要验证 JSON 格式

5. **测试要求**（建议确认）：
   - 是否需要编写单元测试
   - 测试覆盖哪些场景
   - 是否使用实际数据库进行测试

6. **初始化数据**（建议确认）：
   - 是否需要提供数据库初始化脚本
   - 是否需要初始化测试数据

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
