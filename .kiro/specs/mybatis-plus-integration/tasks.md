# MyBatis-Plus 集成与 NodeEntity 实现任务列表

## 任务概述

本任务列表将设计转化为可执行的实现步骤，每个任务都有明确的目标和验收标准。

## 任务依赖关系

```
1.1 配置父 POM
  ↓
1.2 配置 mysql-impl 模块
  ↓
2.1 创建 PageResult 类
  ↓
3.1 创建 MybatisPlusConfig
  ↓
3.2 创建 CustomMetaObjectHandler
  ↓
3.3 配置 application.yml ──┐（可并行）
3.4 配置 application-local.yml ┘
  ↓
4.1 创建 schema.sql
  ↓
5.1 创建 NodeEntity ──┐（可并行）
5.2 创建 NodeRepository ┘
  ↓
6.1 创建 NodePO ──┐（可并行）
6.2 创建 NodeMapper ─┤
6.3 创建 NodeMapper.xml ┘
  ↓
7.1 创建 NodeRepositoryImpl
  ↓
7.2 实现数据验证逻辑
  ↓
7.3 实现异常处理逻辑
  ↓
8.1 创建测试类
  ↓
8.2 编写基础 CRUD 测试
  ↓
8.3 编写数据验证测试
  ↓
8.4 编写异常处理测试
  ↓
8.5 编写边界情况测试
  ↓
9.1 执行所有测试
  ↓
9.2 验证应用启动
  ↓
9.3 代码审查和优化
```

**说明**：
- 任务按照依赖关系排序，必须按顺序执行
- 标注"可并行"的任务可以同时进行
- 每完成一个任务，建议执行 `mvn clean compile` 验证项目可编译
- 测试任务建议顺序执行，确保基础功能正常后再测试复杂场景


## 任务列表

- [x] 1. 配置依赖管理
  - [x] 1.1 配置父 POM 依赖管理
    - **目标**：在父 POM 中配置 MyBatis-Plus 和 Druid 的版本管理
    - **输出产物**：更新 `pom.xml` 文件
    - **验收标准**：
      - 在 `<properties>` 中定义 mybatis-plus.version 为 3.5.7
      - 在 `<properties>` 中定义 druid.version 为 1.2.20
      - 在 `<dependencyManagement>` 中声明 mybatis-plus-spring-boot3-starter 依赖（groupId: com.baomidou）
      - 在 `<dependencyManagement>` 中声明 druid-spring-boot-starter 依赖（groupId: com.alibaba）
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 依赖验证：执行 `mvn dependency:tree` 检查依赖版本是否正确
    - _Requirements: 需求 1 | Design: 4.1.1 父 POM 配置_

  - [x] 1.2 配置 mysql-impl 模块依赖
    - **目标**：在 mysql-impl 模块中声明所需依赖
    - **前置条件**：任务 1.1 已完成
    - **输出产物**：更新 `infrastructure/repository/mysql-impl/pom.xml` 文件
    - **验收标准**：
      - 声明 repository-api、common 模块依赖
      - 声明 mybatis-plus-spring-boot3-starter、druid-spring-boot-starter 依赖（不指定版本号）
      - 声明 mysql-connector-j 依赖（scope=runtime）
      - 声明 lombok 依赖（optional=true）
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 依赖验证：执行 `mvn dependency:tree -pl infrastructure/repository/mysql-impl` 检查依赖是否正确引入且版本从父 POM 继承
    - _Requirements: 需求 1 | Design: 4.1.2 mysql-impl 模块配置_

- [-] 2. 创建通用类
  - [x] 2.1 创建 PageResult 类
    - **目标**：创建通用的分页结果封装类
    - **前置条件**：任务 1.2 已完成
    - **输出产物**：创建 `common/src/main/java/com/demo/ordercore/common/dto/PageResult.java` 文件
    - **验收标准**：
      - 定义为泛型类 PageResult<T>
      - 包含字段：current、size、total、pages、records
      - 实现 Serializable 接口
      - 提供 convert 方法支持类型转换
      - 使用 Lombok 注解
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 功能验证：编写简单测试代码验证 convert 方法的类型转换功能
    - _Requirements: 需求 5 | Design: 3.3.1 PageResult 类_

- [x] 3. 配置 MyBatis-Plus
  - [x] 3.1 创建 MybatisPlusConfig 配置类
    - **目标**：配置 MyBatis-Plus 拦截器和插件
    - **前置条件**：任务 2.1 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/config/MybatisPlusConfig.java` 文件
    - **验收标准**：
      - 配置 MyBatis-Plus 拦截器，包含分页、乐观锁、防全表更新删除三个插件
      - 配置 Mapper 扫描路径指向 mapper 包
      - 分页插件限制单页最大 500 条记录
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap`，在启动日志中搜索 "MybatisPlusInterceptor" 关键字，确认 Bean 创建成功且日志显示加载了 3 个拦截器（PaginationInnerInterceptor、OptimisticLockerInnerInterceptor、BlockAttackInnerInterceptor）
    - _Requirements: 需求 1, 2, 3, 4, 5 | Design: 4.2.1 MybatisPlusConfig_

  - [x] 3.2 创建 CustomMetaObjectHandler 元数据处理器
    - **目标**：实现自动填充创建时间、更新时间等字段
    - **前置条件**：任务 3.1 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/config/CustomMetaObjectHandler.java` 文件
    - **验收标准**：
      - 实现 MyBatis-Plus 元数据自动填充处理器
      - 插入时自动填充 createTime、updateTime、createBy、updateBy 四个字段
      - 更新时自动填充 updateTime、updateBy 两个字段
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap`，在启动日志中搜索 "CustomMetaObjectHandler" 或 "MetaObjectHandler"，确认 Bean 已注册到 Spring 容器
    - _Requirements: 需求 2, 3, 4 | Design: 4.2.2 CustomMetaObjectHandler_

  - [x] 3.3 配置 application.yml
    - **目标**：配置 MyBatis-Plus 全局配置
    - **前置条件**：任务 3.2 已完成
    - **输出产物**：更新 `bootstrap/src/main/resources/application.yml` 文件
    - **验收标准**：
      - 配置 Mapper XML 文件扫描路径
      - 配置实体类别名包路径
      - 配置主键生成策略为雪花算法
      - 配置逻辑删除字段和值
      - 配置驼峰命名转换、缓存策略、日志实现
    - **验证方法**：
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap`，在启动日志中查找 "MyBatis-Plus" 相关信息，确认显示 "Mapper location: classpath*:/mapper/**/*.xml" 和 "Type aliases package: com.demo.ordercore.domain.entity"
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
    - _Requirements: 需求 1, 2, 3, 4, 5 | Design: 4.3 配置文件_

  - [x] 3.4 配置 application-local.yml
    - **目标**：配置 local 环境的数据源
    - **前置条件**：任务 3.3 已完成
    - **输出产物**：创建 `bootstrap/src/main/resources/application-local.yml` 文件
    - **验收标准**：
      - 配置 MySQL 数据源连接信息（URL、用户名、密码、驱动类）
      - 配置 Druid 连接池参数（最小连接数、最大连接数、超时时间等）
    - **验证方法**：
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local`，在启动日志中查找 "Druid" 和 "datasource" 关键字，确认显示 "Druid data source initialized" 和数据库连接 URL
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
    - _Requirements: 需求 1 | Design: 4.3 配置文件_

- [x] 4. 创建数据库表
  - [x] 4.1 创建 schema.sql
    - **目标**：创建 t_node 表的 DDL 脚本
    - **前置条件**：任务 3.4 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/resources/db/schema.sql` 文件
    - **验收标准**：
      - 创建 t_node 表，包含所有业务字段和审计字段
      - 主键使用 BIGINT 类型支持雪花算法
      - 所有字段类型、长度、约束符合设计文档
      - 创建三个索引提升查询性能（节点编码、父节点ID、状态）
      - 添加表和字段注释便于理解
    - **验证方法**：
      - 运行时验证：在 MySQL 数据库中执行 SQL 脚本，执行 `SHOW CREATE TABLE t_node;` 确认表结构正确（包含14个字段），执行 `SHOW INDEX FROM t_node;` 确认创建了4个索引（1个主键索引 + 3个业务索引）
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
    - _Requirements: 需求 2, 3, 4 | Design: 3.2 数据模型设计_

- [x] 5. 创建领域层接口
  - [x] 5.1 创建 NodeEntity 领域实体
    - **目标**：创建 NodeEntity 领域实体类
    - **前置条件**：任务 4.1 已完成
    - **输出产物**：创建 `domain/domain-api/src/main/java/com/demo/ordercore/domain/entity/NodeEntity.java` 文件
    - **验收标准**：
      - 定义所有业务字段（节点编码、名称、父节点、类型、状态、排序等）
      - 定义所有审计字段（创建时间、更新时间、创建人、更新人、删除标记、版本号）
      - 支持序列化以便在分布式环境中传输
      - 所有字段都有清晰的 JavaDoc 注释说明用途
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保类可以正常编译
    - _Requirements: 需求 2, 3, 4 | Design: 3.1 领域模型_

  - [x] 5.2 创建 NodeRepository 仓储接口
    - **目标**：定义 NodeEntity 的仓储接口
    - **前置条件**：任务 5.1 已完成
    - **输出产物**：创建 `domain/domain-api/src/main/java/com/demo/ordercore/domain/repository/NodeRepository.java` 文件
    - **验收标准**：
      - 定义完整的 CRUD 方法（保存、更新、删除、查询）
      - 定义三个条件查询方法（按节点编码、父节点ID、状态查询）
      - 定义分页查询方法，返回 PageResult 类型
      - 所有方法都有清晰的 JavaDoc 注释说明参数、返回值和异常
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保接口可以正常编译
    - _Requirements: 需求 2, 3, 4, 5 | Design: 3.4 仓储接口_

- [x] 6. 创建持久层实现
  - [x] 6.1 创建 NodePO 持久化对象
    - **目标**：创建 NodePO 持久化对象，映射数据库表
    - **前置条件**：任务 5.2 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/po/NodePO.java` 文件
    - **验收标准**：
      - 映射到 t_node 数据库表
      - 定义所有字段，与数据库表结构一致
      - 配置主键生成策略、逻辑删除字段、乐观锁版本字段
      - 使用 Lombok 简化代码
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保类可以正常编译
    - _Requirements: 需求 2, 3, 4 | Design: 3.2 数据模型设计_

  - [x] 6.2 创建 NodeMapper 接口
    - **目标**：创建 NodeMapper 接口，继承 BaseMapper
    - **前置条件**：任务 6.1 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/mapper/NodeMapper.java` 文件
    - **验收标准**：
      - 继承 MyBatis-Plus BaseMapper 获得基础 CRUD 能力
      - 定义四个自定义查询方法（按节点编码、父节点ID、状态查询，以及分页条件查询）
      - 所有方法都有清晰的 JavaDoc 注释说明参数和返回值
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保接口可以正常编译
    - _Requirements: 需求 2, 3, 4, 5 | Design: 4.4 Mapper 接口_

  - [x] 6.3 创建 NodeMapper.xml
    - **目标**：创建 NodeMapper.xml，实现自定义 SQL
    - **前置条件**：任务 6.2 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml` 文件
    - **验收标准**：
      - 定义 ResultMap 映射 NodePO 所有字段
      - 实现四个自定义查询方法的 SQL（按节点编码、父节点ID、状态查询，以及分页条件查询）
      - 所有查询 SQL 都包含逻辑删除过滤条件
    - **验证方法**：
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap`，在启动日志中搜索 "Mapped Statements collection" 或 "NodeMapper"，确认日志显示类似 "Mapped 4 statements in NodeMapper.xml" 的信息，表示 XML 中的 SQL 语句已被正确加载
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
    - _Requirements: 需求 2, 3, 4, 5 | Design: 4.4 Mapper 接口_

- [x] 7. 实现仓储接口
  - [x] 7.1 创建 NodeRepositoryImpl 实现类
    - **目标**：实现 NodeRepository 接口的所有方法
    - **前置条件**：任务 6.3 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/impl/NodeRepositoryImpl.java` 文件
    - **验收标准**：
      - 实现 NodeRepository 接口定义的所有 CRUD 方法
      - 实现领域实体（NodeEntity）和持久化对象（NodePO）之间的双向转换
      - 实现 MyBatis-Plus 分页对象和通用分页结果的转换
      - 所有公共方法都有清晰的 JavaDoc 注释
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保类可以正常编译
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap`，在启动日志中搜索 "NodeRepositoryImpl" 或查看 Bean 创建日志，确认显示 "Creating bean 'nodeRepositoryImpl'" 或类似信息
    - _Requirements: 需求 2, 3, 4, 5 | Design: 4.5 仓储实现_

  - [x] 7.2 实现数据验证逻辑
    - **目标**：在 NodeRepositoryImpl 中实现数据验证
    - **前置条件**：任务 7.1 已完成
    - **输出产物**：更新 `NodeRepositoryImpl.java` 文件
    - **验收标准**：
      - 在 save 和 update 方法中验证必填字段
      - 验证 nodeCode 唯一性
      - 验证 parentId 有效性
      - 验证字段长度和格式
      - 验证失败抛出 IllegalArgumentException
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 代码审查：检查验证逻辑是否完整，覆盖所有必填字段和业务规则
    - _Requirements: 需求 6, 7 | Design: 5.2 数据验证_

  - [x] 7.3 实现异常处理逻辑
    - **目标**：在 NodeRepositoryImpl 中实现异常处理
    - **前置条件**：任务 7.2 已完成
    - **输出产物**：更新 `NodeRepositoryImpl.java` 文件
    - **验收标准**：
      - 捕获数据库异常并转换为业务异常
      - 处理唯一约束冲突
      - 处理乐观锁冲突
      - 记录异常日志
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保项目可以成功构建
      - 代码审查：检查异常处理逻辑是否完整，是否正确捕获和转换异常
    - _Requirements: 需求 8 | Design: 5.1 异常处理_

- [-] 8. 编写测试
  - [x] 8.1 创建 NodeRepositoryImplTest 测试类
    - **目标**：创建测试类框架
    - **前置条件**：任务 7.3 已完成
    - **输出产物**：创建 `infrastructure/repository/mysql-impl/src/test/java/com/demo/ordercore/infrastructure/repository/impl/NodeRepositoryImplTest.java` 文件
    - **验收标准**：
      - 使用 @SpringBootTest 注解
      - 使用 @Transactional 注解（测试后自动回滚）
      - 注入 NodeRepository
      - 创建测试数据准备方法
    - **验证方法**：
      - 构建验证：执行 `mvn clean compile` 确保测试类可以正常编译
    - _Requirements: 需求 9 | Design: 6. 测试策略_

  - [x] 8.2 编写基础 CRUD 测试
    - **目标**：测试基础的增删改查功能
    - **前置条件**：任务 8.1 已完成
    - **输出产物**：更新 `NodeRepositoryImplTest.java` 文件
    - **验收标准**：
      - 测试保存功能（验证自动填充字段、主键生成）
      - 测试更新功能（验证乐观锁、自动填充更新时间）
      - 测试逻辑删除功能（验证 deleted 字段变更）
      - 测试主键查询功能（包括存在和不存在的场景）
      - 测试三个条件查询功能（节点编码、父节点ID、状态）
      - 测试分页查询功能（验证分页参数和结果）
    - **验证方法**：
      - 运行时验证：执行 `mvn test -pl infrastructure/repository/mysql-impl -Dtest=NodeRepositoryImplTest`，确认测试报告显示至少 8 个测试方法全部通过（Tests run: 8, Failures: 0, Errors: 0）
    - _Requirements: 需求 2, 3, 4, 5 | Design: 6. 测试策略_

  - [x] 8.3 编写数据验证测试
    - **目标**：测试数据验证逻辑
    - **前置条件**：任务 8.2 已完成
    - **输出产物**：更新 `NodeRepositoryImplTest.java` 文件
    - **验收标准**：
      - 测试必填字段验证（节点编码、节点名称为空时抛出异常）
      - 测试唯一性约束验证（重复节点编码抛出异常）
      - 测试引用完整性验证（无效的父节点ID抛出异常）
      - 测试字段长度验证（超长字符串抛出异常）
      - 所有验证失败场景都抛出 IllegalArgumentException 并包含清晰的错误信息
    - **验证方法**：
      - 运行时验证：执行 `mvn test -pl infrastructure/repository/mysql-impl -Dtest=NodeRepositoryImplTest`，确认测试报告显示数据验证相关的测试方法全部通过，且日志中可以看到预期的异常信息
    - _Requirements: 需求 6, 7 | Design: 6. 测试策略_

  - [x] 8.4 编写异常处理测试
    - **目标**：测试异常处理逻辑
    - **前置条件**：任务 8.3 已完成
    - **输出产物**：更新 `NodeRepositoryImplTest.java` 文件
    - **验收标准**：
      - 测试数据库唯一约束冲突的异常转换和处理
      - 测试乐观锁版本冲突的异常转换和处理
      - 测试数据库连接异常的异常转换和处理
      - 验证所有异常都包含清晰的错误信息，便于问题定位
    - **验证方法**：
      - 运行时验证：执行 `mvn test -pl infrastructure/repository/mysql-impl -Dtest=NodeRepositoryImplTest`，确认测试报告显示异常处理相关的测试方法全部通过，且日志中可以看到异常被正确捕获和转换
    - _Requirements: 需求 8 | Design: 6. 测试策略_

  - [x] 8.5 编写边界情况测试
    - **目标**：测试边界情况和特殊场景
    - **前置条件**：任务 8.4 已完成
    - **输出产物**：更新 `NodeRepositoryImplTest.java` 文件
    - **验收标准**：
      - 测试分页边界场景（第一页、最后一页、超出范围、空结果集）
      - 测试并发更新场景（模拟多线程同时更新同一记录）
      - 测试大数据量场景（批量插入和查询 1000+ 条记录）
      - 测试特殊字符场景（SQL 注入字符、Unicode 字符等）
    - **验证方法**：
      - 运行时验证：执行 `mvn test -pl infrastructure/repository/mysql-impl -Dtest=NodeRepositoryImplTest`，确认测试报告显示边界情况相关的测试方法全部通过，且性能测试在合理时间内完成（如 1000 条记录查询 < 1 秒）
    - _Requirements: 需求 9, 10 | Design: 6. 测试策略_

- [-] 9. 集成验证
  - [x] 9.1 执行所有测试
    - **目标**：执行完整的测试套件
    - **前置条件**：任务 8.5 已完成
    - **输出产物**：测试报告
    - **验收标准**：
      - 执行 `mvn clean test` 所有测试通过
      - 测试覆盖率达到 80% 以上
      - 无编译错误和警告
    - **验证方法**：
      - 运行时验证：执行 `mvn clean test`，查看测试报告，确认所有测试通过且覆盖率达标
    - _Requirements: 需求 9 | Design: 6. 测试策略_

  - [x] 9.2 验证应用启动
    - **目标**：验证应用可以正常启动
    - **前置条件**：任务 9.1 已完成
    - **输出产物**：应用启动日志
    - **验收标准**：
      - MyBatis-Plus 配置正确加载（拦截器、插件、全局配置）
      - 数据源成功连接到数据库
      - Mapper 接口和 XML 文件正确扫描和映射
      - 所有 Bean 正确创建和注入
      - 应用启动完成且无错误或警告
    - **验证方法**：
      - 运行时验证：执行 `mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local`，查看启动日志，确认以下关键信息：
        - 日志包含 "Started [ApplicationName] in X seconds"（应用启动成功）
        - 日志包含 "Druid data source initialized"（数据源初始化成功）
        - 日志包含 "Mapped X statements"（Mapper XML 加载成功）
        - 日志包含 "MybatisPlusInterceptor"（拦截器加载成功）
        - 日志无 ERROR 或 WARN 级别的异常信息
    - _Requirements: 需求 1, 9 | Design: 全部_

  - [x] 9.3 代码审查和优化
    - **目标**：审查代码质量并进行优化
    - **前置条件**：任务 9.2 已完成
    - **输出产物**：优化后的代码
    - **验收标准**：
      - 代码符合 Java 编码规范和项目约定
      - 所有公共类、接口、方法都有完整的 JavaDoc 注释
      - 消除代码重复，提取公共方法和工具类
      - 清理未使用的导入、变量、方法
      - 关键操作都有适当的日志记录（INFO、WARN、ERROR）
      - 异常处理完整且合理，包含清晰的错误信息
    - **验证方法**：
      - 代码审查：使用以下检查清单逐项审查：
        - [x] 命名规范：类名、方法名、变量名符合驼峰命名规范
        - [x] 注释完整：所有公共 API 都有 JavaDoc，复杂逻辑有行内注释
        - [x] 异常处理：所有可能的异常都被捕获和处理
        - [x] 日志记录：关键操作有日志，日志级别使用正确
        - [x] 代码重复：无明显的代码重复（DRY 原则）
        - [x] 资源管理：数据库连接等资源正确关闭
        - [x] 性能考虑：无明显的性能问题（如 N+1 查询）
      - 构建验证：执行 `mvn clean compile` 确保优化后的代码可以成功构建
      - 测试验证：执行 `mvn clean test` 确保优化后所有测试仍然通过
    - _Requirements: 需求 11 | Design: 全部_

## 执行指南

### 开始执行

1. 确保已阅读需求文档和设计文档
2. 按照任务顺序执行，不要跳过
3. 每完成一个任务，执行 `mvn clean compile` 验证
4. 遇到问题及时记录和反馈

### 验证方法

- **构建验证**：`mvn clean compile`
- **测试验证**：`mvn test`
- **启动验证**：`mvn spring-boot:run -pl bootstrap`
- **依赖验证**：`mvn dependency:tree`

### 预计时间

- 任务 1-2：2 小时
- 任务 3-4：3 小时
- 任务 5-6：4 小时
- 任务 7：6 小时
- 任务 8：8 小时
- 任务 9：2 小时
- **总计**：约 25 小时（3-4 个工作日）

### 故障排查指南

#### 构建失败

**常见原因**：
- 依赖配置错误（版本冲突、groupId/artifactId 错误）
- 语法错误（缺少分号、括号不匹配）
- 导入缺失（缺少必要的 import 语句）
- 模块依赖顺序错误

**排查步骤**：
1. 查看构建日志，定位具体错误信息
2. 检查 pom.xml 中的依赖声明是否正确
3. 执行 `mvn dependency:tree` 检查依赖冲突
4. 确认所有必要的类都已创建且在正确的包路径下
5. 使用 IDE 的自动修复功能（如 IDEA 的 Alt+Enter）

**示例**：
```bash
# 清理并重新构建
mvn clean compile

# 查看详细错误信息
mvn clean compile -X

# 检查特定模块
mvn clean compile -pl infrastructure/repository/mysql-impl
```

#### 测试失败

**常见原因**：
- 数据库连接失败（数据库未启动、连接配置错误）
- 测试数据准备不正确
- 事务未正确回滚导致数据污染
- 测试用例断言错误

**排查步骤**：
1. 确认数据库已启动且可以连接
2. 检查 application-local.yml 中的数据源配置
3. 确认测试类使用了 @Transactional 注解
4. 查看测试日志，定位失败的具体测试方法
5. 单独运行失败的测试方法进行调试

**示例**：
```bash
# 运行单个测试类
mvn test -Dtest=NodeRepositoryImplTest

# 运行单个测试方法
mvn test -Dtest=NodeRepositoryImplTest#testSave

# 查看详细测试日志
mvn test -X
```

#### 应用启动失败

**常见原因**：
- 配置文件格式错误（YAML 缩进错误）
- Bean 循环依赖
- 数据源连接失败
- Mapper 扫描路径错误
- 端口被占用

**排查步骤**：
1. 查看启动日志，定位具体错误信息
2. 检查 application.yml 和 application-local.yml 格式是否正确
3. 确认数据库已启动且连接配置正确
4. 检查 @MapperScan 注解的包路径是否正确
5. 确认应用端口未被占用（默认 8080）

**示例**：
```bash
# 使用 debug 模式启动
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.jvmArguments="-Xdebug"

# 指定不同的端口启动
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.arguments="--server.port=8081"

# 检查端口占用（macOS/Linux）
lsof -i :8080

# 检查端口占用（Windows）
netstat -ano | findstr :8080
```

#### 验证失败

**常见原因**：
- 任务未完全完成
- 验证方法执行错误
- 环境配置问题

**排查步骤**：
1. 重新检查任务的验收标准，确认是否全部满足
2. 确认验证命令执行正确（路径、参数等）
3. 检查环境变量和配置是否正确
4. 如果多次验证失败，考虑回退到上一个成功的任务重新执行

**建议**：
- 每个任务完成后立即验证，不要累积多个任务后再验证
- 使用版本控制（Git）记录每个任务完成后的状态
- 遇到问题及时记录错误信息和解决方案

## 注意事项

1. **渐进式开发**：每个任务完成后都要验证项目可以成功构建
2. **遵循规范**：严格遵循 MyBatis-Plus 最佳实践指南
3. **数据操作规范**：条件查询使用 Mapper XML，插入更新使用 MyBatis-Plus API
4. **测试优先**：先实现功能，再编写测试
5. **代码质量**：保持代码整洁，添加必要的注释
6. **版本控制**：建议每完成一个主任务（如任务 1、2、3 等）就提交一次代码
7. **日志查看**：养成查看日志的习惯，日志中包含大量有用的调试信息
8. **文档参考**：遇到问题时参考 MyBatis-Plus 官方文档和 Spring Boot 文档
