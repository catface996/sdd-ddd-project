# MyBatis-Plus 集成与 NodeEntity 实现需求文档

## 引言

本文档定义了在现有 DDD 分层架构的多模块 Maven 工程中集成 MyBatis-Plus ORM 框架，并实现第一个业务实体 NodeEntity 的持久化功能的需求。

项目已完成基础架构搭建，包括多模块结构、日志与追踪体系、异常处理机制和多环境配置。现在需要引入数据持久化能力，为业务功能开发奠定基础。

## 术语表

- **MyBatis-Plus**：MyBatis 的增强工具，提供强大的 CRUD 操作能力
- **ORM**：对象关系映射（Object-Relational Mapping），用于在对象模型和关系数据库之间建立映射
- **NodeEntity**：系统节点实体，用于管理系统中的各类节点（数据库、应用、API、报表等）及其关系
- **Repository**：仓储层，负责数据访问和持久化操作
- **PO**：持久化对象（Persistent Object），数据库表的直接映射对象
- **Entity**：领域实体，业务层使用的纯 POJO 对象
- **Mapper**：MyBatis 的数据访问接口
- **乐观锁**：通过版本号机制防止并发更新冲突
- **逻辑删除**：标记删除而非物理删除数据

## 需求

### 需求 1：MyBatis-Plus 框架集成

**用户故事**：作为开发人员，我希望在项目中集成 MyBatis-Plus 框架，以便使用其强大的 ORM 能力进行数据持久化操作。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 成功编译并打包，无依赖冲突错误
2. WHEN 应用启动时 THEN THE System SHALL 成功初始化 MyBatis-Plus，日志中显示 MyBatis-Plus 相关配置加载成功
3. THE System SHALL 在父 POM 的 properties 中定义 mybatis-plus.version 为 3.5.7
4. THE System SHALL 在父 POM 的 properties 中定义 druid.version 为 1.2.20
5. THE System SHALL 在父 POM 的 dependencyManagement 中声明 com.baomidou:mybatis-plus-spring-boot3-starter 依赖，版本使用 ${mybatis-plus.version}
6. THE System SHALL 在父 POM 的 dependencyManagement 中声明 com.alibaba:druid-spring-boot-starter 依赖，版本使用 ${druid.version}
7. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 POM 中引入 mybatis-plus-spring-boot3-starter 依赖，不指定版本号
8. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 POM 中引入 druid-spring-boot-starter 依赖，不指定版本号
9. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 POM 中引入 mysql-connector-j 依赖，scope 为 runtime，不指定版本号
10. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 POM 中引入 lombok 依赖，scope 为 provided，不指定版本号
11. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 POM 中引入 repository-api 模块依赖，不指定版本号
12. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 POM 中引入 common 模块依赖，不指定版本号

### 需求 2：MyBatis-Plus 配置

**用户故事**：作为开发人员，我希望配置 MyBatis-Plus 的核心功能，包括分页、乐观锁和防全表操作，以确保数据操作的安全性和高效性。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 config 包中创建 MybatisPlusConfig 配置类
2. THE MybatisPlusConfig SHALL 使用 @Configuration 注解标记为配置类
3. THE MybatisPlusConfig SHALL 使用 @MapperScan 注解扫描 com.demo.infrastructure.repository.mysql.mapper 包
4. THE MybatisPlusConfig SHALL 注册 MybatisPlusInterceptor 拦截器
5. THE MybatisPlusInterceptor SHALL 按顺序注册以下插件：分页插件（第一位）、乐观锁插件、防全表更新删除插件
6. THE 分页插件 SHALL 配置数据库类型为 MySQL，单页最大数量限制为 100 条，溢出处理设置为 false
7. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 config 包中创建 CustomMetaObjectHandler 类
8. THE CustomMetaObjectHandler SHALL 实现 MetaObjectHandler 接口
9. THE CustomMetaObjectHandler SHALL 使用 @Component 注解注册为 Spring Bean
10. WHEN 执行插入操作时 THEN THE CustomMetaObjectHandler SHALL 自动填充 createTime 和 updateTime 字段为当前时间
11. WHEN 执行更新操作时 THEN THE CustomMetaObjectHandler SHALL 自动填充 updateTime 字段为当前时间

### 需求 3：应用配置

**用户故事**：作为开发人员，我希望在应用配置文件中配置 MyBatis-Plus 的全局设置，以便统一管理数据访问行为。

#### 验收标准

1. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置 mapper-locations 为 classpath*:/mapper/**/*.xml
2. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置 type-aliases-package 为 com.demo.infrastructure.repository.mysql.po
3. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置逻辑删除字段 logic-delete-field 为 deleted
4. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置逻辑删除值 logic-delete-value 为 1
5. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置逻辑未删除值 logic-not-delete-value 为 0
6. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置驼峰命名转换 map-underscore-to-camel-case 为 true
7. THE System SHALL 在 bootstrap/src/main/resources/application.yml 中配置日志实现 log-impl 为 org.apache.ibatis.logging.slf4j.Slf4jImpl

### 需求 4：多环境数据源配置

**用户故事**：作为开发人员，我希望为不同环境（local、dev、test、staging、prod）配置独立的数据源，以便在不同环境中使用不同的数据库。

#### 验收标准

1. THE System SHALL 为 local 环境创建配置文件 application-local.yml，位于 bootstrap/src/main/resources/ 目录
2. THE System SHALL 为 dev 环境创建配置文件 application-dev.yml，位于 bootstrap/src/main/resources/ 目录
3. THE System SHALL 为 test 环境创建配置文件 application-test.yml，位于 bootstrap/src/main/resources/ 目录
4. THE System SHALL 为 staging 环境创建配置文件 application-staging.yml，位于 bootstrap/src/main/resources/ 目录
5. THE System SHALL 为 prod 环境创建配置文件 application-prod.yml，位于 bootstrap/src/main/resources/ 目录
6. WHEN 配置环境数据源时 THEN THE System SHALL 包含 spring.datasource.url、spring.datasource.username、spring.datasource.password、spring.datasource.driver-class-name、spring.datasource.type 配置项
7. THE System SHALL 配置 spring.datasource.driver-class-name 为 com.mysql.cj.jdbc.Driver
8. THE System SHALL 配置 spring.datasource.type 为 com.alibaba.druid.pool.DruidDataSource
9. WHEN 配置 Druid 连接池时 THEN THE System SHALL 包含 spring.datasource.druid.initial-size、spring.datasource.druid.min-idle、spring.datasource.druid.max-active、spring.datasource.druid.max-wait 配置项
10. THE local 环境配置 SHALL 使用占位符或实际值配置数据库连接信息（需用户提供）

### 需求 5：通用分页结果类

**用户故事**：作为开发人员，我希望有一个通用的分页结果封装类，以便在各层之间传递分页数据。

#### 验收标准

1. THE System SHALL 在 common/src/main/java/com/demo/common/dto/ 目录下创建 PageResult 类
2. THE PageResult SHALL 是泛型类 PageResult&lt;T&gt;
3. THE PageResult SHALL 实现 Serializable 接口
4. THE PageResult SHALL 包含以下字段：current（Long 类型，当前页）、size（Long 类型，每页大小）、total（Long 类型，总记录数）、pages（Long 类型，总页数）、records（List&lt;T&gt; 类型，数据列表）
5. THE PageResult SHALL 提供全参构造方法和无参构造方法
6. THE PageResult SHALL 提供 convert 方法，接收 Function&lt;T, R&gt; 转换函数作为参数，返回 PageResult&lt;R&gt; 类型
7. THE PageResult SHALL 使用 Lombok 的 @Data 注解生成 getter、setter、toString、equals、hashCode 方法

### 需求 6：NodeEntity 领域实体定义

**用户故事**：作为开发人员，我希望定义 NodeEntity 领域实体，用于表示系统中的节点概念，不依赖任何持久化框架。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/repository-api 模块的 entity 包中创建 NodeEntity 类
2. THE NodeEntity SHALL 是纯 POJO 类，不包含任何框架特定注解
3. THE NodeEntity SHALL 实现 Serializable 接口
4. THE NodeEntity SHALL 包含以下字段：id（Long）、name（String）、type（String）、description（String）、properties（String）、createTime（LocalDateTime）、updateTime（LocalDateTime）、createBy（String）、updateBy（String）、deleted（Integer）、version（Integer）
5. THE NodeEntity SHALL 使用 Lombok 的 @Data 注解生成 getter、setter、toString、equals、hashCode 方法

### 需求 7：NodePO 持久化对象定义

**用户故事**：作为开发人员，我希望定义 NodePO 持久化对象，用于与数据库表 t_node 进行映射，包含所有 MyBatis-Plus 注解。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 po 包中创建 NodePO 类
2. THE NodePO SHALL 使用 @TableName 注解指定表名为 t_node
3. THE NodePO SHALL 使用 @TableId 注解标记 id 字段，主键类型为 IdType.ASSIGN_ID
4. THE NodePO SHALL 使用 @TableField 注解标记 createTime 字段，填充策略为 FieldFill.INSERT
5. THE NodePO SHALL 使用 @TableField 注解标记 updateTime 字段，填充策略为 FieldFill.INSERT_UPDATE
6. THE NodePO SHALL 使用 @TableLogic 注解标记 deleted 字段
7. THE NodePO SHALL 使用 @Version 注解标记 version 字段
8. THE NodePO SHALL 包含与 NodeEntity 相同的所有字段

### 需求 8：NodeRepository 仓储接口定义

**用户故事**：作为开发人员，我希望定义 NodeRepository 仓储接口，提供节点数据访问的契约，不依赖任何持久化框架。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/repository-api 模块的 api 包中创建 NodeRepository 接口
2. THE NodeRepository SHALL 定义 save 方法，参数为 NodeEntity entity 和 String operator，返回 void
3. THE NodeRepository SHALL 定义 update 方法，参数为 NodeEntity entity 和 String operator，返回 void
4. THE NodeRepository SHALL 定义 findById 方法，参数为 Long id，返回 NodeEntity
5. THE NodeRepository SHALL 定义 findByName 方法，参数为 String name，返回 NodeEntity
6. THE NodeRepository SHALL 定义 findByType 方法，参数为 String type，返回 List&lt;NodeEntity&gt;
7. THE NodeRepository SHALL 定义 findPage 方法，参数为 Integer current、Integer size、String name、String type，返回 PageResult&lt;NodeEntity&gt;
8. THE NodeRepository SHALL 定义 deleteById 方法，参数为 Long id 和 String operator，返回 void

### 需求 9：NodeMapper 接口定义

**用户故事**：作为开发人员，我希望定义 NodeMapper 接口，继承 MyBatis-Plus 的 BaseMapper，并添加自定义查询方法。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 mapper 包中创建 NodeMapper 接口
2. THE NodeMapper SHALL 使用 @Mapper 注解标记为 MyBatis Mapper
3. THE NodeMapper SHALL 继承 BaseMapper&lt;NodePO&gt;
4. THE NodeMapper SHALL 定义 selectByName 方法，参数为 @Param("name") String name，返回 NodePO
5. THE NodeMapper SHALL 定义 selectByType 方法，参数为 @Param("type") String type，返回 List&lt;NodePO&gt;
6. THE NodeMapper SHALL 定义 selectPageByCondition 方法，参数为 Page&lt;?&gt; page、@Param("name") String name、@Param("type") String type，返回 IPage&lt;NodePO&gt;

### 需求 10：NodeMapper XML 配置

**用户故事**：作为开发人员，我希望在 Mapper XML 文件中定义所有条件查询的 SQL 语句，以便统一管理和优化。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/mysql-impl/src/main/resources/mapper/ 目录下创建 NodeMapper.xml 文件
2. THE NodeMapper.xml SHALL 配置 namespace 为 com.demo.infrastructure.repository.mysql.mapper.NodeMapper
3. THE NodeMapper.xml SHALL 定义 BaseResultMap，type 为 com.demo.infrastructure.repository.mysql.po.NodePO
4. THE BaseResultMap SHALL 映射所有字段：id、name、type、description、properties、createTime、updateTime、createBy、updateBy、deleted、version
5. THE NodeMapper.xml SHALL 定义 selectByName 查询，使用 BaseResultMap，包含 deleted = 0 条件
6. THE NodeMapper.xml SHALL 定义 selectByType 查询，使用 BaseResultMap，包含 deleted = 0 条件，按 createTime 降序排序
7. THE NodeMapper.xml SHALL 定义 selectPageByCondition 查询，使用 BaseResultMap，支持名称模糊查询和类型精确查询，包含 deleted = 0 条件，按 createTime 降序排序

### 需求 11：NodeRepositoryImpl 实现类

**用户故事**：作为开发人员，我希望实现 NodeRepository 接口，提供节点数据访问的具体实现，负责 Entity 和 PO 之间的转换。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/mysql-impl 模块的 impl 包中创建 NodeRepositoryImpl 类
2. THE NodeRepositoryImpl SHALL 使用 @Repository 注解标记为仓储实现
3. THE NodeRepositoryImpl SHALL 实现 NodeRepository 接口
4. THE NodeRepositoryImpl SHALL 注入 NodeMapper 依赖
5. WHEN 调用 save 方法时 THEN THE NodeRepositoryImpl SHALL 转换 Entity 为 PO，设置 createBy 和 updateBy，如果 deleted 为 null 则设置为 0，如果 version 为 null 则设置为 0，调用 Mapper.insert，回填生成的 ID 和时间戳到 Entity
6. WHEN 调用 update 方法时 THEN THE NodeRepositoryImpl SHALL 转换 Entity 为 PO，设置 updateBy，将 updateTime 设置为 null（让自动填充生效），调用 Mapper.updateById，回填更新后的时间戳和版本号到 Entity
7. WHEN 调用 findById 方法时 THEN THE NodeRepositoryImpl SHALL 调用 Mapper.selectById，IF PO 为 null THEN 返回 null，否则转换 PO 为 Entity 并返回
8. WHEN 调用 findByName 方法时 THEN THE NodeRepositoryImpl SHALL 调用 Mapper.selectByName，IF PO 为 null THEN 返回 null，否则转换 PO 为 Entity 并返回
9. WHEN 调用 findByType 方法时 THEN THE NodeRepositoryImpl SHALL 调用 Mapper.selectByType，转换 PO 列表为 Entity 列表并返回
10. WHEN 调用 findPage 方法时 THEN THE NodeRepositoryImpl SHALL 创建 Page 对象，调用 Mapper.selectPageByCondition，转换 IPage&lt;NodePO&gt; 为 PageResult&lt;NodeEntity&gt; 并返回
11. WHEN 调用 deleteById 方法时 THEN THE NodeRepositoryImpl SHALL 先调用 Mapper.selectById 查询 PO，IF PO 不为 null THEN 设置 updateBy，调用 Mapper.deleteById 执行逻辑删除
12. THE NodeRepositoryImpl SHALL 实现私有方法 toEntity(NodePO po) 和 toPO(NodeEntity entity)，用于 Entity 和 PO 之间的字段复制转换

### 需求 12：数据库表创建

**用户故事**：作为开发人员，我希望有数据库初始化脚本来创建 t_node 表，用于存储节点数据。

#### 验收标准

1. THE System SHALL 在 infrastructure/repository/mysql-impl/src/main/resources/db/ 目录下创建 schema.sql 文件
2. THE schema.sql SHALL 包含创建 t_node 表的 DDL 语句
3. THE t_node 表 SHALL 使用字符集 UTF8MB4，存储引擎 InnoDB
4. THE t_node 表 SHALL 包含以下字段：id（BIGINT，主键，NOT NULL，COMMENT '主键ID'）、name（VARCHAR(100)，NOT NULL，COMMENT '节点名称'）、type（VARCHAR(50)，NOT NULL，COMMENT '节点类型'）、description（VARCHAR(500)，COMMENT '节点描述'）、properties（TEXT，COMMENT '节点属性JSON'）、create_time（DATETIME，NOT NULL，COMMENT '创建时间'）、update_time（DATETIME，NOT NULL，COMMENT '更新时间'）、create_by（VARCHAR(50)，NOT NULL，COMMENT '创建人'）、update_by（VARCHAR(50)，NOT NULL，COMMENT '更新人'）、deleted（TINYINT，NOT NULL，DEFAULT 0，COMMENT '逻辑删除标记'）、version（INT，NOT NULL，DEFAULT 0，COMMENT '版本号'）
5. THE t_node 表 SHALL 在 name 字段上创建唯一索引 uk_name
6. THE t_node 表 SHALL 在 type 字段上创建普通索引 idx_type
7. THE t_node 表 SHALL 在 deleted 字段上创建普通索引 idx_deleted
8. THE schema.sql SHALL 使用 IF NOT EXISTS 语法，避免重复创建表时报错

### 需求 13：集成测试

**用户故事**：作为开发人员，我希望编写集成测试，验证 NodeRepository 的所有功能是否正常工作。

#### 验收标准

1. THE System SHALL 在 bootstrap/src/test/java/com/demo/bootstrap/repository/ 目录下创建 NodeRepositoryImplTest 测试类
2. THE NodeRepositoryImplTest SHALL 使用 @SpringBootTest 注解加载完整的 Spring 上下文
3. THE NodeRepositoryImplTest SHALL 使用 @ActiveProfiles("local") 注解激活 local 环境配置
4. THE NodeRepositoryImplTest SHALL 使用 @Transactional 注解确保测试后自动回滚
5. THE NodeRepositoryImplTest SHALL 注入 NodeRepository 接口
6. WHEN 执行 testSave 测试时 THEN THE System SHALL 验证 ID 自动生成、时间自动填充、createBy/updateBy 正确、deleted 默认 0、version 默认 0
7. WHEN 执行 testFindById 测试时 THEN THE System SHALL 验证查询成功、字段值正确、查询不存在的 ID 返回 null
8. WHEN 执行 testFindByName 测试时 THEN THE System SHALL 验证查询成功、字段值正确、查询不存在的名称返回 null
9. WHEN 执行 testFindByType 测试时 THEN THE System SHALL 验证查询成功、返回列表、按 createTime 降序排序
10. WHEN 执行 testFindPage 测试时 THEN THE System SHALL 验证分页参数正确、总记录数正确、数据列表正确、支持名称和类型过滤
11. WHEN 执行 testUpdate 测试时 THEN THE System SHALL 验证更新成功、updateTime 自动更新、updateBy 正确、version 自动增加
12. WHEN 执行 testDeleteById 测试时 THEN THE System SHALL 验证 deleted 设置为 1、查询时不返回已删除的节点
13. WHEN 执行 testUniqueConstraint 测试时 THEN THE System SHALL 验证创建名称重复的节点抛出异常、更新节点名称为已存在的名称抛出异常
14. WHEN 执行 testOptimisticLock 测试时 THEN THE System SHALL 验证并发更新同一节点时，后更新的操作抛出异常

### 需求 14：项目构建验证

**用户故事**：作为开发人员，我希望确保项目可以成功构建和启动，所有配置正确无误。

#### 验收标准

1. WHEN 执行 mvn clean compile 命令时 THEN THE System SHALL 成功编译，无错误和警告
2. WHEN 执行 mvn clean package 命令时 THEN THE System SHALL 成功打包，生成可执行 JAR 文件
3. WHEN 启动应用时 THEN THE System SHALL 成功启动，无异常日志
4. WHEN 启动应用时 THEN THE System SHALL 在日志中显示 MyBatis-Plus 初始化成功信息
5. WHEN 启动应用时 THEN THE System SHALL 成功连接数据库，连接池正常工作
6. WHEN 启动应用时 THEN THE System SHALL 在日志中显示 Mapper 扫描成功信息
