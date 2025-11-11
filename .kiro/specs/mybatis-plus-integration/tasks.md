# MyBatis-Plus 集成与 NodeEntity 实现任务列表

## 任务概述

本任务列表将 MyBatis-Plus 集成和 NodeEntity 实现的设计转化为可执行的开发任务。每个任务都有明确的目标和验收标准。

## 任务列表

- [x] 1. 配置 MyBatis-Plus 和 Druid 依赖
  - 在父 POM 的 dependencyManagement 中添加 MyBatis-Plus、MySQL Connector/J、Druid 版本管理
  - 在 mysql-impl 模块的 POM 中添加依赖（不指定版本）
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 mysql-impl 模块，无错误和警告
    2. 检查父 POM 的 `<dependencyManagement>` 中包含 mybatis-plus-boot-starter（版本 3.5.5）
    3. 检查父 POM 的 `<dependencyManagement>` 中包含 mysql-connector-j（由 Spring Boot 管理版本）
    4. 检查父 POM 的 `<dependencyManagement>` 中包含 druid-spring-boot-starter（版本 1.2.20）
    5. 检查 mysql-impl 模块的 POM 中包含上述三个依赖，且未指定版本号
    6. 执行 `mvn dependency:tree -pl infrastructure/repository/mysql-impl` 确认依赖正确引入

- [x] 2. 创建 NodeEntity 领域实体类
  - 在 repository-api 模块的 com.demo.ordercore.infrastructure.repository.entity 包中创建 NodeEntity 类
  - 定义所有字段（id、name、type、description、properties、createTime、updateTime、createBy、updateBy、deleted、version）
  - 作为纯 POJO，不包含任何框架特定注解
  - _需求: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 5.10, 5.11, 5.12, 5.13_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 repository-api 模块，无错误和警告
    2. 确认 NodeEntity 类位于 `infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/entity/NodeEntity.java`
    3. 确认类上没有任何框架特定注解（如 @TableName、@Entity 等）
    4. 确认所有字段类型正确（id: Long, name/type/description/properties/createBy/updateBy: String, createTime/updateTime: LocalDateTime, deleted/version: Integer）
    5. 确认类包含标准的 getter/setter 方法
    6. 代码审查：确认这是一个纯 POJO，不依赖任何持久化框架

- [x] 2.1. 创建 NodePO 持久化对象
  - 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.po 包中创建 NodePO 类
  - 定义所有字段（与 NodeEntity 相同）
  - 使用正确的 MyBatis-Plus 注解（@TableName、@TableId、@TableField、@TableLogic、@Version）
  - _需求: 5.1.1, 5.1.2, 5.1.3, 5.1.4, 5.1.5, 5.1.6, 5.1.7, 5.1.8, 5.1.9, 5.1.10, 5.1.11, 5.1.12, 5.1.13_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 mysql-impl 模块，无错误和警告
    2. 确认 NodePO 类位于 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/po/NodePO.java`
    3. 确认类上有 `@TableName("t_node")` 注解
    4. 确认 id 字段有 `@TableId(type = IdType.ASSIGN_ID)` 注解
    5. 确认 createTime 字段有 `@TableField(fill = FieldFill.INSERT)` 注解
    6. 确认 updateTime 字段有 `@TableField(fill = FieldFill.INSERT_UPDATE)` 注解
    7. 确认 createBy 字段没有 `@TableField` 注解（通过方法参数传递）
    8. 确认 updateBy 字段没有 `@TableField` 注解（通过方法参数传递）
    9. 确认 deleted 字段有 `@TableLogic` 注解
    10. 确认 version 字段有 `@Version` 注解
    11. 确认所有字段类型正确（与 NodeEntity 一致）

- [x] 3. 创建数据库表和初始化脚本
  - 创建 schema.sql 脚本，包含 t_node 表的 DDL 语句
  - 定义所有字段和约束（主键、唯一索引、普通索引）
  - 设置表字符集为 UTF8MB4，存储引擎为 InnoDB
  - _需求: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9, 8.10, 8.11, 8.12, 8.13, 8.14, 20.1, 20.2, 20.3, 20.4, 20.5, 20.6, 20.7_
  - _验收标准:_
    1. 确认 schema.sql 文件位于 `infrastructure/repository/mysql-impl/src/main/resources/schema.sql`
    2. 执行 schema.sql 脚本成功创建 t_node 表，无错误
    3. 执行 `SHOW CREATE TABLE t_node` 确认表结构正确
    4. 确认主键为 id（BIGINT 类型）
    5. 确认 name 字段有唯一索引 uk_name（VARCHAR(100) NOT NULL）
    6. 确认 type 字段有普通索引 idx_type（VARCHAR(50) NOT NULL）
    7. 确认 deleted 字段有普通索引 idx_deleted（TINYINT NOT NULL DEFAULT 0）
    8. 确认表字符集为 utf8mb4
    9. 确认表存储引擎为 InnoDB
    10. 确认 SQL 脚本包含 `IF NOT EXISTS` 判断，可重复执行
    11. 确认所有字段定义与 NodeEntity 一致

- [x] 4. 配置 MyBatis-Plus 基础功能
  - 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.config 包中创建 MybatisPlusConfig 配置类
  - 配置分页插件（MySQL，单页最大 100）
  - 配置乐观锁插件
  - 配置防全表更新删除插件
  - 使用 @MapperScan 注解，扫描包路径为 com.demo.ordercore.infrastructure.repository.mysql.mapper
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.7_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 mysql-impl 模块，无错误和警告
    2. 确认 MybatisPlusConfig 类位于 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/MybatisPlusConfig.java`
    3. 确认类上有 `@Configuration` 注解
    4. 确认类上有 `@MapperScan("com.demo.ordercore.infrastructure.repository.mysql.mapper")` 注解
    5. 确认存在 `mybatisPlusInterceptor()` 方法，返回类型为 MybatisPlusInterceptor
    6. 代码审查：分页插件配置为 MySQL 类型，maxLimit 设置为 100
    7. 代码审查：添加了 OptimisticLockerInnerInterceptor（乐观锁插件）
    8. 代码审查：添加了 BlockAttackInnerInterceptor（防全表更新删除插件）
    9. 代码审查：拦截器添加顺序正确（分页插件在第一位）

- [x] 5. 创建元数据自动填充处理器
  - 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.config 包中创建 CustomMetaObjectHandler 类
  - 实现 insertFill 方法，自动填充 createTime、updateTime
  - 实现 updateFill 方法，自动填充 updateTime
  - 不填充 createBy 和 updateBy（这两个字段通过方法参数传递）
  - _需求: 2.6_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 mysql-impl 模块，无错误和警告
    2. 确认 CustomMetaObjectHandler 类位于 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/CustomMetaObjectHandler.java`
    3. 确认类实现了 MetaObjectHandler 接口
    4. 确认类上有 `@Component` 注解
    5. 代码审查：insertFill 方法中调用 `strictInsertFill` 填充 createTime（LocalDateTime.now()）
    6. 代码审查：insertFill 方法中调用 `strictInsertFill` 填充 updateTime（LocalDateTime.now()）
    7. 代码审查：insertFill 方法中不填充 createBy 和 updateBy
    8. 代码审查：updateFill 方法中调用 `strictUpdateFill` 填充 updateTime（LocalDateTime.now()）
    9. 代码审查：updateFill 方法中不填充 updateBy



- [x] 6. 配置数据源和 MyBatis-Plus 全局参数
  - 在 application.yml 中配置数据源基础属性（驱动类名、连接池类型）
  - 配置 Druid 连接池参数（初始化连接数、最小空闲连接数、最大活跃连接数、连接超时时间）
  - 配置 Druid 监控统计功能（stat-view-servlet）
  - 配置 Druid SQL 防火墙功能（wall filter）
  - 配置 MyBatis-Plus 全局参数（mapper-locations、type-aliases-package、id-type、table-prefix、logic-delete-field 等）
  - _需求: 3.1, 3.7, 3.8, 3.9, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 26.1, 26.2, 26.3, 26.4, 26.5, 26.6, 26.7, 26.8, 26.9_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译项目，无错误和警告
    2. 确认 application.yml 中配置了 `spring.datasource.driver-class-name: com.mysql.cj.jdbc.Driver`
    3. 确认 application.yml 中配置了 `spring.datasource.type: com.alibaba.druid.pool.DruidDataSource`
    4. 确认 Druid 连接池参数：initial-size: 5, min-idle: 5, max-active: 20, max-wait: 30000
    5. 确认 Druid 监控配置：stat-view-servlet.enabled: true, url-pattern: /druid/*, login-username 和 login-password 已设置
    6. 确认 Druid filter 配置：stat.enabled: true, wall.enabled: true, stat.log-slow-sql: true, stat.slow-sql-millis: 1000
    7. 确认 MyBatis-Plus 配置：mapper-locations: classpath*:/mapper/**/*.xml
    8. 确认 MyBatis-Plus 配置：type-aliases-package: com.demo.ordercore.infrastructure.repository.mysql.po
    9. 确认 MyBatis-Plus 配置：id-type: ASSIGN_ID, table-prefix: t_
    10. 确认 MyBatis-Plus 配置：logic-delete-field: deleted, logic-delete-value: 1, logic-not-delete-value: 0
    11. 确认 MyBatis-Plus 配置：map-underscore-to-camel-case: true, cache-enabled: false
    12. 使用 YAML 验证工具检查配置文件格式正确

- [x] 7. 修改启动类配置
  - 在 OrderCoreApplication 类中移除 DataSourceAutoConfiguration 的排除
  - _需求: 隐含需求（需要启用数据源自动配置）_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 bootstrap 模块，无错误和警告
    2. 确认 OrderCoreApplication 类的 @SpringBootApplication 注解中不再排除 DataSourceAutoConfiguration
    3. 代码审查：确认 exclude 数组中已移除 `org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class`
    4. 代码审查：确认其他排除项（如 RedisAutoConfiguration）保持不变

- [x] 8. 配置多环境数据库连接信息
  - 在 application-local.yml 中配置本地数据库连接信息
  - 在 application-dev.yml 中配置开发环境数据库连接信息（使用环境变量占位符）
  - 在 application-test.yml 中配置测试环境数据库连接信息（使用环境变量占位符）
  - 在 application-staging.yml 中配置预发布环境数据库连接信息（使用环境变量占位符）
  - 在 application-prod.yml 中配置生产环境数据库连接信息（使用环境变量占位符）
  - _需求: 3.2, 3.3, 3.4, 3.5, 3.6, 25.1, 25.2, 25.3, 25.4, 25.5, 25.6, 25.7_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译项目，无错误和警告
    2. 确认 application-local.yml 中配置了完整的数据库连接信息（url: jdbc:mysql://localhost:3306/ordercore_local?..., username: root, password: root123）
    3. 确认 application-dev.yml 中使用环境变量占位符（${DB_HOST}, ${DB_PORT}, ${DB_NAME}, ${DB_USERNAME}, ${DB_PASSWORD}）
    4. 确认 application-test.yml 中使用环境变量占位符
    5. 确认 application-staging.yml 中使用环境变量占位符
    6. 确认 application-prod.yml 中使用环境变量占位符
    7. 确认所有环境的 URL 包含必要的参数（useUnicode=true, characterEncoding=utf8, useSSL=false, serverTimezone=Asia/Shanghai）
    8. 使用 YAML 验证工具检查所有配置文件格式正确

- [x] 9. 配置多环境日志级别
  - 在 application-local.yml 中配置 Mapper 包日志级别为 DEBUG
  - 在 application-dev.yml 中配置 Mapper 包日志级别为 DEBUG
  - 在 application-test.yml 中配置 Mapper 包日志级别为 INFO
  - 在 application-staging.yml 中配置 Mapper 包日志级别为 INFO
  - 在 application-prod.yml 中配置 Mapper 包日志级别为 WARN
  - _需求: 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译项目，无错误和警告
    2. 确认 application-local.yml 中配置了 `logging.level.com.demo.ordercore.infrastructure.repository.mysql.mapper: DEBUG`
    3. 确认 application-dev.yml 中配置了 `logging.level.com.demo.ordercore.infrastructure.repository.mysql.mapper: DEBUG`
    4. 确认 application-test.yml 中配置了 `logging.level.com.demo.ordercore.infrastructure.repository.mysql.mapper: INFO`
    5. 确认 application-staging.yml 中配置了 `logging.level.com.demo.ordercore.infrastructure.repository.mysql.mapper: INFO`
    6. 确认 application-prod.yml 中配置了 `logging.level.com.demo.ordercore.infrastructure.repository.mysql.mapper: WARN`
    7. 使用 YAML 验证工具检查所有配置文件格式正确

- [x] 10. 创建 NodeMapper 接口
  - 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.mapper 包中创建 NodeMapper 接口
  - 继承 BaseMapper<NodePO>
  - 使用 @Mapper 注解
  - 定义 selectByName、selectByType、selectPageByCondition 方法
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 mysql-impl 模块，无错误和警告
    2. 确认 NodeMapper 接口位于 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/mapper/NodeMapper.java`
    3. 确认接口上有 `@Mapper` 注解
    4. 确认接口继承了 `BaseMapper<NodePO>`
    5. 确认存在 `NodePO selectByName(@Param("name") String name)` 方法
    6. 确认存在 `List<NodePO> selectByType(@Param("type") String type)` 方法
    7. 确认存在 `IPage<NodePO> selectPageByCondition(Page<?> page, @Param("name") String name, @Param("type") String type)` 方法
    8. 代码审查：所有自定义方法参数都使用了 @Param 注解

- [x] 11. 创建 NodeMapper XML 文件
  - 在 mysql-impl 模块的 resources/mapper 目录下创建 NodeMapper.xml
  - 定义 BaseResultMap，映射所有字段到 NodePO
  - 实现 selectByName 方法（根据名称查询节点）
  - 实现 selectByType 方法（根据类型查询节点列表）
  - 实现 selectPageByCondition 方法（支持按名称和类型过滤的分页查询）
  - 确保所有 SQL 语句包含 deleted = 0 条件
  - _需求: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  - _验收标准:_
    1. 执行 `mvn clean package` 成功将 Mapper XML 文件打包到 JAR 中，无错误和警告
    2. 确认 NodeMapper.xml 文件位于 `infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml`
    3. 确认 XML 文件的 namespace 为 `com.demo.ordercore.infrastructure.repository.mysql.mapper.NodeMapper`
    4. 确认 BaseResultMap 的 type 为 `com.demo.ordercore.infrastructure.repository.mysql.po.NodePO`
    5. 确认定义了 BaseResultMap，映射了所有字段（id, name, type, description, properties, createTime, updateTime, createBy, updateBy, deleted, version）
    6. 确认 selectByName 方法的 SQL 包含 `WHERE name = #{name} AND deleted = 0`
    7. 确认 selectByType 方法的 SQL 包含 `WHERE type = #{type} AND deleted = 0 ORDER BY create_time DESC`
    8. 确认 selectPageByCondition 方法使用 `<if>` 标签处理动态条件，包含 `WHERE deleted = 0`
    9. 确认 selectPageByCondition 方法的 SQL 包含 `ORDER BY create_time DESC`
    10. 使用 XML 验证工具检查 XML 文件格式正确
    11. 执行 `jar tf target/mysql-impl-*.jar | grep NodeMapper.xml` 确认文件已打包

- [x] 12. 创建 NodeRepository 接口
  - 在 repository-api 模块的 com.demo.ordercore.infrastructure.repository.api 包中创建 NodeRepository 接口
  - 定义 save、update、findById、findByName、findByType、findPage、deleteById 方法
  - _需求: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 repository-api 模块，无错误和警告
    2. 确认 NodeRepository 接口位于 `infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/NodeRepository.java`
    3. 确认存在 `void save(NodeEntity entity, String operator)` 方法
    4. 确认存在 `void update(NodeEntity entity, String operator)` 方法
    5. 确认存在 `NodeEntity findById(Long id)` 方法
    6. 确认存在 `NodeEntity findByName(String name)` 方法
    7. 确认存在 `List<NodeEntity> findByType(String type)` 方法
    8. 确认存在 `IPage<NodeEntity> findPage(Integer current, Integer size, String name, String type)` 方法
    9. 确认存在 `void deleteById(Long id, String operator)` 方法
    10. 代码审查：所有修改操作（save、update、deleteById）都包含 operator 参数

- [x] 13. 创建 NodeRepository 实现类
  - 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.impl 包中创建 NodeRepositoryImpl 类
  - 实现 NodeRepository 接口
  - 使用 @Repository 注解
  - 注入 NodeMapper
  - 实现所有方法，负责 NodeEntity 和 NodePO 之间的转换
  - 实现转换方法：toEntity、toPO、toEntityList、toEntityPage
  - _需求: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9, 10.10, 10.11, 10.12_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译 mysql-impl 模块，无错误和警告
    2. 确认 NodeRepositoryImpl 类位于 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java`
    3. 确认类上有 `@Repository` 注解
    4. 确认类实现了 NodeRepository 接口
    5. 确认注入了 NodeMapper（使用 @Autowired 或构造函数注入）
    6. 代码审查：save 方法将 NodeEntity 转换为 NodePO，设置 operator 到 createBy 和 updateBy，调用 nodeMapper.insert(po)
    7. 代码审查：update 方法将 NodeEntity 转换为 NodePO，设置 operator 到 updateBy，调用 nodeMapper.updateById(po)
    8. 代码审查：deleteById 方法先查询 NodePO，设置 operator 到 updateBy，调用 nodeMapper.deleteById(id)
    9. 代码审查：findById 方法调用 nodeMapper.selectById(id)，将 NodePO 转换为 NodeEntity 返回
    10. 代码审查：findByName 方法调用 nodeMapper.selectByName(name)，将 NodePO 转换为 NodeEntity 返回
    11. 代码审查：findByType 方法调用 nodeMapper.selectByType(type)，将 List<NodePO> 转换为 List<NodeEntity> 返回
    12. 代码审查：findPage 方法创建 Page 对象，调用 nodeMapper.selectPageByCondition(page, name, type)，将 IPage<NodePO> 转换为 IPage<NodeEntity> 返回
    13. 代码审查：存在 toEntity(NodePO po) 方法，实现 PO 到 Entity 的转换
    14. 代码审查：存在 toPO(NodeEntity entity) 方法，实现 Entity 到 PO 的转换
    15. 代码审查：存在 toEntityList(List<NodePO> poList) 方法，实现批量转换
    16. 代码审查：存在 toEntityPage(IPage<NodePO> poPage) 方法，实现分页结果转换

- [x] 14. 实现 JSON 格式验证
  - 在 NodeRepositoryImpl 的 save 和 update 方法中验证 properties 字段的 JSON 格式
  - 使用 Jackson 或 Gson 进行验证
  - 格式错误时抛出 BusinessException
  - 如果 GlobalExceptionHandler 不存在，先在 interface/http 模块中创建它
  - 在 GlobalExceptionHandler 中捕获 JSON 格式验证异常，返回友好的错误信息
  - _需求: 21.1, 21.2, 21.3, 21.4_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译项目，无错误和警告
    2. 代码审查：save 方法在调用 insert 前验证 properties 字段（如果不为空）
    3. 代码审查：update 方法在调用 updateById 前验证 properties 字段（如果不为空）
    4. 代码审查：使用 Jackson 的 ObjectMapper.readTree() 或 Gson 的 JsonParser.parseString() 验证 JSON 格式
    5. 代码审查：JSON 格式错误时抛出 BusinessException，错误信息为"节点属性格式错误，必须是有效的 JSON"
    6. 确认 GlobalExceptionHandler 存在于 interface/http 模块
    7. 代码审查：GlobalExceptionHandler 中有处理 BusinessException 的方法
    8. 代码审查：异常处理方法返回 Result.error() 格式的响应

- [x] 15. 实现唯一约束冲突处理
  - 在 GlobalExceptionHandler 中捕获 DuplicateKeyException
  - 转换为 BusinessException
  - 返回友好的错误信息："节点名称已存在"
  - _需求: 18.1, 18.2, 18.3, 18.4_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译项目，无错误和警告
    2. 代码审查：GlobalExceptionHandler 中有处理 DuplicateKeyException 的方法
    3. 代码审查：方法上有 @ExceptionHandler(DuplicateKeyException.class) 注解
    4. 代码审查：方法返回 Result.error()，错误信息为"节点名称已存在"
    5. 代码审查：错误码设置合理（如 "DUPLICATE_KEY" 或 "40001"）

- [x] 16. 实现乐观锁冲突处理
  - 在 NodeRepositoryImpl 的 update 方法中检查更新结果
  - 更新失败时（影响行数为 0）抛出 BusinessException
  - 返回友好的错误信息："数据已被其他用户修改，请刷新后重试"
  - _需求: 隐含需求（需求 13.5 提到乐观锁机制）_
  - _验收标准:_
    1. 执行 `mvn clean compile` 成功编译项目，无错误和警告
    2. 代码审查：update 方法调用 nodeMapper.updateById(entity) 后检查返回值
    3. 代码审查：如果返回值为 0，抛出 BusinessException，错误信息为"数据已被其他用户修改，请刷新后重试"
    4. 代码审查：错误码设置合理（如 "OPTIMISTIC_LOCK_CONFLICT" 或 "40002"）

- [x] 17. 配置测试数据库
  - 在 mysql-impl 模块的 src/test/resources 目录下创建 application-test.yml
  - 配置测试数据库连接信息（jdbc:mysql://localhost:3306/ordercore_test）
  - _需求: 23.1, 23.2, 23.3, 23.4, 23.5, 23.6_
  - _验收标准:_
    1. 确认 application-test.yml 文件位于 `infrastructure/repository/mysql-impl/src/test/resources/application-test.yml`
    2. 确认配置了 `spring.datasource.url: jdbc:mysql://localhost:3306/ordercore_test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai`
    3. 确认配置了 `spring.datasource.username: root`
    4. 确认配置了 `spring.datasource.password: root123`
    5. 使用 YAML 验证工具检查配置文件格式正确
    6. 执行 `mvn test -Dtest=NodeRepositoryImplTest` 确认使用测试数据库（检查日志中的数据库连接信息）

- [x] 18. 编写单元测试
  - 在 mysql-impl 模块中创建 NodeRepositoryImplTest 测试类
  - 使用 @SpringBootTest 和 @Transactional 注解
  - 编写测试方法验证 save、findById、findByName、findByType、findPage、update、deleteById 功能
  - 编写测试方法验证唯一约束冲突场景
  - 编写测试方法验证乐观锁并发更新场景
  - 编写测试方法验证逻辑删除功能
  - 编写测试方法验证自动填充功能
  - _需求: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6, 19.7, 19.8, 19.9, 19.10, 19.11, 19.12_
  - _验收标准:_
    1. 执行 `mvn test` 所有测试通过，无失败和错误
    2. 确认 NodeRepositoryImplTest 类位于 `infrastructure/repository/mysql-impl/src/test/java/com/demo/infrastructure/repository/sql/impl/NodeRepositoryImplTest.java`
    3. 确认类上有 `@SpringBootTest` 和 `@Transactional` 注解
    4. 确认存在 testSave() 方法，验证保存功能和自动填充（id、createTime、updateTime、createBy、updateBy、deleted、version）
    5. 确认存在 testFindById() 方法，验证根据 ID 查询功能
    6. 确认存在 testFindByName() 方法，验证根据名称查询功能
    7. 确认存在 testFindByType() 方法，验证根据类型查询功能
    8. 确认存在 testFindPage() 方法，验证分页查询功能（总记录数、总页数、当前页数据）
    9. 确认存在 testUpdate() 方法，验证更新功能和自动填充（updateTime、updateBy、version 自动增加）
    10. 确认存在 testDeleteById() 方法，验证逻辑删除功能（deleted = 1，查询时不返回）
    11. 确认存在 testDuplicateName() 方法，验证唯一约束冲突（创建和更新时名称重复抛出异常）
    12. 确认存在 testOptimisticLock() 方法，验证乐观锁并发更新（并发更新同一节点，后更新的操作失败）
    13. 执行 `mvn test -Dtest=NodeRepositoryImplTest` 确认所有测试方法都通过

- [ ] 19. 集成验证
  - 启动应用，检查日志中是否输出 MyBatis-Plus 初始化成功信息
  - 访问 Druid 监控页面（/druid/*），验证监控功能正常
  - 验证所有 CRUD 功能正常工作
  - _需求: 2.8, 3.10, 4.8_
  - _验收标准:_
    1. 执行 `mvn clean package` 成功打包项目，无错误和警告
    2. 使用 `java -jar bootstrap/target/bootstrap-*.jar --spring.profiles.active=local` 启动应用
    3. 应用启动成功，无错误日志
    4. 检查日志中包含 MyBatis-Plus 初始化信息（如 "MyBatis-Plus initialized"）
    5. 检查日志中包含 Mapper 扫描信息（扫描到 NodeMapper）
    6. 检查日志中包含数据源连接成功信息
    7. 访问 `http://localhost:8080/druid/` 可以打开 Druid 监控页面
    8. 使用配置的用户名密码（admin/admin123）登录 Druid 监控页面
    9. 在 Druid 监控页面中可以看到数据源信息和 SQL 统计
    10. 如果有测试接口，调用接口验证 CRUD 功能正常（创建、查询、更新、删除节点）
    11. 检查日志中输出 SQL 语句（因为 local 环境日志级别为 DEBUG）
    12. 应用可以正常关闭，无错误日志

## 任务执行说明

1. **任务顺序**：按照任务编号顺序执行，确保依赖关系正确
2. **验证要求**：每个任务完成后，执行验收标准中的验证步骤
3. **编译要求**：每个任务完成后，项目必须可以成功编译
4. **必需任务**：所有任务都是必需的，必须全部完成
5. **问题处理**：如果遇到问题，及时记录并寻求帮助

## 任务依赖关系

**严格顺序执行的任务**：
- 任务 1 → 任务 2, 2.1, 3（依赖配置必须先完成）
- 任务 2.1 → 任务 4（MybatisPlusConfig 需要知道 PO 类位置）
- 任务 6 → 任务 7（启动类修改必须在数据源配置之后）
- 任务 2.1 → 任务 10（NodeMapper 依赖 NodePO）
- 任务 10 → 任务 11（Mapper XML 依赖 Mapper 接口）
- 任务 2, 12 → 任务 13（Repository 实现依赖 NodeEntity、Repository 接口）
- 任务 10, 13 → 任务 13（Repository 实现依赖 Mapper）
- 任务 13 → 任务 14-16（错误处理依赖 Repository 实现）
- 所有前置任务 → 任务 17-18（测试依赖完整功能）
- 所有前置任务 → 任务 19（集成验证）

**可以并行执行的任务**：
- 任务 2、2.1、3 可以并行（Entity、PO、数据库表互不依赖）
- 任务 8、9 可以并行（多环境配置和日志配置互不依赖）

**推荐执行顺序**：
1 → 2, 2.1, 3 → 4 → 5 → 6 → 7 → 8, 9 → 10 → 11 → 12 → 13 → 14, 15, 16 → 17 → 18 → 19

