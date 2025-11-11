# MyBatis-Plus 集成与 NodeEntity 实现需求文档

## 引言

本文档定义了 OrderCore 系统集成 MyBatis-Plus ORM 框架并实现 NodeEntity 持久化功能的需求。NodeEntity 是系统中用于建立和管理系统节点关系的核心实体，用于记录数据库、业务应用、API 接口、报表系统等节点及其依赖关系。

## 术语表

- **MyBatis-Plus**：MyBatis 的增强工具，提供强大的 CRUD 操作和代码生成能力
- **NodeEntity**：系统节点实体，用于记录和管理系统中的各类节点（数据库、应用、API 等）
- **Repository**：仓储层，负责数据持久化操作
- **Mapper**：MyBatis-Plus 的数据访问接口
- **逻辑删除**：标记删除而非物理删除数据
- **乐观锁**：通过版本号机制防止并发更新冲突
- **自动填充**：框架自动填充创建时间、更新时间等字段
- **Druid**：阿里巴巴开源的数据库连接池，提供监控、防火墙等功能
- **Operator**：操作人标识，用于记录数据的创建人和更新人

## 需求

### 需求 1：MyBatis-Plus 依赖配置

**用户故事：** 作为开发人员，我希望在项目中正确配置 MyBatis-Plus 和 Druid 依赖，以便使用其提供的 ORM 功能和连接池功能。

#### 验收标准

1. THE OrderCore SHALL 在父 POM 的 dependencyManagement 中声明 MyBatis-Plus Boot Starter 版本为 3.5.5
2. THE OrderCore SHALL 在父 POM 的 dependencyManagement 中声明 MySQL Connector/J 版本
3. THE OrderCore SHALL 在父 POM 的 dependencyManagement 中声明 Druid Spring Boot Starter 版本
4. THE OrderCore SHALL 在 mysql-impl 模块中添加 MyBatis-Plus Boot Starter 依赖，不指定版本
5. THE OrderCore SHALL 在 mysql-impl 模块中添加 MySQL Connector/J 依赖，不指定版本
6. THE OrderCore SHALL 在 mysql-impl 模块中添加 Druid Spring Boot Starter 依赖，不指定版本
7. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 mysql-impl 模块

### 需求 2：MyBatis-Plus 基础配置

**用户故事：** 作为开发人员，我希望配置 MyBatis-Plus 的基础功能，以便支持分页、乐观锁、逻辑删除等特性。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.config 包中创建 MybatisPlusConfig 配置类
2. THE OrderCore SHALL 配置分页插件，支持 MySQL 数据库类型
3. THE OrderCore SHALL 配置分页插件单页最大数量为 100
4. THE OrderCore SHALL 配置乐观锁插件，支持版本号并发控制
5. THE OrderCore SHALL 配置防全表更新删除插件，防止误操作
6. THE OrderCore SHALL 配置元数据自动填充处理器，自动填充创建时间、更新时间（createBy 和 updateBy 通过方法参数传递，不使用自动填充）
7. THE OrderCore SHALL 在配置类上使用 @MapperScan 注解，扫描包路径为 com.demo.ordercore.infrastructure.repository.mysql.mapper
8. WHEN 应用启动时 THEN THE OrderCore SHALL 在日志中输出 MyBatis-Plus 初始化成功信息

### 需求 3：数据源配置

**用户故事：** 作为开发人员，我希望配置多环境数据源，以便在不同环境中连接不同的数据库。

#### 验收标准

1. THE OrderCore SHALL 在 application.yml 中配置数据源基础属性（驱动类名为 com.mysql.cj.jdbc.Driver，连接池类型为 Druid）
2. THE OrderCore SHALL 在 application-local.yml 中配置本地数据库连接信息（URL、用户名、密码）
3. THE OrderCore SHALL 在 application-dev.yml 中配置开发环境数据库连接信息（URL、用户名、密码）
4. THE OrderCore SHALL 在 application-test.yml 中配置测试环境数据库连接信息（URL、用户名、密码）
5. THE OrderCore SHALL 在 application-staging.yml 中配置预发布环境数据库连接信息（URL、用户名、密码）
6. THE OrderCore SHALL 在 application-prod.yml 中配置生产环境数据库连接信息（URL、用户名、密码）
7. THE OrderCore SHALL 配置 Druid 连接池参数（初始化连接数、最小空闲连接数、最大活跃连接数、连接超时时间等）
8. THE OrderCore SHALL 配置 Druid 监控统计功能（stat-view-servlet）
9. THE OrderCore SHALL 配置 Druid SQL 防火墙功能（wall filter）
10. WHEN 应用使用 local profile 启动时 THEN THE OrderCore SHALL 成功连接到本地数据库

### 需求 4：MyBatis-Plus 全局配置

**用户故事：** 作为开发人员，我希望配置 MyBatis-Plus 的全局参数，以便统一管理表名前缀、主键策略、逻辑删除等。

#### 验收标准

1. THE OrderCore SHALL 在 application.yml 中配置 Mapper XML 文件位置为 classpath*:/mapper/**/*.xml
2. THE OrderCore SHALL 在 application.yml 中配置实体类包路径为 com.demo.ordercore.infrastructure.repository.mysql.po
3. THE OrderCore SHALL 在 application.yml 中配置表名前缀为 t_
4. THE OrderCore SHALL 在 application.yml 中配置主键类型为 ASSIGN_ID（雪花算法）
5. THE OrderCore SHALL 在 application.yml 中配置逻辑删除字段为 deleted
6. THE OrderCore SHALL 在 application.yml 中配置逻辑删除值为 1，未删除值为 0
7. THE OrderCore SHALL 在 application.yml 中配置驼峰命名转换为 true
8. WHEN 应用启动时 THEN THE OrderCore SHALL 加载 MyBatis-Plus 全局配置

### 需求 5：NodeEntity 领域实体定义

**用户故事：** 作为开发人员，我希望定义 NodeEntity 领域实体类，作为纯 POJO 不依赖任何持久化框架。

#### 验收标准

1. THE OrderCore SHALL 在 repository-api 模块的 com.demo.ordercore.infrastructure.repository.entity 包中创建 NodeEntity 类
2. THE OrderCore SHALL 定义 id 字段，类型为 Long
3. THE OrderCore SHALL 定义 name 字段，类型为 String，最大长度 100 字符，作为唯一约束字段
4. THE OrderCore SHALL 定义 type 字段，类型为 String，用于存储节点类型（支持扩展，不限制固定值）
5. THE OrderCore SHALL 定义 description 字段，类型为 String，最大长度 500 字符
6. THE OrderCore SHALL 定义 properties 字段，类型为 String，用于存储 JSON 格式的扩展属性
7. THE OrderCore SHALL 定义 createTime 字段，类型为 LocalDateTime
8. THE OrderCore SHALL 定义 updateTime 字段，类型为 LocalDateTime
9. THE OrderCore SHALL 定义 createBy 字段，类型为 String
10. THE OrderCore SHALL 定义 updateBy 字段，类型为 String
11. THE OrderCore SHALL 定义 deleted 字段，类型为 Integer
12. THE OrderCore SHALL 定义 version 字段，类型为 Integer
13. THE OrderCore SHALL 确保 NodeEntity 不包含任何框架特定注解（如 @TableName、@TableId、@TableField 等）
14. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 repository-api 模块

### 需求 5.1：NodePO 持久化对象定义

**用户故事：** 作为开发人员，我希望定义 NodePO 持久化对象，以便映射数据库表结构。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.po 包中创建 NodePO 类
2. THE OrderCore SHALL 使用 @TableName 注解指定表名为 t_node
3. THE OrderCore SHALL 定义 id 字段，类型为 Long，使用 @TableId 注解，主键策略为 ASSIGN_ID
4. THE OrderCore SHALL 定义 name 字段，类型为 String
5. THE OrderCore SHALL 定义 type 字段，类型为 String
6. THE OrderCore SHALL 定义 description 字段，类型为 String
7. THE OrderCore SHALL 定义 properties 字段，类型为 String
8. THE OrderCore SHALL 定义 createTime 字段，类型为 LocalDateTime，使用 @TableField 注解，填充策略为 INSERT
9. THE OrderCore SHALL 定义 updateTime 字段，类型为 LocalDateTime，使用 @TableField 注解，填充策略为 INSERT_UPDATE
10. THE OrderCore SHALL 定义 createBy 字段，类型为 String（不使用自动填充）
11. THE OrderCore SHALL 定义 updateBy 字段，类型为 String（不使用自动填充）
12. THE OrderCore SHALL 定义 deleted 字段，类型为 Integer，使用 @TableLogic 注解
13. THE OrderCore SHALL 定义 version 字段，类型为 Integer，使用 @Version 注解
14. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 mysql-impl 模块

### 需求 6：NodeMapper 接口定义

**用户故事：** 作为开发人员，我希望定义 NodeMapper 接口，以便提供数据访问能力。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.mapper 包中创建 NodeMapper 接口
2. THE OrderCore SHALL 使 NodeMapper 继承 BaseMapper<NodePO>
3. THE OrderCore SHALL 使用 @Mapper 注解标记 NodeMapper
4. THE OrderCore SHALL 在 NodeMapper 中定义 selectByName 方法，接收 name 参数，返回 NodePO
5. THE OrderCore SHALL 在 NodeMapper 中定义 selectByType 方法，接收 type 参数，返回 List<NodePO>
6. THE OrderCore SHALL 在 NodeMapper 中定义 selectPageByCondition 方法，接收 Page、name、type 参数，返回 IPage<NodePO>
7. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 mysql-impl 模块

### 需求 7：NodeMapper XML 实现

**用户故事：** 作为开发人员，我希望在 Mapper XML 中实现复杂查询，以便统一管理 SQL 语句。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 resources/mapper 目录下创建 NodeMapper.xml
2. THE OrderCore SHALL 在 NodeMapper.xml 中定义 BaseResultMap，映射所有字段
3. THE OrderCore SHALL 在 NodeMapper.xml 中实现 selectByName 方法，根据名称查询节点
4. THE OrderCore SHALL 在 NodeMapper.xml 中实现 selectByType 方法，根据类型查询节点列表
5. THE OrderCore SHALL 在 NodeMapper.xml 中实现 selectPageByCondition 方法，支持按名称和类型过滤的分页查询
6. THE OrderCore SHALL 确保所有 SQL 语句包含 deleted = 0 条件，只查询未删除的数据
7. WHEN 执行 mvn clean package 命令时 THEN THE OrderCore SHALL 将 Mapper XML 文件打包到 JAR 中

### 需求 8：数据库表创建

**用户故事：** 作为开发人员，我希望创建 t_node 数据库表，以便存储节点数据。

#### 验收标准

1. THE OrderCore SHALL 创建 t_node 表，包含所有 NodeEntity 定义的字段
2. THE OrderCore SHALL 为 id 字段设置为主键，类型为 BIGINT
3. THE OrderCore SHALL 为 name 字段设置为 VARCHAR(100)，NOT NULL，并创建唯一索引
4. THE OrderCore SHALL 为 type 字段设置为 VARCHAR(50)，NOT NULL
5. THE OrderCore SHALL 为 description 字段设置为 VARCHAR(500)
6. THE OrderCore SHALL 为 properties 字段设置为 TEXT
7. THE OrderCore SHALL 为 createTime、updateTime 字段设置为 DATETIME，NOT NULL
8. THE OrderCore SHALL 为 createBy、updateBy 字段设置为 VARCHAR(100)
9. THE OrderCore SHALL 为 deleted 字段设置为 TINYINT，默认值为 0
10. THE OrderCore SHALL 为 version 字段设置为 INT，默认值为 0
11. THE OrderCore SHALL 为 type 字段创建普通索引
12. THE OrderCore SHALL 为 deleted 字段创建普通索引
13. THE OrderCore SHALL 设置表字符集为 UTF8MB4
14. THE OrderCore SHALL 设置表存储引擎为 InnoDB

### 需求 9：NodeRepository 接口定义

**用户故事：** 作为开发人员，我希望定义 NodeRepository 接口，以便在领域层使用仓储模式。

#### 验收标准

1. THE OrderCore SHALL 在 repository-api 模块的 com.demo.ordercore.infrastructure.repository.api 包中创建 NodeRepository 接口
2. THE OrderCore SHALL 在 NodeRepository 中定义 save 方法，接收 NodeEntity 和 operator 参数，返回 void
3. THE OrderCore SHALL 在 NodeRepository 中定义 update 方法，接收 NodeEntity 和 operator 参数，返回 void
4. THE OrderCore SHALL 在 NodeRepository 中定义 findById 方法，接收 id 参数，返回 NodeEntity
5. THE OrderCore SHALL 在 NodeRepository 中定义 findByName 方法，接收 name 参数，返回 NodeEntity
6. THE OrderCore SHALL 在 NodeRepository 中定义 findByType 方法，接收 type 参数，返回 List<NodeEntity>
7. THE OrderCore SHALL 在 NodeRepository 中定义 findPage 方法，接收 page、name、type 参数，返回 IPage<NodeEntity>
8. THE OrderCore SHALL 在 NodeRepository 中定义 deleteById 方法，接收 id 和 operator 参数，返回 void
9. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 repository-api 模块

### 需求 10：NodeRepository 实现类

**用户故事：** 作为开发人员，我希望实现 NodeRepository 接口，以便提供实际的数据访问功能。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 com.demo.ordercore.infrastructure.repository.mysql.impl 包中创建 NodeRepositoryImpl 类
2. THE OrderCore SHALL 使 NodeRepositoryImpl 实现 NodeRepository 接口
3. THE OrderCore SHALL 使用 @Repository 注解标记 NodeRepositoryImpl
4. THE OrderCore SHALL 在 NodeRepositoryImpl 中注入 NodeMapper
5. THE OrderCore SHALL 实现 save 方法，接收 entity 和 operator 参数，将 NodeEntity 转换为 NodePO，设置 operator 到 createBy 和 updateBy 字段，调用 NodeMapper 的 insert 方法
6. THE OrderCore SHALL 实现 update 方法，接收 entity 和 operator 参数，将 NodeEntity 转换为 NodePO，设置 operator 到 updateBy 字段，调用 NodeMapper 的 updateById 方法
7. THE OrderCore SHALL 实现 findById 方法，调用 NodeMapper 的 selectById 方法，将 NodePO 转换为 NodeEntity 返回
8. THE OrderCore SHALL 实现 findByName 方法，调用 NodeMapper 的 selectByName 方法，将 NodePO 转换为 NodeEntity 返回
9. THE OrderCore SHALL 实现 findByType 方法，调用 NodeMapper 的 selectByType 方法，将 List<NodePO> 转换为 List<NodeEntity> 返回
10. THE OrderCore SHALL 实现 findPage 方法，调用 NodeMapper 的 selectPageByCondition 方法，将 IPage<NodePO> 转换为 IPage<NodeEntity> 返回
11. THE OrderCore SHALL 实现 deleteById 方法，接收 id 和 operator 参数，先查询 NodePO，设置 operator 到 updateBy 字段，调用 NodeMapper 的 deleteById 方法（逻辑删除）
12. THE OrderCore SHALL 在 NodeRepositoryImpl 中实现 Entity 和 PO 之间的转换方法（toEntity、toPO）
13. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 mysql-impl 模块

### 需求 11：功能验证 - 创建节点

**用户故事：** 作为开发人员，我希望能够创建节点记录，以便存储系统节点信息。

#### 验收标准

1. WHEN 调用 NodeRepository 的 save 方法（传入 entity 和 operator）创建节点时 THEN THE OrderCore SHALL 在数据库中插入一条记录
2. WHEN 创建节点时 THEN THE OrderCore SHALL 自动生成 id（雪花算法）
3. WHEN 创建节点时 THEN THE OrderCore SHALL 自动填充 createTime 为当前时间
4. WHEN 创建节点时 THEN THE OrderCore SHALL 自动填充 updateTime 为当前时间
5. WHEN 创建节点时 THEN THE OrderCore SHALL 将 createBy 设置为传入的 operator 参数值
6. WHEN 创建节点时 THEN THE OrderCore SHALL 将 updateBy 设置为传入的 operator 参数值
7. WHEN 创建节点时 THEN THE OrderCore SHALL 设置 deleted 为 0
8. WHEN 创建节点时 THEN THE OrderCore SHALL 设置 version 为 0

### 需求 12：功能验证 - 查询节点

**用户故事：** 作为开发人员，我希望能够查询节点记录，以便获取系统节点信息。

#### 验收标准

1. WHEN 调用 NodeRepository 的 findById 方法时 THEN THE OrderCore SHALL 返回指定 ID 的节点
2. WHEN 调用 NodeRepository 的 findByName 方法时 THEN THE OrderCore SHALL 返回指定名称的节点
3. WHEN 调用 NodeRepository 的 findByType 方法时 THEN THE OrderCore SHALL 返回指定类型的所有节点
4. WHEN 调用 NodeRepository 的 findPage 方法时 THEN THE OrderCore SHALL 返回分页结果，包含总记录数、总页数、当前页数据
5. WHEN 查询节点时 THEN THE OrderCore SHALL 只返回未删除的节点（deleted = 0）

### 需求 13：功能验证 - 更新节点

**用户故事：** 作为开发人员，我希望能够更新节点记录，以便修改系统节点信息。

#### 验收标准

1. WHEN 调用 NodeRepository 的 update 方法（传入 entity 和 operator）更新节点时 THEN THE OrderCore SHALL 更新数据库中的记录
2. WHEN 更新节点时 THEN THE OrderCore SHALL 自动更新 updateTime 为当前时间
3. WHEN 更新节点时 THEN THE OrderCore SHALL 将 updateBy 设置为传入的 operator 参数值
4. WHEN 更新节点时 THEN THE OrderCore SHALL 自动增加 version 字段值
5. WHEN 并发更新同一节点时 THEN THE OrderCore SHALL 通过乐观锁机制防止数据冲突

### 需求 14：功能验证 - 删除节点

**用户故事：** 作为开发人员，我希望能够删除节点记录，以便移除不需要的系统节点。

#### 验收标准

1. WHEN 调用 NodeRepository 的 deleteById 方法（传入 id 和 operator）时 THEN THE OrderCore SHALL 执行逻辑删除，设置 deleted = 1
2. WHEN 删除节点时 THEN THE OrderCore SHALL 将 updateBy 设置为传入的 operator 参数值
3. WHEN 删除节点后 THEN THE OrderCore SHALL 在查询时不返回已删除的节点
4. WHEN 删除节点时 THEN THE OrderCore SHALL 保留数据库记录，不执行物理删除

### 需求 15：性能要求

**用户故事：** 作为系统管理员，我希望数据访问操作具有良好的性能，以便支持高并发场景。

#### 验收标准

1. WHEN 执行单表查询时 THEN THE OrderCore SHALL 在 100ms 内返回结果
2. WHEN 执行分页查询时 THEN THE OrderCore SHALL 在 200ms 内返回结果
3. THE OrderCore SHALL 为常用查询字段（name、type、deleted）创建索引
4. THE OrderCore SHALL 配置合理的数据库连接池参数，支持并发访问

### 需求 16：安全要求

**用户故事：** 作为安全管理员，我希望系统能够防止 SQL 注入和误操作，以便保护数据安全。

#### 验收标准

1. THE OrderCore SHALL 使用参数化查询，防止 SQL 注入
2. THE OrderCore SHALL 配置防全表更新删除插件，防止误操作
3. THE OrderCore SHALL 在日志中不输出敏感字段内容
4. THE OrderCore SHALL 使用乐观锁机制，防止并发更新冲突

### 需求 17：可维护性要求

**用户故事：** 作为开发人员，我希望代码具有良好的可维护性，以便后续扩展和维护。

#### 验收标准

1. THE OrderCore SHALL 将所有 SQL 语句集中在 Mapper XML 中管理
2. THE OrderCore SHALL 遵循 MyBatis-Plus 最佳实践规范
3. THE OrderCore SHALL 为所有类和方法添加必要的注释
4. THE OrderCore SHALL 遵循项目的编码规范和命名规范


### 需求 18：唯一性约束验证

**用户故事：** 作为开发人员，我希望系统能够保证节点名称的唯一性，以便避免重复的节点记录。

#### 验收标准

1. THE OrderCore SHALL 在数据库表中为 name 字段创建唯一索引
2. WHEN 尝试创建名称重复的节点时 THEN THE OrderCore SHALL 抛出异常
3. WHEN 尝试更新节点名称为已存在的名称时 THEN THE OrderCore SHALL 抛出异常
4. THE OrderCore SHALL 在异常处理器中捕获唯一约束冲突异常，返回友好的错误信息


### 需求 19：单元测试

**用户故事：** 作为开发人员，我希望为 Repository 实现类编写单元测试，以便验证数据访问功能的正确性。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块中创建测试类 NodeRepositoryImplTest
2. THE OrderCore SHALL 使用 @SpringBootTest 注解标记测试类
3. THE OrderCore SHALL 编写测试方法验证 save 功能
4. THE OrderCore SHALL 编写测试方法验证 findById 功能
5. THE OrderCore SHALL 编写测试方法验证 findByName 功能
6. THE OrderCore SHALL 编写测试方法验证 findByType 功能
7. THE OrderCore SHALL 编写测试方法验证 findPage 功能
8. THE OrderCore SHALL 编写测试方法验证 update 功能
9. THE OrderCore SHALL 编写测试方法验证 deleteById 功能
10. THE OrderCore SHALL 编写测试方法验证唯一约束冲突场景
11. THE OrderCore SHALL 编写测试方法验证乐观锁并发更新场景
12. WHEN 执行 mvn test 命令时 THEN THE OrderCore SHALL 所有测试通过

### 需求 20：数据库初始化脚本

**用户故事：** 作为开发人员，我希望提供数据库初始化脚本，以便快速创建表结构和初始化数据。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 resources 目录下创建 schema.sql 脚本
2. THE OrderCore SHALL 在 schema.sql 中包含创建 t_node 表的 DDL 语句
3. THE OrderCore SHALL 在 schema.sql 中包含创建唯一索引的语句（name 字段）
4. THE OrderCore SHALL 在 schema.sql 中包含创建普通索引的语句（type、deleted 字段）
5. THE OrderCore SHALL 在 schema.sql 中添加 IF NOT EXISTS 判断，避免重复创建
6. THE OrderCore SHALL 在 schema.sql 中添加清晰的注释说明
7. WHEN 执行 schema.sql 脚本时 THEN THE OrderCore SHALL 成功创建 t_node 表及其索引

### 需求 21：JSON 格式验证

**用户故事：** 作为开发人员，我希望系统能够验证 properties 字段的 JSON 格式，以便确保数据的正确性。

#### 验收标准

1. THE OrderCore SHALL 在 NodeEntity 中为 properties 字段添加 JSON 格式验证
2. WHEN 保存或更新节点时 IF properties 字段不为空 THEN THE OrderCore SHALL 验证其是否为有效的 JSON 格式
3. WHEN properties 字段不是有效的 JSON 格式时 THEN THE OrderCore SHALL 抛出业务异常
4. THE OrderCore SHALL 在异常处理器中捕获 JSON 格式验证异常，返回友好的错误信息

### 需求 22：日志配置

**用户故事：** 作为开发人员，我希望配置不同环境的日志级别，以便在开发环境中调试 SQL，在生产环境中减少日志输出。

#### 验收标准

1. THE OrderCore SHALL 在 application-local.yml 中配置 Mapper 包日志级别为 DEBUG（com.demo.ordercore.infrastructure.repository.mysql.mapper）
2. THE OrderCore SHALL 在 application-dev.yml 中配置 Mapper 包日志级别为 DEBUG
3. THE OrderCore SHALL 在 application-test.yml 中配置 Mapper 包日志级别为 INFO
4. THE OrderCore SHALL 在 application-staging.yml 中配置 Mapper 包日志级别为 INFO
5. THE OrderCore SHALL 在 application-prod.yml 中配置 Mapper 包日志级别为 WARN
6. WHEN 应用在 local 或 dev 环境启动时 THEN THE OrderCore SHALL 在日志中输出 SQL 语句
7. WHEN 应用在 test、staging 或 prod 环境启动时 THEN THE OrderCore SHALL 不输出 SQL 语句到日志

### 需求 23：测试数据库配置

**用户故事：** 作为开发人员，我希望配置测试数据库，以便在单元测试中使用实际的 MySQL 数据库。

#### 验收标准

1. THE OrderCore SHALL 在 mysql-impl 模块的 src/test/resources 目录下创建 application-test.yml 配置文件
2. THE OrderCore SHALL 在测试配置文件中配置数据源连接到本地 MySQL 数据库（jdbc:mysql://localhost:3306/ordercore_test）
3. THE OrderCore SHALL 在测试配置文件中配置数据库用户名为 root
4. THE OrderCore SHALL 在测试配置文件中配置数据库密码为 root123
5. THE OrderCore SHALL 在测试类上使用 @Transactional 注解，确保测试完成后自动回滚
6. WHEN 执行 mvn test 命令时 THEN THE OrderCore SHALL 使用测试数据库执行测试

### 需求 24：数据库连接信息配置

**用户故事：** 作为开发人员，我希望为各环境配置数据库连接信息，以便系统能够连接到正确的数据库。

#### 验收标准

1. THE OrderCore SHALL 在 application-local.yml 中配置数据库 URL 为 jdbc:mysql://localhost:3306/ordercore_local?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
2. THE OrderCore SHALL 在 application-local.yml 中配置数据库用户名为 root
3. THE OrderCore SHALL 在 application-local.yml 中配置数据库密码为 root123
4. THE OrderCore SHALL 在 application-dev.yml 中使用占位符配置数据库连接信息（${DB_HOST}、${DB_PORT}、${DB_NAME}、${DB_USERNAME}、${DB_PASSWORD}）
5. THE OrderCore SHALL 在 application-test.yml 中使用占位符配置数据库连接信息
6. THE OrderCore SHALL 在 application-staging.yml 中使用占位符配置数据库连接信息
7. THE OrderCore SHALL 在 application-prod.yml 中使用占位符配置数据库连接信息
8. WHEN 应用启动时 THEN THE OrderCore SHALL 根据当前 profile 加载对应的数据库连接信息

### 需求 25：Druid 连接池详细配置

**用户故事：** 作为开发人员，我希望详细配置 Druid 连接池参数，以便优化数据库连接性能和监控。

#### 验收标准

1. THE OrderCore SHALL 在 application.yml 中配置 Druid 初始化连接数为 5
2. THE OrderCore SHALL 在 application.yml 中配置 Druid 最小空闲连接数为 5
3. THE OrderCore SHALL 在 application.yml 中配置 Druid 最大活跃连接数为 20
4. THE OrderCore SHALL 在 application.yml 中配置 Druid 连接超时时间为 30000ms
5. THE OrderCore SHALL 在 application.yml 中配置 Druid 监控统计功能，访问路径为 /druid/*
6. THE OrderCore SHALL 在 application.yml 中配置 Druid 监控登录用户名和密码
7. THE OrderCore SHALL 在 application.yml 中启用 Druid SQL 防火墙功能（wall filter）
8. THE OrderCore SHALL 在 application.yml 中启用 Druid 统计功能（stat filter）
9. WHEN 应用启动后 THEN THE OrderCore SHALL 可以通过 /druid/* 路径访问 Druid 监控页面
