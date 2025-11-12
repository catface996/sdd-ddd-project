# MyBatis-Plus 集成与 NodeEntity 实现设计文档

## 1. 概述

### 1.1 设计目标

在现有 DDD 分层架构的多模块 Maven 工程中集成 MyBatis-Plus ORM 框架，并实现 NodeEntity 持久化功能。

**核心目标**：
- 集成 MyBatis-Plus 3.5.7 作为数据持久化框架
- 实现 NodeEntity 的完整 CRUD 功能
- 遵循 DDD 分层架构原则，保持领域层与基础设施层的解耦
- 支持多环境配置（local、dev、test、staging、prod）
- 确保数据操作的安全性（防 SQL 注入、防全表操作、乐观锁）

### 1.2 设计范围

**包含内容**：
- Maven 依赖管理策略
- MyBatis-Plus 核心功能配置策略
- 数据模型设计（Entity、PO 分离策略）
- 数据访问层架构设计
- 通用分页结果类设计
- 数据库表结构设计
- 多环境数据源配置策略
- 测试策略

**不包含内容**：
- 业务逻辑实现（Application 层）
- API 接口设计（Interface 层）
- 前端交互设计

### 1.3 技术选型

| 技术 | 版本 | 选型理由 |
|------|------|---------|
| MyBatis-Plus | 3.5.7 | 基于 MyBatis 增强，提供强大 CRUD 能力，支持 Spring Boot 3，社区活跃 |
| Druid | 1.2.20 | 阿里巴巴开源，性能优秀，提供监控和统计功能，支持 Spring Boot 3 |
| MySQL | 8.x | 成熟稳定的关系型数据库，支持事务和复杂查询 |
| Lombok | Spring Boot 管理 | 减少样板代码，提高开发效率 |

## 2. 架构设计

### 2.1 整体架构

**分层架构**：

```
Application Layer (业务逻辑层 - 未来实现)
    ↓ 依赖
Repository API Layer (仓储接口层)
    - NodeRepository (接口)
    - NodeEntity (领域实体)
    ↓ 实现
Repository Implementation Layer (仓储实现层)
    - NodeRepositoryImpl (实现类)
    - NodeMapper (Mapper 接口)
    - NodePO (持久化对象)
    - NodeMapper.xml (SQL 定义)
    ↓ 操作
Database Layer (数据库层)
    - t_node 表
```

**架构原则**：
1. **依赖倒置**：业务层依赖 Repository 接口，不依赖实现
2. **分层解耦**：领域层（Entity）与基础设施层（PO）分离
3. **单向依赖**：上层依赖下层，下层不依赖上层
4. **框架隔离**：框架特定代码封装在 mysql-impl 模块


### 2.2 模块依赖关系

**依赖关系图**：

```
bootstrap (启动模块)
    ↓ 依赖
infrastructure/repository/mysql-impl (MySQL 实现)
    ↓ 依赖
infrastructure/repository/repository-api (仓储接口)
    ↓ 依赖
common (通用模块)
```

**依赖说明**：
- bootstrap 依赖 mysql-impl：启动时加载 MySQL 实现
- mysql-impl 依赖 repository-api：实现仓储接口
- mysql-impl 依赖 common：使用 PageResult
- repository-api 依赖 common：使用 PageResult
- mysql-impl 依赖 MyBatis-Plus、Druid、MySQL Connector：技术框架依赖

**为什么这么设计**：
- 单向依赖：上层依赖下层，下层不依赖上层
- 依赖倒置：业务层依赖接口，不依赖实现
- 框架隔离：框架特定代码封装在 mysql-impl 模块

### 2.3 模块职责划分

#### 2.3.1 common 模块

**职责**：提供跨层共享的通用类

**要做什么**：
- 创建 `PageResult<T>` 泛型类
- 包含分页字段：current（Long）、size（Long）、total（Long）、pages（Long）、records（List<T>）
- 提供全参构造方法和无参构造方法
- 提供 convert(Function<T, R>) 方法，返回 PageResult<R>
- 实现 Serializable 接口
- 使用 Lombok @Data 注解

**为什么这么做**：
- 分页结构在所有层都一致，无需分层定义，避免重复代码和无意义的转换
- convert 方法支持不同层之间的类型转换（Entity、DTO、VO）
- 与项目现有 Result<T> 的设计理念保持一致
- 业界主流做法（Spring Data 的 Page<T>、MyBatis-Plus 的 IPage<T> 都是跨层共享）

**依赖关系**：无外部模块依赖

#### 2.3.2 infrastructure/repository/repository-api 模块

**职责**：定义数据访问契约，保持领域层纯净

**要做什么**：
- 创建 `NodeEntity` 纯 POJO 类，包含所有业务字段（id、name、type、description、properties、createTime、updateTime、createBy、updateBy、deleted、version）
- 创建 `NodeRepository` 接口，定义数据访问方法（save、update、findById、findByName、findByType、findPage、deleteById）
- 接口方法包含 operator 参数（用于审计）
- 返回类型使用 PageResult（不使用框架特定类型）
- 使用 Lombok 简化代码
- 实现 Serializable 接口

**为什么这么做**：
- Entity 是纯 POJO，遵循 DDD 原则，领域模型不应依赖技术框架
- operator 参数支持审计需求，记录操作人信息
- 返回 PageResult 避免业务层依赖 MyBatis-Plus 框架
- 遵循依赖倒置原则，业务层不应依赖技术实现

**依赖关系**：
- 依赖 common 模块（使用 PageResult）

#### 2.3.3 infrastructure/repository/mysql-impl 模块

**职责**：基于 MyBatis-Plus 实现数据访问

**要做什么**：
- 创建 MybatisPlusConfig 配置类，注册插件（分页、乐观锁、防全表操作）
- 创建 CustomMetaObjectHandler，自动填充时间字段
- 创建 NodePO 类，包含 MyBatis-Plus 注解
- 创建 NodeMapper 接口，继承 BaseMapper，定义自定义查询方法
- 创建 NodeMapper.xml，定义所有条件查询的 SQL
- 创建 NodeRepositoryImpl 实现类，负责 Entity 和 PO 转换
- 创建 schema.sql 数据库初始化脚本
- 在 RepositoryImpl 中处理审计字段和默认值

**为什么这么做**：
- Entity 和 PO 分离：Entity 是领域概念，PO 是技术实现，分离后易于替换持久化方案
- 条件查询用 XML：SQL 集中管理，便于 DBA 审查和性能优化，避免 SQL 分散在代码中
- RepositoryImpl 做转换：封装技术细节，对外只暴露领域概念
- 配置内聚：所有 MySQL 相关配置和实现都在 mysql-impl 模块

**依赖关系**：
- 依赖 repository-api 模块（实现接口）
- 依赖 common 模块（使用 PageResult）
- 依赖 MyBatis-Plus、Druid、MySQL Connector

### 2.4 数据流转设计

**核心流程**：

1. **保存操作**：
   - Application 调用 Repository.save(Entity, operator)
   - RepositoryImpl 转换 Entity → PO
   - 设置审计字段（createBy、updateBy）
   - 设置默认值（deleted=0、version=0，如果为 null）
   - 调用 Mapper.insert
   - CustomMetaObjectHandler 自动填充时间戳（createTime、updateTime）
   - 回填生成的 ID 和时间戳到 Entity

2. **查询操作**：
   - Application 调用 Repository.findByXxx
   - RepositoryImpl 调用 Mapper 方法
   - Mapper 执行 XML 中定义的 SQL（包含 deleted=0 条件）
   - 返回 PO 对象或列表
   - RepositoryImpl 转换 PO → Entity
   - 返回 Entity 给 Application

3. **更新操作**：
   - Application 调用 Repository.update(Entity, operator)
   - RepositoryImpl 转换 Entity → PO
   - 设置 updateBy，清空 updateTime（让自动填充生效）
   - 调用 Mapper.updateById
   - CustomMetaObjectHandler 自动填充 updateTime
   - 乐观锁插件检查 version 并自动增加
   - 回填时间戳和版本号到 Entity

4. **删除操作**：
   - Application 调用 Repository.deleteById(id, operator)
   - RepositoryImpl 查询 PO
   - 设置 updateBy
   - 调用 Mapper.deleteById（逻辑删除，设置 deleted=1）
   - CustomMetaObjectHandler 自动填充 updateTime

5. **分页查询操作**：
   - Application 调用 Repository.findPage(current, size, name, type)
   - RepositoryImpl 创建 Page 对象
   - 调用 Mapper.selectPageByCondition
   - Mapper 执行 XML 中定义的 SQL（包含 deleted=0 条件和可选的过滤条件）
   - 分页插件自动处理分页逻辑
   - 返回 IPage<PO>
   - RepositoryImpl 转换 IPage<PO> → PageResult<Entity>
   - 返回 PageResult<Entity> 给 Application

**为什么这么设计**：
- 审计字段通过方法参数传递：操作人信息需要从业务上下文获取，不能自动生成
- 时间字段由 CustomMetaObjectHandler 自动填充：统一处理，避免遗漏
- 默认值字段在 save 方法中手动设置：确保数据完整性，避免 null 值
- 所有查询都包含 deleted = 0 条件：逻辑删除后的数据不应被查询到
- 分页查询转换为 PageResult：避免业务层依赖 MyBatis-Plus 的 IPage


## 3. 详细设计

### 3.1 依赖管理设计（覆盖需求1）

**要做什么**：

1. **父 POM 配置**：
   - 在 properties 中定义 mybatis-plus.version=3.5.7
   - 在 properties 中定义 druid.version=1.2.20
   - 在 dependencyManagement 中声明 com.baomidou:mybatis-plus-spring-boot3-starter，版本使用 ${mybatis-plus.version}
   - 在 dependencyManagement 中声明 com.alibaba:druid-spring-boot-starter，版本使用 ${druid.version}

2. **mysql-impl 模块配置**：
   - 引入 repository-api 模块依赖（不指定版本）
   - 引入 common 模块依赖（不指定版本）
   - 引入 mybatis-plus-spring-boot3-starter（不指定版本）
   - 引入 druid-spring-boot-starter（不指定版本）
   - 引入 mysql-connector-j，scope=runtime（不指定版本）
   - 引入 lombok，scope=provided（不指定版本）

**为什么这么做**：
- 使用 mybatis-plus-spring-boot3-starter：Spring Boot 3 对 Jakarta EE 的支持与 Spring Boot 2 不同，必须使用专用启动器
- 选择 Druid：提供强大的监控和统计功能，性能优秀，支持 SQL 防火墙
- 版本统一管理：避免版本冲突，便于升级维护
- runtime scope：mysql-connector-j 只在运行时需要
- provided scope：lombok 只在编译时需要

### 3.2 MyBatis-Plus 配置设计（覆盖需求2）

**要做什么**：

1. **MybatisPlusConfig 配置类**：
   - 使用 @Configuration 注解
   - 使用 @MapperScan("com.demo.infrastructure.repository.mysql.mapper")
   - 创建 MybatisPlusInterceptor Bean
   - 按顺序注册插件：分页插件（第一位）、乐观锁插件、防全表更新删除插件
   - 分页插件配置：DbType.MYSQL、maxLimit=100、overflow=false

2. **CustomMetaObjectHandler 配置类**：
   - 使用 @Component 注解
   - 实现 MetaObjectHandler 接口
   - insertFill 方法：填充 createTime 和 updateTime
   - updateFill 方法：填充 updateTime

**为什么这么做**：
- 分页插件必须第一位：MyBatis-Plus 要求分页插件在拦截器链的最前面
- 限制单页 100 条：防止大量数据查询影响性能和内存
- 不自动填充 createBy/updateBy：操作人信息需要从业务上下文获取，不能自动生成
- 使用逻辑删除：保留数据历史记录，支持数据恢复和审计
- 使用乐观锁：防止并发更新冲突，保证数据一致性
- 防全表操作：防止误操作导致数据丢失或性能问题

### 3.3 应用配置设计（覆盖需求3和需求4）

**要做什么**：

1. **application.yml 全局配置**：
   - mapper-locations: classpath*:/mapper/**/*.xml
   - type-aliases-package: com.demo.infrastructure.repository.mysql.po
   - logic-delete-field: deleted
   - logic-delete-value: 1
   - logic-not-delete-value: 0
   - map-underscore-to-camel-case: true
   - log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

2. **多环境数据源配置**：
   - 创建 application-local.yml
   - 创建 application-dev.yml
   - 创建 application-test.yml
   - 创建 application-staging.yml
   - 创建 application-prod.yml
   - 每个文件配置：spring.datasource.url、username、password、driver-class-name、type
   - 配置 Druid 连接池：initial-size、min-idle、max-active、max-wait
   - 驱动类名使用：com.mysql.cj.jdbc.Driver
   - 数据源类型：com.alibaba.druid.pool.DruidDataSource

3. **Mapper XML 文件位置**：
   - 在 infrastructure/repository/mysql-impl/src/main/resources/mapper/ 目录下创建 XML 文件
   - 确保 XML 文件会被打包到 JAR 中

**为什么这么做**：
- 配置 PO 路径而不是 Entity 路径：MyBatis-Plus 直接操作 PO，Entity 是领域概念
- 使用 classpath*：支持多模块项目，扫描所有 JAR 包中的 Mapper XML
- 每个环境独立配置：不同环境使用不同的数据库，便于管理和切换
- 使用 Druid 连接池：提供监控和统计功能，性能优秀
- 使用 com.mysql.cj.jdbc.Driver：MySQL 8.x 的新驱动类名
- Mapper XML 放在 resources/mapper：遵循 Maven 资源目录约定，确保被打包


### 3.4 数据模型设计

#### 3.4.1 PageResult 通用分页结果类（覆盖需求5）

**要做什么**：
- 创建泛型类 PageResult<T>
- 定义字段：current（Long）、size（Long）、total（Long）、pages（Long）、records（List<T>）
- 提供全参构造方法和无参构造方法
- 提供 convert(Function<T, R>) 方法，返回 PageResult<R>
- 实现 Serializable 接口
- 使用 Lombok @Data 注解

**为什么这么做**：
- 使用泛型：支持不同层的数据类型（Entity、DTO、VO）
- 提供 convert 方法：便于在不同层之间转换数据类型
- 实现 Serializable：支持分布式场景下的序列化传输
- 使用 Long 类型：支持大数据量场景

#### 3.4.2 NodeEntity 领域实体（覆盖需求6）

**要做什么**：
- 创建纯 POJO 类 NodeEntity
- 定义字段：id（Long）、name（String）、type（String）、description（String）、properties（String）、createTime（LocalDateTime）、updateTime（LocalDateTime）、createBy（String）、updateBy（String）、deleted（Integer）、version（Integer）
- 实现 Serializable 接口
- 使用 Lombok @Data 注解
- 添加字段注释说明

**为什么这么做**：
- 不包含框架注解：遵循 DDD 原则，领域模型不应依赖技术框架
- 实现 Serializable：支持序列化，便于缓存和传输
- 使用 Lombok：减少样板代码，提高可读性
- 使用包装类型：避免 null 值问题

#### 3.4.3 NodePO 持久化对象（覆盖需求7）

**要做什么**：
- 创建 NodePO 类
- 使用 @TableName("t_node") 指定表名
- id 字段使用 @TableId(type = IdType.ASSIGN_ID)
- createTime 字段使用 @TableField(fill = FieldFill.INSERT)
- updateTime 字段使用 @TableField(fill = FieldFill.INSERT_UPDATE)
- deleted 字段使用 @TableLogic
- version 字段使用 @Version
- 实现 Serializable 接口
- 使用 Lombok @Data 注解
- 包含与 NodeEntity 相同的所有字段

**为什么这么做**：
- 使用雪花算法：分布式环境下生成唯一 ID，性能好，无需数据库自增
- 使用 @TableLogic：支持逻辑删除，MyBatis-Plus 自动处理查询和删除
- 使用 @Version：支持乐观锁，MyBatis-Plus 自动处理并发更新
- 自动填充时间字段：统一处理，避免遗漏

### 3.5 数据访问层设计

#### 3.5.1 NodeRepository 仓储接口（覆盖需求8）

**要做什么**：
- 创建 NodeRepository 接口
- 定义 save(NodeEntity entity, String operator) 方法
- 定义 update(NodeEntity entity, String operator) 方法
- 定义 findById(Long id) 方法，返回 NodeEntity
- 定义 findByName(String name) 方法，返回 NodeEntity
- 定义 findByType(String type) 方法，返回 List<NodeEntity>
- 定义 findPage(Integer current, Integer size, String name, String type) 方法，返回 PageResult<NodeEntity>
- 定义 deleteById(Long id, String operator) 方法

**为什么这么做**：
- 包含 operator 参数：支持审计需求，记录操作人信息
- 返回 PageResult：避免业务层依赖 MyBatis-Plus 框架
- 不依赖框架：遵循依赖倒置原则，业务层不应依赖技术实现

#### 3.5.2 NodeMapper 接口（覆盖需求9）

**要做什么**：
- 创建 NodeMapper 接口
- 使用 @Mapper 注解
- 继承 BaseMapper<NodePO>
- 定义 selectByName(@Param("name") String name) 方法，返回 NodePO
- 定义 selectByType(@Param("type") String type) 方法，返回 List<NodePO>
- 定义 selectPageByCondition(Page<?> page, @Param("name") String name, @Param("type") String type) 方法，返回 IPage<NodePO>

**为什么这么做**：
- 继承 BaseMapper：获得 MyBatis-Plus 提供的基础 CRUD 能力，减少代码
- 使用 @Param：MyBatis 要求多参数方法使用 @Param 注解
- SQL 在 XML 中定义：便于管理和优化，避免 SQL 分散在代码中

#### 3.5.3 NodeMapper.xml SQL 定义（覆盖需求10）

**要做什么**：
- 创建 NodeMapper.xml 文件
- namespace 设置为：com.demo.infrastructure.repository.mysql.mapper.NodeMapper
- 定义 BaseResultMap，type 为：com.demo.infrastructure.repository.mysql.po.NodePO
- 映射所有字段（id、name、type、description、properties、create_time、update_time、create_by、update_by、deleted、version）
- 定义 selectByName 查询，包含 deleted = 0 条件
- 定义 selectByType 查询，包含 deleted = 0 条件，按 create_time DESC 排序
- 定义 selectPageByCondition 查询，支持可选的 name 模糊查询和 type 精确查询，包含 deleted = 0 条件
- 所有参数使用 #{} 占位符

**为什么这么做**：
- 所有查询都包含 deleted = 0：逻辑删除后的数据不应被查询到
- 使用参数化查询：防止 SQL 注入攻击
- 支持可选参数：提高查询灵活性，支持多种查询场景
- 按 create_time 降序排序：最新创建的数据排在前面

#### 3.5.4 NodeRepositoryImpl 实现类（覆盖需求11）

**要做什么**：
- 创建 NodeRepositoryImpl 类
- 使用 @Repository 注解
- 实现 NodeRepository 接口
- 使用 @RequiredArgsConstructor 注入 NodeMapper
- 实现私有方法 toEntity(NodePO) 和 toPO(NodeEntity)
- 在 save 方法中：转换 Entity→PO，设置 createBy/updateBy，设置 deleted=0/version=0（如果为 null），调用 insert，回填 ID 和时间戳
- 在 update 方法中：转换 Entity→PO，设置 updateBy，清空 updateTime，调用 updateById，回填时间戳和版本号
- 在 findById 方法中：调用 selectById，转换 PO→Entity
- 在 findByName 方法中：调用 selectByName，转换 PO→Entity
- 在 findByType 方法中：调用 selectByType，转换 PO 列表→Entity 列表
- 在 findPage 方法中：创建 Page 对象，调用 selectPageByCondition，转换 IPage→PageResult
- 在 deleteById 方法中：查询 PO，设置 updateBy，调用 deleteById

**为什么这么做**：
- 在 RepositoryImpl 中转换：封装技术细节，对外只暴露领域概念
- 回填 ID 和时间戳：业务层可能需要这些信息
- updateTime 设置为 null：让 CustomMetaObjectHandler 自动填充最新时间
- deleteById 先查询：需要设置 updateBy，记录删除操作人
- 转换方法为 private：不对外暴露技术实现细节


### 3.6 数据库设计（覆盖需求12）

#### 3.6.1 表结构设计

**要做什么**：
- 创建表 t_node
- 定义字段：
  - id（BIGINT，主键，NOT NULL，COMMENT '主键ID'）
  - name（VARCHAR(100)，NOT NULL，COMMENT '节点名称'）
  - type（VARCHAR(50)，NOT NULL，COMMENT '节点类型'）
  - description（VARCHAR(500)，COMMENT '节点描述'）
  - properties（TEXT，COMMENT '节点属性JSON'）
  - create_time（DATETIME，NOT NULL，COMMENT '创建时间'）
  - update_time（DATETIME，NOT NULL，COMMENT '更新时间'）
  - create_by（VARCHAR(50)，NOT NULL，COMMENT '创建人'）
  - update_by（VARCHAR(50)，NOT NULL，COMMENT '更新人'）
  - deleted（TINYINT，NOT NULL，DEFAULT 0，COMMENT '逻辑删除标记'）
  - version（INT，NOT NULL，DEFAULT 0，COMMENT '版本号'）
- 创建主键索引：PRIMARY KEY (id)
- 创建唯一索引：UNIQUE KEY uk_name (name)
- 创建普通索引：KEY idx_type (type)
- 创建普通索引：KEY idx_deleted (deleted)
- 设置字符集：UTF8MB4
- 设置存储引擎：InnoDB
- 添加表注释：'系统节点表'

**为什么这么做**：
- 使用 BIGINT 作为主键：支持雪花算法生成的 64 位 ID
- name 字段唯一索引：业务要求节点名称唯一，同时提升查询性能
- 在 type 和 deleted 上创建索引：这两个字段经常用于查询条件
- 使用 UTF8MB4：支持完整的 Unicode 字符集，包括 emoji
- 使用 InnoDB：支持事务和外键，性能好
- 添加字段注释：便于理解字段含义，提高可维护性

#### 3.6.2 数据库初始化脚本

**要做什么**：
- 创建 schema.sql 文件
- 使用 CREATE TABLE IF NOT EXISTS 语法
- 包含完整的表结构定义
- 包含所有索引定义
- 添加表注释和字段注释
- 放置在 infrastructure/repository/mysql-impl/src/main/resources/db/ 目录

**为什么这么做**：
- 使用 IF NOT EXISTS：支持重复执行，不会报错
- 添加字段注释：便于理解字段含义，提高可维护性
- 放在 resources/db 目录：遵循 Spring Boot 约定，便于管理

## 4. 错误处理设计

### 4.1 异常类型

**数据库异常**：
- **DuplicateKeyException**：唯一约束冲突（如节点名称重复）
- **OptimisticLockException**：乐观锁冲突（并发更新）
- **DataAccessException**：数据访问异常（如连接失败）

**业务异常**：
- **EntityNotFoundException**：实体不存在（如查询不存在的节点）
- **IllegalArgumentException**：参数非法（如 ID 为 null）

### 4.2 异常处理策略

**Repository 层**：
- 捕获 MyBatis-Plus 异常，转换为业务异常
- 记录详细的错误日志
- 不在 Repository 层处理业务逻辑

**异常转换示例**：
```java
try {
    mapper.insert(po);
} catch (DuplicateKeyException e) {
    log.error("节点名称重复: {}", entity.getName(), e);
    throw new BusinessException("节点名称已存在");
}
```

**为什么这么做**：
- 封装技术细节：业务层不应感知 MyBatis-Plus 异常
- 提供友好的错误信息：便于前端展示和用户理解
- 记录详细日志：便于问题排查和监控

### 4.3 事务处理

**事务边界**：
- 事务在 Application 层（Service）开启
- Repository 层不开启事务
- 使用 @Transactional 注解

**事务配置**：
- 传播行为：REQUIRED（默认）
- 隔离级别：READ_COMMITTED（默认）
- 回滚策略：rollbackFor = Exception.class

**为什么这么做**：
- 事务边界在业务层：符合分层架构原则
- Repository 层不开启事务：避免事务嵌套和复杂性
- 回滚所有异常：确保数据一致性

## 5. 非功能性设计

### 5.1 性能设计

**优化策略**：

1. **索引优化**：
   - name 字段唯一索引（uk_name）：保证唯一性，提升查询性能
   - type 字段普通索引（idx_type）：提升类型查询性能
   - deleted 字段普通索引（idx_deleted）：提升逻辑删除查询性能

2. **分页限制**：
   - 单页最大 100 条记录
   - 防止大量数据查询影响性能
   - 配置 overflow=false，防止溢出总页数

3. **连接池配置**：
   - 初始连接数（initial-size）：5
   - 最小空闲连接数（min-idle）：5
   - 最大活跃连接数（max-active）：20
   - 最大等待时间（max-wait）：60000ms（60 秒）

4. **SQL 优化**：
   - 所有查询都包含 deleted = 0 条件
   - 使用参数化查询（#{}）
   - 避免全表扫描
   - 明确列出查询字段（不使用 SELECT *）

5. **查询优化**：
   - 条件查询使用索引字段
   - 分页查询按 create_time 降序排序（利用索引）
   - 支持可选参数，避免不必要的条件

**性能指标**：
- 单表查询响应时间 < 100ms
- 分页查询响应时间 < 200ms
- 并发支持：20 个并发连接

**为什么这么做**：
- 限制单页 100 条：防止大量数据查询影响性能和内存
- 配置连接池参数：合理的连接池配置可以提高并发性能，避免连接耗尽
- 使用参数化查询：防止 SQL 注入，同时提高查询性能（预编译）
- 创建合适的索引：提升查询性能，特别是高频查询字段

### 5.2 安全设计

**安全措施**：

1. **SQL 注入防护**：
   - 使用 MyBatis 参数化查询
   - 所有参数使用 #{} 占位符

2. **防全表操作**：
   - 配置 BlockAttackInnerInterceptor
   - 拦截无条件的 UPDATE 和 DELETE

3. **并发控制**：
   - 使用乐观锁（@Version）
   - 防止并发更新冲突

4. **逻辑删除**：
   - 不物理删除数据
   - 保留数据历史记录

**为什么这么做**：
- 使用参数化查询：防止 SQL 注入攻击，这是最基本的安全措施
- 防全表操作：防止误操作导致数据丢失或性能问题
- 使用乐观锁：在高并发场景下保证数据一致性，避免数据覆盖
- 使用逻辑删除：保留数据历史，支持数据恢复和审计

### 5.3 可维护性设计

**维护策略**：

1. **代码规范**：
   - 遵循 MyBatis-Plus 最佳实践
   - 使用 Lombok 简化代码
   - 添加清晰的注释

2. **SQL 管理**：
   - 所有条件查询在 XML 中定义
   - SQL 集中管理，便于优化

3. **分层解耦**：
   - Entity 和 PO 分离
   - Repository 接口和实现分离
   - 业务层不依赖持久化框架

4. **配置管理**：
   - 多环境配置分离
   - 版本号统一管理
   - 配置项清晰明确

**为什么这么做**：
- SQL 在 XML 中定义：便于 DBA 审查和优化，避免 SQL 分散在代码中
- Entity 和 PO 分离：易于替换持久化方案，保持领域模型纯净
- 多环境配置分离：不同环境使用不同的数据库，便于管理和切换

### 5.4 可测试性设计（覆盖需求13）

**测试策略**：

1. **测试框架**：
   - Spring Boot Test + JUnit 5
   - @SpringBootTest 加载完整上下文
   - @Transactional 自动回滚
   - @ActiveProfiles("local") 激活 local 环境

2. **测试数据库**：
   - 使用实际 MySQL 数据库
   - local 环境配置
   - 测试后自动回滚

3. **测试覆盖**：
   - 基本 CRUD 操作（testSave、testFindById、testUpdate、testDeleteById）
   - 条件查询（testFindByName、testFindByType）
   - 分页查询（testFindPage）
   - 唯一约束（testUniqueConstraint）
   - 乐观锁（testOptimisticLock）

4. **测试隔离**：
   - 每个测试方法独立
   - 不依赖执行顺序
   - 使用 @Transactional 回滚

**为什么这么做**：
- 使用实际数据库：更接近生产环境，测试更可靠
- 使用 @Transactional：测试后自动回滚，不影响数据库状态
- 测试方法独立：避免测试之间的相互影响，提高测试可靠性


## 6. 技术决策记录（ADR）

### ADR-001：选择 MyBatis-Plus 作为 ORM 框架

**状态**：已接受

**背景**：
- 项目需要数据持久化能力
- 需要支持复杂的条件查询
- 需要良好的性能和可维护性

**决策**：选择 MyBatis-Plus 3.5.7 作为 ORM 框架

**理由**：
- 基于 MyBatis，性能优秀，SQL 可控
- 提供强大的 CRUD 能力，减少样板代码
- 支持 Lambda 查询，类型安全
- 内置分页、乐观锁等常用功能
- 支持 Spring Boot 3
- 社区活跃，文档完善

**后果**：
- 正面：开发效率高，代码简洁，功能强大，性能好
- 负面：需要学习 MyBatis-Plus 特性，增加依赖

**替代方案**：
- JPA/Hibernate：更抽象，但 SQL 不可控，性能调优困难
- 纯 MyBatis：灵活，但样板代码多，开发效率低

### ADR-002：Entity 和 PO 分离

**状态**：已接受

**背景**：
- 需要遵循 DDD 分层架构
- 领域层不应依赖基础设施框架
- 需要保持领域模型的纯净性

**决策**：将 Entity（领域实体）和 PO（持久化对象）分离

**理由**：
- Entity 是纯 POJO，不包含框架注解，符合 DDD 原则
- PO 包含 MyBatis-Plus 注解，用于数据库映射
- Repository 实现类负责 Entity 和 PO 之间的转换
- 业务层只依赖 Entity，不依赖 PO
- 符合 DDD 的依赖倒置原则
- 易于替换持久化实现（如切换到 JPA）

**后果**：
- 正面：分层清晰，易于替换持久化实现，领域模型纯净，符合 DDD 原则
- 负面：需要进行 Entity 和 PO 之间的转换，增加少量代码

**替代方案**：
- Entity 和 PO 合并：减少转换代码，但领域层依赖技术框架，违反 DDD 原则

### ADR-003：条件查询使用 XML 定义 SQL

**状态**：已接受

**背景**：
- MyBatis-Plus 提供 Wrapper 构造查询条件
- 项目需要统一管理 SQL 语句
- 需要便于 DBA 审查和优化

**决策**：所有条件查询在 Mapper XML 中定义 SQL，不使用 Wrapper

**理由**：
- SQL 集中管理，便于维护和优化
- 便于 DBA 审查和性能分析
- 避免 SQL 分散在各个 Service 中
- 提高代码可读性和可维护性
- 符合项目的 MyBatis-Plus 最佳实践
- SQL 可见，易于调试和优化

**后果**：
- 正面：SQL 统一管理，易于优化，便于审查，可读性好
- 负面：需要编写 XML 文件，不如 Wrapper 灵活

**替代方案**：
- 使用 Wrapper：灵活，但 SQL 分散，难以管理和优化

### ADR-004：PageResult 放在 common 模块

**状态**：已接受

**背景**：
- 需要在各层之间传递分页数据
- 分页结构在所有层都是一致的
- 需要避免重复定义

**决策**：将 PageResult 放在 common 模块，作为通用的分页结果封装类

**理由**：
- 分页是通用概念，结构在所有层都一致
- 避免在各层之间进行无意义的转换
- 与 common/dto/Result<T> 的设计理念一致
- 符合实用主义原则，减少复杂度
- 提供 convert 方法支持类型转换
- 业界主流做法（Spring Data 的 Page<T>、MyBatis-Plus 的 IPage<T> 都是跨层共享）

**后果**：
- 正面：简单实用，减少重复代码，易于使用，避免过度设计
- 负面：PageResult 跨层共享，不符合严格的分层原则

**替代方案**：
- 每层定义自己的 Page 模型：符合严格分层，但导致大量重复代码和无意义转换

### ADR-005：使用 Druid 作为数据库连接池

**状态**：已接受

**背景**：
- 需要高性能的数据库连接池
- 需要监控和统计功能
- 需要支持 Spring Boot 3

**决策**：使用 Druid 1.2.20 作为数据库连接池

**理由**：
- 阿里巴巴开源，性能优秀
- 提供强大的监控和统计功能
- 支持 SQL 防火墙，增强安全性
- 支持 Spring Boot 3
- 社区活跃，文档完善
- 国内使用广泛，经验丰富

**后果**：
- 正面：性能好，功能强大，监控完善，安全性高
- 负面：增加依赖，配置相对复杂

**替代方案**：
- HikariCP：Spring Boot 默认连接池，性能好，但监控功能弱
- DBCP：Apache 开源，但性能和功能不如 Druid

### ADR-006：审计字段通过方法参数传递

**状态**：已接受

**背景**：
- 需要记录操作人信息（createBy、updateBy）
- MyBatis-Plus 提供自动填充功能
- 操作人信息需要从业务上下文获取

**决策**：审计字段（createBy、updateBy）通过方法参数传递，不使用自动填充

**理由**：
- 操作人信息需要从业务上下文（如 Spring Security）获取
- 不同的操作可能有不同的操作人
- 自动填充无法灵活处理不同场景
- 通过方法参数传递更明确，易于理解和测试

**后果**：
- 正面：灵活，明确，易于测试，支持不同场景
- 负面：需要在每个方法中传递 operator 参数

**替代方案**：
- 使用自动填充：简化代码，但不够灵活，难以处理不同场景

### ADR-007：使用雪花算法生成主键

**状态**：已接受

**背景**：
- 需要生成唯一的主键 ID
- 分布式环境下需要避免 ID 冲突
- 需要高性能的 ID 生成方案

**决策**：使用雪花算法（ASSIGN_ID）生成主键

**理由**：
- 分布式环境下生成唯一 ID
- 性能好，无需访问数据库
- ID 有序，便于索引
- MyBatis-Plus 内置支持
- 64 位长整型，范围足够大

**后果**：
- 正面：性能好，分布式友好，无需数据库自增
- 负面：ID 不连续，可能泄露业务信息（如创建时间）

**替代方案**：
- 数据库自增：简单，但分布式环境下需要额外处理
- UUID：唯一性好，但无序，索引性能差


## 7. 风险和应对

### 7.1 技术风险

**风险 1：版本兼容性问题**

- **描述**：Spring Boot 3.4.1 + MyBatis-Plus 3.5.7 + JDK 21 的兼容性
- **影响**：可能导致启动失败或运行时错误
- **概率**：低
- **应对**：
  - 使用 mybatis-plus-spring-boot3-starter（Spring Boot 3 专用）
  - 参考官方文档和社区实践
  - 在 local 环境充分测试

**风险 2：数据库连接信息缺失**

- **描述**：各环境的数据库连接信息未提供
- **影响**：无法连接数据库，功能无法测试
- **概率**：中
- **应对**：
  - 在需求阶段与用户确认
  - 提供配置模板
  - local 环境使用默认配置

**风险 3：并发更新冲突**

- **描述**：多个用户同时更新同一节点
- **影响**：数据不一致
- **概率**：中
- **应对**：
  - 使用乐观锁（@Version）
  - 捕获并处理 OptimisticLockerException
  - 提示用户重新加载数据

### 7.2 性能风险

**风险 1：分页查询性能问题**

- **描述**：大数据量时分页查询性能下降
- **影响**：响应时间超过 200ms
- **概率**：中
- **应对**：
  - 在 type 和 deleted 字段上创建索引
  - 限制单页最大 100 条
  - 避免深度分页

**风险 2：连接池耗尽**

- **描述**：高并发时连接池连接数不足
- **影响**：请求等待或超时
- **概率**：低
- **应对**：
  - 合理配置连接池参数
  - 监控连接池使用情况
  - 及时释放连接

### 7.3 数据风险

**风险 1：唯一约束冲突**

- **描述**：创建或更新节点时名称重复
- **影响**：操作失败，抛出异常
- **概率**：中
- **应对**：
  - 在数据库层面创建唯一索引
  - 在应用层捕获并处理异常
  - 返回友好的错误信息

**风险 2：JSON 格式错误**

- **描述**：properties 字段存储的不是有效的 JSON
- **影响**：数据解析失败
- **概率**：低
- **应对**：
  - 在应用层验证 JSON 格式（如需要）
  - 提供清晰的错误提示
  - 记录错误日志

## 8. 需求覆盖检查

### 8.1 需求覆盖矩阵

| 需求编号 | 需求描述 | 设计章节 | 覆盖情况 |
|---------|---------|---------|---------|
| 需求 1 | MyBatis-Plus 框架集成 | 3.1 依赖管理设计 | ✅ 完全覆盖 |
| 需求 2 | MyBatis-Plus 配置 | 3.2 MyBatis-Plus 配置设计 | ✅ 完全覆盖 |
| 需求 3 | 应用配置 | 3.3 应用配置设计 | ✅ 完全覆盖 |
| 需求 4 | 多环境数据源配置 | 3.3 应用配置设计 | ✅ 完全覆盖 |
| 需求 5 | 通用分页结果类 | 3.4.1 PageResult 设计 | ✅ 完全覆盖 |
| 需求 6 | NodeEntity 领域实体定义 | 3.4.2 NodeEntity 设计 | ✅ 完全覆盖 |
| 需求 7 | NodePO 持久化对象定义 | 3.4.3 NodePO 设计 | ✅ 完全覆盖 |
| 需求 8 | NodeRepository 仓储接口定义 | 3.5.1 NodeRepository 设计 | ✅ 完全覆盖 |
| 需求 9 | NodeMapper 接口定义 | 3.5.2 NodeMapper 设计 | ✅ 完全覆盖 |
| 需求 10 | NodeMapper XML 配置 | 3.5.3 NodeMapper.xml 设计 | ✅ 完全覆盖 |
| 需求 11 | NodeRepositoryImpl 实现类 | 3.5.4 NodeRepositoryImpl 设计 | ✅ 完全覆盖 |
| 需求 12 | 数据库表创建 | 3.6 数据库设计 | ✅ 完全覆盖 |
| 需求 13 | 集成测试 | 4.4 可测试性设计 | ✅ 完全覆盖 |
| 需求 14 | 项目构建验证 | 8.2 验收标准 | ✅ 完全覆盖 |

### 8.2 非功能性需求覆盖

| 非功能性需求 | 设计章节 | 覆盖情况 |
|-------------|---------|---------|
| 错误处理要求 | 4. 错误处理设计 | ✅ 完全覆盖 |
| 性能要求 | 5.1 性能设计 | ✅ 完全覆盖 |
| 安全要求 | 5.2 安全设计 | ✅ 完全覆盖 |
| 可维护性要求 | 5.3 可维护性设计 | ✅ 完全覆盖 |
| 可测试性要求 | 5.4 可测试性设计 | ✅ 完全覆盖 |

## 9. 实施计划

### 9.1 实施阶段

**阶段 1：依赖和配置**（需求 1-4）
- 配置父 POM 依赖管理（mybatis-plus.version、druid.version）
- 配置 mysql-impl 模块依赖（mybatis-plus、druid、mysql-connector-j、lombok、repository-api、common）
- 创建 MybatisPlusConfig 配置类（分页插件、乐观锁插件、防全表操作插件）
- 创建 CustomMetaObjectHandler 配置类（自动填充时间字段）
- 配置 application.yml（mapper-locations、type-aliases-package、逻辑删除、驼峰命名、日志）
- 配置多环境数据源（application-local.yml、application-dev.yml、application-test.yml、application-staging.yml、application-prod.yml）

**阶段 2：数据模型**（需求 5-7）
- 创建 PageResult 类（common 模块）
- 创建 NodeEntity 类（repository-api 模块）
- 创建 NodePO 类（mysql-impl 模块）

**阶段 3：数据访问层**（需求 8-11）
- 创建 NodeRepository 接口（repository-api 模块）
- 创建 NodeMapper 接口（mysql-impl 模块）
- 创建 NodeMapper.xml（mysql-impl/resources/mapper）
- 创建 NodeRepositoryImpl 实现类（mysql-impl 模块）

**阶段 4：数据库**（需求 12）
- 创建数据库初始化脚本 schema.sql（mysql-impl/resources/db）
- 执行脚本创建 t_node 表

**阶段 5：测试**（需求 13）
- 创建集成测试类 NodeRepositoryImplTest（bootstrap/src/test）
- 编写测试用例（testSave、testFindById、testFindByName、testFindByType、testFindPage、testUpdate、testDeleteById、testUniqueConstraint、testOptimisticLock）
- 执行测试验证

**阶段 6：验证**（需求 14）
- 编译项目（mvn clean compile）
- 打包项目（mvn clean package）
- 启动应用
- 验证功能（MyBatis-Plus 初始化、数据库连接、Mapper 扫描）

### 9.2 验收标准

**构建验证**：
- ✅ mvn clean compile 成功
- ✅ mvn clean package 成功
- ✅ 无依赖冲突错误

**启动验证**：
- ✅ 应用成功启动
- ✅ MyBatis-Plus 初始化成功
- ✅ 数据库连接成功

**功能验证**：
- ✅ 创建节点成功
- ✅ 查询节点成功
- ✅ 更新节点成功
- ✅ 删除节点成功
- ✅ 分页查询成功
- ✅ 唯一约束生效
- ✅ 乐观锁生效

**测试验证**：
- ✅ 所有测试用例通过
- ✅ 测试覆盖核心功能

## 10. 参考资料

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Druid 官方文档](https://github.com/alibaba/druid)
- 项目 MyBatis-Plus 最佳实践：`.kiro/steering/06-mybatis-plus-best-practice.md`
- 原始需求文档：`mybatis-plus-integration.md`
- 需求文档：`.kiro/specs/mybatis-plus-integration/requirements.md`
