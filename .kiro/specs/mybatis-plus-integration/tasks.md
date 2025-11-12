# MyBatis-Plus 集成与 NodeEntity 实现任务列表

## 任务概述

本任务列表将设计方案转化为可执行的开发任务，按照增量开发的原则，确保每个任务完成后项目都处于可构建状态。

## 任务列表

- [x] 1. 配置项目依赖管理
  - [x] 1.1 配置父 POM 依赖版本
    - 在父 POM 的 `<properties>` 中添加 `mybatis-plus.version` 为 3.5.7
    - 在父 POM 的 `<properties>` 中添加 `druid.version` 为 1.2.20
    - 在父 POM 的 `<dependencyManagement>` 中声明 `mybatis-plus-spring-boot3-starter`，版本使用 `${mybatis-plus.version}`
    - 在父 POM 的 `<dependencyManagement>` 中声明 `druid-spring-boot-starter`，版本使用 `${druid.version}`
    - _需求：1.3-1.6_
  - [x] 1.2 配置 mysql-impl 模块依赖
    - 在 `infrastructure/repository/mysql-impl/pom.xml` 中引入 `mybatis-plus-spring-boot3-starter`（不指定版本）
    - 引入 `druid-spring-boot-starter`（不指定版本）
    - 引入 `mysql-connector-j`，scope 为 runtime（不指定版本）
    - 引入 `lombok`，scope 为 provided（不指定版本）
    - 引入 `repository-api` 模块依赖（不指定版本）
    - 引入 `common` 模块依赖（不指定版本）
    - _需求：1.7-1.12_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功，无依赖冲突错误
    - 检查父 POM 的 `<properties>` 包含 mybatis-plus.version 和 druid.version
    - 检查父 POM 的 `<dependencyManagement>` 包含两个依赖声明
    - 检查 mysql-impl 模块的 pom.xml 包含 6 个依赖（mybatis-plus、druid、mysql-connector-j、lombok、repository-api、common）
    - 确认所有依赖都不指定版本号（从父 POM 继承）

- [x] 2. 创建 MyBatis-Plus 配置类
  - [x] 2.1 创建 MybatisPlusConfig 配置类
    - 在 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/` 创建 MybatisPlusConfig 类
    - 添加 `@Configuration` 注解
    - 添加 `@MapperScan("com.demo.infrastructure.repository.mysql.mapper")` 注解
    - 创建 `mybatisPlusInterceptor()` 方法，返回 MybatisPlusInterceptor
    - 按顺序注册插件：分页插件（DbType.MYSQL、maxLimit=100、overflow=false）、乐观锁插件、防全表更新删除插件
    - _需求：2.1-2.6_
  - [x] 2.2 创建 CustomMetaObjectHandler 配置类
    - 在 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/` 创建 CustomMetaObjectHandler 类
    - 添加 `@Component` 注解
    - 实现 MetaObjectHandler 接口
    - 实现 `insertFill()` 方法，填充 createTime 和 updateTime 为当前时间
    - 实现 `updateFill()` 方法，填充 updateTime 为当前时间
    - _需求：2.7-2.11_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 MybatisPlusConfig 类存在，包含 @Configuration 和 @MapperScan 注解
    - 检查 mybatisPlusInterceptor() 方法按顺序注册了 3 个插件
    - 检查分页插件配置了 DbType.MYSQL、maxLimit=100、overflow=false
    - 检查 CustomMetaObjectHandler 类存在，包含 @Component 注解
    - 检查 insertFill() 方法填充 createTime 和 updateTime
    - 检查 updateFill() 方法填充 updateTime

- [x] 3. 配置应用全局设置和多环境数据源
  - [x] 3.1 配置 application.yml 全局设置
    - 在 `bootstrap/src/main/resources/application.yml` 中配置 `mybatis-plus.mapper-locations` 为 `classpath*:/mapper/**/*.xml`
    - 配置 `mybatis-plus.type-aliases-package` 为 `com.demo.infrastructure.repository.mysql.po`
    - 配置 `mybatis-plus.global-config.db-config.logic-delete-field` 为 `deleted`
    - 配置 `mybatis-plus.global-config.db-config.logic-delete-value` 为 `1`
    - 配置 `mybatis-plus.global-config.db-config.logic-not-delete-value` 为 `0`
    - 配置 `mybatis-plus.configuration.map-underscore-to-camel-case` 为 `true`
    - 配置 `mybatis-plus.configuration.log-impl` 为 `org.apache.ibatis.logging.slf4j.Slf4jImpl`
    - _需求：3.1-3.7_
  - [x] 3.2 创建多环境数据源配置文件
    - 在 `bootstrap/src/main/resources/` 创建 `application-local.yml`
    - 创建 `application-dev.yml`
    - 创建 `application-test.yml`
    - 创建 `application-staging.yml`
    - 创建 `application-prod.yml`
    - 每个文件配置 `spring.datasource.url`、`username`、`password`、`driver-class-name`（com.mysql.cj.jdbc.Driver）、`type`（com.alibaba.druid.pool.DruidDataSource）
    - 配置 Druid 连接池：`initial-size: 5`、`min-idle: 5`、`max-active: 20`、`max-wait: 60000`
    - _需求：4.1-4.10_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 application.yml 包含 7 个 MyBatis-Plus 配置项
    - 检查 mapper-locations 配置为 `classpath*:/mapper/**/*.xml`
    - 检查 type-aliases-package 配置为 PO 包路径
    - 检查逻辑删除配置正确（logic-delete-field、logic-delete-value、logic-not-delete-value）
    - 检查 5 个环境配置文件都存在
    - 检查每个环境配置文件包含数据源配置（url、username、password、driver-class-name、type）
    - 检查每个环境配置文件包含 Druid 连接池配置（initial-size、min-idle、max-active、max-wait）

- [x] 4. 实现通用分页结果类
  - 在 `common/src/main/java/com/demo/common/dto/` 创建 `PageResult<T>` 泛型类
  - 添加 `@Data` 注解（Lombok）
  - 实现 Serializable 接口
  - 定义字段：`current`（Long）、`size`（Long）、`total`（Long）、`pages`（Long）、`records`（List<T>）
  - 提供全参构造方法和无参构造方法
  - 实现 `convert(Function<T, R> converter)` 方法，返回 `PageResult<R>`
  - _需求：5.1-5.7_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 PageResult 类存在于 common/dto 包
    - 检查 PageResult 是泛型类 `PageResult<T>`
    - 检查包含 5 个字段（current、size、total、pages、records）
    - 检查字段类型正确（current/size/total/pages 为 Long，records 为 List<T>）
    - 检查实现了 Serializable 接口
    - 检查有 @Data 注解
    - 检查有 convert 方法，参数为 Function<T, R>，返回 PageResult<R>

- [x] 5. 定义 NodeEntity 领域实体
  - 在 `infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/entity/` 创建 NodeEntity 类
  - 添加 `@Data` 注解（Lombok）
  - 实现 Serializable 接口
  - 定义字段：`id`（Long）、`name`（String）、`type`（String）、`description`（String）、`properties`（String）、`createTime`（LocalDateTime）、`updateTime`（LocalDateTime）、`createBy`（String）、`updateBy`（String）、`deleted`（Integer）、`version`（Integer）
  - 不添加任何框架特定注解（纯 POJO）
  - _需求：6.1-6.5_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 NodeEntity 类存在于 repository-api/entity 包
    - 检查是纯 POJO 类，不包含任何框架注解（@TableName、@TableId 等）
    - 检查包含 11 个字段，字段类型正确
    - 检查实现了 Serializable 接口
    - 检查有 @Data 注解

- [x] 6. 定义 NodeRepository 仓储接口
  - 在 `infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/` 创建 NodeRepository 接口
  - 定义 `save(NodeEntity entity, String operator)` 方法，返回 void
  - 定义 `update(NodeEntity entity, String operator)` 方法，返回 void
  - 定义 `findById(Long id)` 方法，返回 NodeEntity
  - 定义 `findByName(String name)` 方法，返回 NodeEntity
  - 定义 `findByType(String type)` 方法，返回 List<NodeEntity>
  - 定义 `findPage(Integer current, Integer size, String name, String type)` 方法，返回 PageResult<NodeEntity>
  - 定义 `deleteById(Long id, String operator)` 方法，返回 void
  - _需求：8.1-8.8_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 NodeRepository 接口存在于 repository-api 包
    - 检查包含 8 个方法（save、update、findById、findByName、findByType、findPage、deleteById）
    - 检查 save 和 update 方法包含 operator 参数
    - 检查 findPage 方法返回 PageResult<NodeEntity>
    - 检查 deleteById 方法包含 operator 参数

- [x] 7. 定义 NodePO 持久化对象
  - 在 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/po/` 创建 NodePO 类
  - 添加 `@Data` 注解（Lombok）
  - 添加 `@TableName("t_node")` 注解
  - 实现 Serializable 接口
  - 定义字段：与 NodeEntity 相同的 11 个字段
  - id 字段添加 `@TableId(type = IdType.ASSIGN_ID)` 注解
  - createTime 字段添加 `@TableField(fill = FieldFill.INSERT)` 注解
  - updateTime 字段添加 `@TableField(fill = FieldFill.INSERT_UPDATE)` 注解
  - deleted 字段添加 `@TableLogic` 注解
  - version 字段添加 `@Version` 注解
  - _需求：7.1-7.8_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 NodePO 类存在于 mysql-impl/po 包
    - 检查有 @TableName("t_node") 注解
    - 检查包含 11 个字段，与 NodeEntity 字段一致
    - 检查 id 字段有 @TableId(type = IdType.ASSIGN_ID) 注解
    - 检查 createTime 字段有 @TableField(fill = FieldFill.INSERT) 注解
    - 检查 updateTime 字段有 @TableField(fill = FieldFill.INSERT_UPDATE) 注解
    - 检查 deleted 字段有 @TableLogic 注解
    - 检查 version 字段有 @Version 注解

- [x] 8. 定义 NodeMapper 接口
  - 在 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/mapper/` 创建 NodeMapper 接口
  - 添加 `@Mapper` 注解
  - 继承 `BaseMapper<NodePO>`
  - 定义 `selectByName(@Param("name") String name)` 方法，返回 NodePO
  - 定义 `selectByType(@Param("type") String type)` 方法，返回 List<NodePO>
  - 定义 `selectPageByCondition(Page<?> page, @Param("name") String name, @Param("type") String type)` 方法，返回 IPage<NodePO>
  - _需求：9.1-9.6_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 NodeMapper 接口存在于 mysql-impl/mapper 包
    - 检查有 @Mapper 注解
    - 检查继承了 BaseMapper<NodePO>
    - 检查包含 3 个自定义方法（selectByName、selectByType、selectPageByCondition）
    - 检查方法参数使用了 @Param 注解
    - 检查 selectPageByCondition 方法第一个参数为 Page<?>

- [x] 9. 实现 NodeMapper.xml SQL 定义
  - 在 `infrastructure/repository/mysql-impl/src/main/resources/mapper/` 创建 NodeMapper.xml 文件
  - 配置 namespace 为 `com.demo.infrastructure.repository.mysql.mapper.NodeMapper`
  - 定义 BaseResultMap，type 为 `com.demo.infrastructure.repository.mysql.po.NodePO`
  - 在 BaseResultMap 中映射所有 11 个字段（id、name、type、description、properties、createTime、updateTime、createBy、updateBy、deleted、version）
  - 实现 selectByName 查询：使用 BaseResultMap，WHERE name = #{name} AND deleted = 0
  - 实现 selectByType 查询：使用 BaseResultMap，WHERE type = #{type} AND deleted = 0，ORDER BY create_time DESC
  - 实现 selectPageByCondition 查询：使用 BaseResultMap，支持可选的 name 模糊查询（LIKE）和 type 精确查询，WHERE deleted = 0，ORDER BY create_time DESC
  - _需求：10.1-10.7_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 NodeMapper.xml 文件存在于 mysql-impl/resources/mapper 目录
    - 检查 namespace 为 com.demo.infrastructure.repository.mysql.mapper.NodeMapper
    - 检查 BaseResultMap 定义存在，type 为 NodePO 全限定名
    - 检查 BaseResultMap 映射了所有 11 个字段
    - 检查 selectByName 查询存在，包含 deleted = 0 条件
    - 检查 selectByType 查询存在，包含 deleted = 0 条件和 ORDER BY create_time DESC
    - 检查 selectPageByCondition 查询存在，支持可选参数（使用 <if test="...">），包含 deleted = 0 条件

- [x] 10. 实现 NodeRepositoryImpl 仓储实现类
  - [x] 10.1 创建 NodeRepositoryImpl 类和转换方法
    - 在 `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/` 创建 NodeRepositoryImpl 类
    - 添加 `@Repository` 注解
    - 实现 NodeRepository 接口
    - 使用 `@RequiredArgsConstructor` 注入 NodeMapper
    - 实现私有方法 `toEntity(NodePO po)`：字段复制，返回 NodeEntity
    - 实现私有方法 `toPO(NodeEntity entity)`：字段复制，返回 NodePO
    - _需求：11.1-11.4, 11.12_
  - [x] 10.2 实现 save 方法
    - 调用 toPO 转换 Entity 为 PO
    - 设置 createBy 和 updateBy 为 operator 参数
    - 如果 deleted 为 null，设置为 0
    - 如果 version 为 null，设置为 0
    - 调用 mapper.insert(po)
    - 回填 PO 的 id、createTime、updateTime 到 Entity
    - _需求：11.5_
  - [x] 10.3 实现 update 方法
    - 调用 toPO 转换 Entity 为 PO
    - 设置 updateBy 为 operator 参数
    - 将 updateTime 设置为 null（让自动填充生效）
    - 调用 mapper.updateById(po)
    - 回填 PO 的 updateTime 和 version 到 Entity
    - _需求：11.6_
  - [x] 10.4 实现查询方法
    - 实现 findById：调用 mapper.selectById，如果 PO 为 null 返回 null，否则调用 toEntity 转换
    - 实现 findByName：调用 mapper.selectByName，如果 PO 为 null 返回 null，否则调用 toEntity 转换
    - 实现 findByType：调用 mapper.selectByType，遍历 PO 列表调用 toEntity 转换为 Entity 列表
    - 实现 findPage：创建 Page 对象，调用 mapper.selectPageByCondition，转换 IPage<NodePO> 为 PageResult<NodeEntity>
    - _需求：11.7-11.10_
  - [x] 10.5 实现 deleteById 方法
    - 调用 mapper.selectById 查询 PO
    - 如果 PO 不为 null，设置 updateBy 为 operator 参数
    - 调用 mapper.deleteById 执行逻辑删除
    - _需求：11.11_
  - **验收标准**：
    - 执行 `mvn clean compile` 成功
    - 检查 NodeRepositoryImpl 类存在于 mysql-impl/impl 包
    - 检查有 @Repository 注解
    - 检查实现了 NodeRepository 接口
    - 检查注入了 NodeMapper 依赖
    - 检查有私有方法 toEntity 和 toPO
    - 检查 save 方法设置了 createBy、updateBy、deleted、version，并回填了 id 和时间戳
    - 检查 update 方法设置了 updateBy，清空了 updateTime，并回填了时间戳和版本号
    - 检查 findById、findByName、findByType、findPage 方法都进行了 PO 到 Entity 的转换
    - 检查 deleteById 方法先查询 PO，设置 updateBy，然后调用 deleteById

- [x] 11. 创建数据库表和初始化脚本
  - 在 `infrastructure/repository/mysql-impl/src/main/resources/db/` 创建 schema.sql 文件
  - 使用 `CREATE TABLE IF NOT EXISTS t_node` 语法
  - 定义所有 11 个字段：id（BIGINT，主键）、name（VARCHAR(100)，NOT NULL）、type（VARCHAR(50)，NOT NULL）、description（VARCHAR(500)）、properties（TEXT）、create_time（DATETIME，NOT NULL）、update_time（DATETIME，NOT NULL）、create_by（VARCHAR(50)，NOT NULL）、update_by（VARCHAR(50)，NOT NULL）、deleted（TINYINT，NOT NULL，DEFAULT 0）、version（INT，NOT NULL，DEFAULT 0）
  - 创建主键索引：PRIMARY KEY (id)
  - 创建唯一索引：UNIQUE KEY uk_name (name)
  - 创建普通索引：KEY idx_type (type)
  - 创建普通索引：KEY idx_deleted (deleted)
  - 设置字符集 UTF8MB4，存储引擎 InnoDB
  - 添加表注释和字段注释
  - _需求：12.1-12.8_
  - **验收标准**：
    - 检查 schema.sql 文件存在于 mysql-impl/resources/db 目录
    - 检查使用了 CREATE TABLE IF NOT EXISTS 语法
    - 检查定义了所有 11 个字段，字段类型和约束正确
    - 检查 deleted 字段有 DEFAULT 0
    - 检查 version 字段有 DEFAULT 0
    - 检查创建了主键索引 PRIMARY KEY (id)
    - 检查创建了唯一索引 uk_name (name)
    - 检查创建了普通索引 idx_type (type) 和 idx_deleted (deleted)
    - 检查设置了字符集 UTF8MB4 和存储引擎 InnoDB
    - 检查有表注释和字段注释

- [x] 12. 编写集成测试
  - [x] 12.1 创建测试类和基本 CRUD 测试
    - 在 `bootstrap/src/test/java/com/demo/bootstrap/repository/` 创建 NodeRepositoryImplTest 类
    - 添加 `@SpringBootTest` 注解
    - 添加 `@ActiveProfiles("local")` 注解
    - 添加 `@Transactional` 注解（测试后自动回滚）
    - 注入 NodeRepository 接口
    - 编写 testSave 测试：创建节点，验证 ID 自动生成、时间自动填充、createBy/updateBy 正确、deleted 默认 0、version 默认 0
    - 编写 testFindById 测试：保存节点后查询，验证查询成功、字段值正确；查询不存在的 ID，验证返回 null
    - 编写 testUpdate 测试：保存节点后更新，验证更新成功、updateTime 自动更新、updateBy 正确、version 自动增加
    - 编写 testDeleteById 测试：保存节点后删除，验证 deleted 设置为 1；再次查询，验证不返回已删除的节点
    - _需求：13.1-13.7, 13.11-13.12_
  - [x] 12.2 编写条件查询和分页测试
    - 编写 testFindByName 测试：保存节点后按名称查询，验证查询成功、字段值正确；查询不存在的名称，验证返回 null
    - 编写 testFindByType 测试：保存多个不同类型的节点，按类型查询，验证查询成功、返回列表、按 createTime 降序排序
    - 编写 testFindPage 测试：保存多个节点，分页查询，验证分页参数正确（current、size、total、pages）、数据列表正确；测试名称和类型过滤
    - _需求：13.8-13.10_
  - [x] 12.3 编写唯一约束和乐观锁测试
    - 编写 testUniqueConstraint 测试：保存节点后，尝试创建名称重复的节点，验证抛出异常；更新节点名称为已存在的名称，验证抛出异常
    - 编写 testOptimisticLock 测试：保存节点后，查询两次得到两个对象，更新第一个对象成功，更新第二个对象，验证抛出异常（乐观锁冲突）
    - _需求：13.13-13.14_
  - **验收标准**：
    - 执行 `mvn test` 成功，所有测试通过
    - 检查 NodeRepositoryImplTest 类存在于 bootstrap/src/test 目录
    - 检查有 @SpringBootTest、@ActiveProfiles("local")、@Transactional 注解
    - 检查注入了 NodeRepository 接口
    - 检查有 testSave、testFindById、testUpdate、testDeleteById 测试方法
    - 检查有 testFindByName、testFindByType、testFindPage 测试方法
    - 检查有 testUniqueConstraint、testOptimisticLock 测试方法
    - 检查每个测试方法都有断言验证预期结果

- [x] 13. 项目构建和启动验证
  - [x] 13.1 执行编译验证
    - 执行 `mvn clean compile` 命令
    - 验证编译成功，无错误和警告
    - 验证所有模块都成功编译
    - _需求：14.1_
  - [x] 13.2 执行打包验证
    - 执行 `mvn clean package` 命令
    - 验证打包成功，生成可执行 JAR 文件
    - 检查 bootstrap/target 目录下生成了 JAR 文件
    - _需求：14.2_
  - [x] 13.3 执行应用启动验证
    - 启动应用（使用 local 环境配置）
    - 验证应用成功启动，无异常日志
    - 检查日志中显示 MyBatis-Plus 初始化成功信息
    - 检查日志中显示数据库连接成功信息
    - 检查日志中显示 Mapper 扫描成功信息（扫描到 NodeMapper）
    - 验证 Druid 连接池正常工作
    - _需求：14.3-14.6_
  - **验收标准**：
    - `mvn clean compile` 执行成功，无错误和警告
    - `mvn clean package` 执行成功，生成 JAR 文件
    - 应用成功启动，无异常日志
    - 日志中包含 "MyBatis-Plus" 相关初始化信息
    - 日志中包含数据库连接成功信息
    - 日志中包含 Mapper 扫描信息，显示扫描到 NodeMapper
    - 日志中包含 Druid 连接池初始化信息

## 任务执行说明

### 执行顺序
任务按照编号顺序执行，每个任务完成后项目都应该可以成功构建。子任务也应按照编号顺序执行。

### 验证方法
- **任务 1-3**：通过构建验证（mvn clean compile）+ 代码检查
- **任务 4-11**：通过构建验证（mvn clean compile）+ 代码检查
- **任务 12**：通过测试执行验证（mvn test）
- **任务 13**：通过应用启动验证

### 可选任务说明
- 标记为 `*` 的任务为可选任务（如任务 12 的集成测试）
- 可选任务不影响核心功能，可以根据项目需要决定是否执行
- 建议在核心功能完成后执行可选任务

### 验收标准说明
每个任务都有明确的验收标准，包括：
- **构建验证**：确保项目可以成功编译
- **代码检查**：确认文件存在、注解正确、方法签名正确
- **配置检查**：确认配置项存在且值正确
- **功能验证**：通过测试或启动验证功能正常

## 任务依赖关系

```
任务 1（依赖管理）
  ├─ 1.1 配置父 POM
  └─ 1.2 配置 mysql-impl 模块
  ↓
任务 2（MyBatis-Plus 配置）
  ├─ 2.1 MybatisPlusConfig
  └─ 2.2 CustomMetaObjectHandler
  ↓
任务 3（应用配置）
  ├─ 3.1 application.yml
  └─ 3.2 多环境配置
  ↓
任务 4（PageResult）
  ↓
任务 5（NodeEntity）
  ↓
任务 6（NodeRepository 接口）
  ↓
任务 7（NodePO）
  ↓
任务 8（NodeMapper 接口）
  ↓
任务 9（NodeMapper.xml）
  ↓
任务 10（NodeRepositoryImpl）
  ├─ 10.1 类和转换方法
  ├─ 10.2 save 方法
  ├─ 10.3 update 方法
  ├─ 10.4 查询方法
  └─ 10.5 deleteById 方法
  ↓
任务 11（数据库表）
  ↓
任务 12（集成测试）*
  ├─ 12.1 基本 CRUD 测试
  ├─ 12.2 条件查询和分页测试
  └─ 12.3 唯一约束和乐观锁测试
  ↓
任务 13（构建和启动验证）
  ├─ 13.1 编译验证
  ├─ 13.2 打包验证
  └─ 13.3 启动验证
```

## 注意事项

1. **渐进式开发**：每完成一个任务，立即执行构建验证，确保项目处于健康状态
2. **依赖顺序**：严格按照任务顺序执行，不要跳过前置任务
3. **测试策略**：任务 9 为可选测试任务，建议在核心功能完成后执行
4. **配置管理**：任务 2 需要用户提供数据库连接信息，local 环境可以使用占位符
5. **代码质量**：遵循项目的 MyBatis-Plus 最佳实践，所有条件查询在 XML 中定义
