# MyBatis-Plus 集成与 NodeEntity 实现需求文档

## 引言

本文档定义了在现有 DDD 分层架构的多模块 Maven 工程中集成 MyBatis-Plus ORM 框架，并实现 NodeEntity 业务实体持久化功能的需求。

### 项目背景

当前项目已完成基础架构搭建，包括多模块结构、日志追踪体系、异常处理机制和多环境配置。现需要集成 MyBatis-Plus 3.5.7（Spring Boot 3 专用）作为持久层框架，并实现第一个业务实体 NodeEntity 的完整 CRUD 功能。

### 业务目标

NodeEntity 用于管理企业级应用中的系统节点（数据库、业务应用、API 接口、报表系统等）及其依赖关系和属性信息，为系统架构管理和依赖分析提供数据支持。

## 术语表

- **System**：指本项目的 Spring Boot 应用系统
- **MyBatis-Plus**：基于 MyBatis 的增强 ORM 框架，版本 3.5.7
- **NodeEntity**：节点领域实体，表示系统中的一个节点（纯 POJO，无框架注解）
- **NodePO**：节点持久化对象，数据库表映射对象（包含 MyBatis-Plus 注解）
- **Repository**：仓储接口，定义数据访问契约
- **Mapper**：MyBatis 映射器接口，继承 BaseMapper
- **逻辑删除**：通过标记字段（deleted）标识删除状态，不物理删除数据
- **乐观锁**：通过版本号（version）字段实现并发控制
- **自动填充**：通过 MetaObjectHandler 自动填充时间戳字段
- **Druid**：阿里巴巴数据库连接池，版本 1.2.20
- **PageResult**：通用分页结果封装类，包含分页信息和数据列表
- **EARS**：Easy Approach to Requirements Syntax，需求编写语法标准


## 需求

### 需求 1：MyBatis-Plus 框架集成

**用户故事**：作为开发人员，我希望在项目中集成 MyBatis-Plus 框架，以便使用 ORM 功能进行数据持久化操作。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在父 POM 的 `<properties>` 中定义 `mybatis-plus.version` 为 3.5.7
2. WHEN 项目构建时 THEN THE System SHALL 在父 POM 的 `<dependencyManagement>` 中声明 `mybatis-plus-spring-boot3-starter` 依赖
3. WHEN mysql-impl 模块构建时 THEN THE System SHALL 声明 `mybatis-plus-spring-boot3-starter`、`druid-spring-boot-starter`、`mysql-connector-j` 依赖且不指定版本号
4. WHEN 项目编译时 THEN THE System SHALL 成功编译无错误
5. WHEN 项目打包时 THEN THE System SHALL 成功打包无错误

#### 验证方法
- 执行 `mvn clean compile` 命令，检查编译输出无错误
- 执行 `mvn clean package` 命令，检查打包输出无错误
- 检查父 POM 文件中的 `<properties>` 和 `<dependencyManagement>` 配置
- 检查 mysql-impl 模块的 POM 文件中的依赖声明
- 执行 `mvn dependency:tree -pl infrastructure/repository/mysql-impl` 命令，验证依赖版本正确


### 需求 2：MyBatis-Plus 配置

**用户故事**：作为开发人员，我希望配置 MyBatis-Plus 的核心功能（分页、乐观锁、防全表操作），以便满足项目的数据操作需求。

#### 验收标准

1. WHEN 应用启动时 THEN THE System SHALL 在 mysql-impl 模块的 config 包下提供 MybatisPlusConfig 配置类
2. WHEN MybatisPlusConfig 加载时 THEN THE System SHALL 扫描 `com.demo.ordercore.infrastructure.repository.mysql.mapper` 包下的 Mapper 接口
3. WHEN MybatisPlusConfig 加载时 THEN THE System SHALL 注册分页插件，支持 MySQL 数据库，单页最大数量限制为 100 条
4. WHEN MybatisPlusConfig 加载时 THEN THE System SHALL 注册乐观锁插件，支持版本号并发控制
5. WHEN MybatisPlusConfig 加载时 THEN THE System SHALL 注册防全表更新删除插件，防止误操作
6. WHEN MybatisPlusConfig 加载时 THEN THE System SHALL 确保分页插件在插件链的第一位
7. WHEN 应用启动时 THEN THE System SHALL 在 mysql-impl 模块的 config 包下提供 CustomMetaObjectHandler 类
8. WHEN 执行插入操作时 THEN THE System SHALL 自动填充 createTime、updateTime 字段为当前时间
9. WHEN 执行插入操作时 THEN THE System SHALL 自动填充 deleted 字段为 0，version 字段为 0
10. WHEN 执行更新操作时 THEN THE System SHALL 自动填充 updateTime 字段为当前时间

#### 验证方法
- 启动应用，检查日志中是否包含 MyBatis-Plus 配置加载信息
- 检查 MybatisPlusConfig 类的源代码，确认插件注册顺序和配置参数
- 检查 CustomMetaObjectHandler 类的源代码，确认自动填充逻辑
- 执行插入和更新操作，验证时间戳和默认值自动填充


### 需求 3：application.yml 配置

**用户故事**：作为开发人员，我希望在 application.yml 中配置 MyBatis-Plus 的全局参数，以便统一管理框架行为。

#### 验收标准

1. WHEN 应用启动时 THEN THE System SHALL 配置 Mapper XML 文件扫描路径为 `classpath*:/mapper/**/*.xml`
2. WHEN 应用启动时 THEN THE System SHALL 配置类型别名包路径为 `com.demo.ordercore.infrastructure.repository.mysql.po`
3. WHEN 应用启动时 THEN THE System SHALL 配置逻辑删除字段为 deleted，逻辑删除值为 1，逻辑未删除值为 0
4. WHEN 应用启动时 THEN THE System SHALL 启用驼峰命名自动转换（数据库下划线命名转 Java 驼峰命名）
5. WHEN 应用启动时 THEN THE System SHALL 配置日志实现为 SLF4J

#### 验证方法
- 检查 `bootstrap/src/main/resources/application.yml` 文件内容
- 启动应用，检查日志中是否包含 MyBatis-Plus 配置加载信息
- 执行查询操作，验证驼峰命名转换是否生效
- 执行删除操作，验证逻辑删除是否生效


### 需求 4：数据源配置

**用户故事**：作为开发人员，我希望配置数据源连接信息，以便应用能够连接到 MySQL 数据库。

#### 验收标准

1. WHEN 应用启动时 THEN THE System SHALL 在 application-local.yml 中配置本地开发环境的数据库连接信息
2. WHEN 应用启动时 THEN THE System SHALL 使用 MySQL 8.x 驱动（com.mysql.cj.jdbc.Driver）
3. WHEN 应用启动时 THEN THE System SHALL 使用 Druid 连接池
4. WHEN 应用启动时 THEN THE System SHALL 配置 Druid 连接池参数（初始连接数 5、最小空闲连接数 5、最大活跃连接数 20、最大等待时间 60000 毫秒）
5. WHEN 应用启动时 THEN THE System SHALL 成功连接到数据库

#### 验证方法
- 检查 `bootstrap/src/main/resources/application-local.yml` 文件是否存在并包含数据源配置
- 启动应用，检查日志中的数据库连接信息
- 检查日志中是否包含 Druid 连接池初始化成功信息
- 执行简单的数据库查询操作，验证连接是否正常


### 需求 5：通用分页结果类

**用户故事**：作为开发人员，我希望有一个通用的分页结果封装类，以便在各层之间传递分页数据。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 common 模块的 dto 包下提供 PageResult 泛型类
2. WHEN PageResult 类定义时 THEN THE System SHALL 包含当前页（current）、每页大小（size）、总记录数（total）、总页数（pages）、数据列表（records）字段
3. WHEN PageResult 类定义时 THEN THE System SHALL 实现 Serializable 接口，支持序列化
4. WHEN PageResult 类定义时 THEN THE System SHALL 提供 convert 方法，支持数据类型转换
5. WHEN 使用 PageResult 时 THEN THE System SHALL 允许各层使用不同的泛型参数（如 PageResult<NodeEntity>、PageResult<NodeDTO>）

#### 验证方法
- 检查 `common/src/main/java/com/demo/ordercore/common/dto/PageResult.java` 文件是否存在
- 检查 PageResult 类的字段定义和方法签名
- 编写测试代码，验证 convert 方法的类型转换功能
- 在 Repository 层返回 PageResult<NodeEntity>，验证编译通过


### 需求 6：NodeEntity 领域实体定义

**用户故事**：作为开发人员，我希望定义 NodeEntity 领域实体类，以便表示系统节点的业务概念。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 repository-api 模块的 entity 包下提供 NodeEntity 类
2. WHEN NodeEntity 类定义时 THEN THE System SHALL 包含节点的基本属性（id、name、type、description、properties）
3. WHEN NodeEntity 类定义时 THEN THE System SHALL 包含审计字段（createTime、updateTime、createBy、updateBy）
4. WHEN NodeEntity 类定义时 THEN THE System SHALL 包含逻辑删除标记（deleted）和版本号（version）字段
5. WHEN NodeEntity 类定义时 THEN THE System SHALL 实现 Serializable 接口
6. WHEN NodeEntity 类定义时 THEN THE System SHALL 不包含任何持久化框架特定注解
7. WHEN NodeEntity 类定义时 THEN THE System SHALL 提供构建器模式支持

#### 验证方法
- 检查 `infrastructure/repository/repository-api/src/main/java/com/demo/ordercore/infrastructure/repository/entity/NodeEntity.java` 文件是否存在
- 检查 NodeEntity 类的字段定义，确认包含所有必需字段
- 检查 NodeEntity 类是否包含 MyBatis-Plus 或其他框架注解（应该没有）
- 编写测试代码，验证构建器模式是否可用


### 需求 7：NodePO 持久化对象定义

**用户故事**：作为开发人员，我希望定义 NodePO 持久化对象类，以便映射数据库表结构。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 mysql-impl 模块的 po 包下提供 NodePO 类
2. WHEN NodePO 类定义时 THEN THE System SHALL 映射到数据库表 t_node
3. WHEN NodePO 类定义时 THEN THE System SHALL 配置 id 字段为主键，使用雪花算法自动生成
4. WHEN NodePO 类定义时 THEN THE System SHALL 配置 createTime 字段在插入时自动填充
5. WHEN NodePO 类定义时 THEN THE System SHALL 配置 updateTime 字段在插入和更新时自动填充
6. WHEN NodePO 类定义时 THEN THE System SHALL 配置 deleted 字段为逻辑删除标记
7. WHEN NodePO 类定义时 THEN THE System SHALL 配置 version 字段为乐观锁版本号
8. WHEN NodePO 类定义时 THEN THE System SHALL 实现 Serializable 接口
9. WHEN NodePO 类定义时 THEN THE System SHALL 提供构建器模式支持

#### 验证方法
- 检查 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/mysql/po/NodePO.java` 文件是否存在
- 检查 NodePO 类的注解配置，确认表名、主键策略、自动填充、逻辑删除、乐观锁配置正确
- 执行插入操作，验证 id 是否自动生成（雪花算法）
- 执行插入操作，验证 createTime 和 updateTime 是否自动填充
- 执行更新操作，验证 updateTime 是否自动更新
- 执行删除操作，验证 deleted 字段是否设置为 1
- 执行并发更新操作，验证乐观锁是否生效


### 需求 8：数据库表创建

**用户故事**：作为开发人员，我希望创建 t_node 数据库表，以便存储节点数据。

#### 验收标准

1. WHEN 数据库初始化时 THEN THE System SHALL 创建名为 t_node 的表，使用 UTF8MB4 字符集和 InnoDB 存储引擎
2. WHEN t_node 表创建时 THEN THE System SHALL 在 name 字段上创建唯一索引，确保节点名称唯一
3. WHEN t_node 表创建时 THEN THE System SHALL 在 type 字段上创建普通索引，提升查询性能
4. WHEN t_node 表创建时 THEN THE System SHALL 在 deleted 字段上创建普通索引，提升查询性能
5. WHEN t_node 表创建时 THEN THE System SHALL 定义 id 字段为 BIGINT 类型，作为主键
6. WHEN t_node 表创建时 THEN THE System SHALL 定义 name 字段为 VARCHAR(100) 类型，不允许为空
7. WHEN t_node 表创建时 THEN THE System SHALL 定义 type 字段为 VARCHAR(50) 类型，不允许为空
8. WHEN t_node 表创建时 THEN THE System SHALL 定义 description 字段为 VARCHAR(500) 类型，允许为空
9. WHEN t_node 表创建时 THEN THE System SHALL 定义 properties 字段为 TEXT 类型，允许为空
10. WHEN t_node 表创建时 THEN THE System SHALL 定义 create_time、update_time 字段为 DATETIME 类型，不允许为空
11. WHEN t_node 表创建时 THEN THE System SHALL 定义 create_by、update_by 字段为 VARCHAR(100) 类型，不允许为空
12. WHEN t_node 表创建时 THEN THE System SHALL 定义 deleted 字段为 TINYINT 类型，不允许为空，默认值为 0
13. WHEN t_node 表创建时 THEN THE System SHALL 定义 version 字段为 INT 类型，不允许为空，默认值为 0

#### 验证方法
- 执行数据库初始化脚本，检查表是否创建成功
- 执行 `SHOW CREATE TABLE t_node` 命令，检查表结构定义
- 执行 `SHOW INDEX FROM t_node` 命令，检查索引定义
- 尝试插入重复 name 的记录，验证唯一索引是否生效


### 需求 9：NodeRepository 仓储接口定义

**用户故事**：作为开发人员，我希望定义 NodeRepository 仓储接口，以便声明数据访问契约。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 repository-api 模块的 api 包下提供 NodeRepository 接口
2. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供保存节点的方法，接收节点实体和操作人参数
3. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供更新节点的方法，接收节点实体和操作人参数
4. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供根据 ID 查询节点的方法
5. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供根据名称查询节点的方法
6. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供根据类型查询节点列表的方法
7. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供分页查询节点的方法，支持按名称和类型过滤，返回 PageResult<NodeEntity>
8. WHEN NodeRepository 接口定义时 THEN THE System SHALL 提供逻辑删除节点的方法，接收节点 ID 和操作人参数

#### 验证方法
- 检查 `infrastructure/repository/repository-api/src/main/java/com/demo/ordercore/infrastructure/repository/api/NodeRepository.java` 文件是否存在
- 检查 NodeRepository 接口的方法签名，确认参数和返回值类型正确
- 检查分页查询方法的返回类型是否为 PageResult<NodeEntity>
- 编译项目，验证接口定义无语法错误


### 需求 10：NodeMapper 接口定义

**用户故事**：作为开发人员，我希望定义 NodeMapper 接口，以便使用 MyBatis-Plus 的基础 CRUD 功能和自定义 SQL。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 mysql-impl 模块的 mapper 包下提供 NodeMapper 接口
2. WHEN NodeMapper 接口定义时 THEN THE System SHALL 继承 BaseMapper，获得基础 CRUD 能力
3. WHEN NodeMapper 接口定义时 THEN THE System SHALL 提供根据名称查询节点的方法
4. WHEN NodeMapper 接口定义时 THEN THE System SHALL 提供根据类型查询节点列表的方法
5. WHEN NodeMapper 接口定义时 THEN THE System SHALL 提供分页查询节点的方法，支持按名称和类型过滤

#### 验证方法
- 检查 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/mysql/mapper/NodeMapper.java` 文件是否存在
- 检查 NodeMapper 接口是否继承 BaseMapper<NodePO>
- 检查 NodeMapper 接口的自定义方法签名
- 启动应用，检查日志中是否包含 NodeMapper 加载信息
- 编译项目，验证接口定义无语法错误


### 需求 11：NodeMapper XML 配置

**用户故事**：作为开发人员，我希望在 XML 文件中定义条件查询的 SQL 语句，以便统一管理和优化 SQL。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 mysql-impl 模块的 resources/mapper 目录下提供 NodeMapper.xml 文件
2. WHEN NodeMapper.xml 文件定义时 THEN THE System SHALL 设置 namespace 为 NodeMapper 接口的全限定名
3. WHEN NodeMapper.xml 文件定义时 THEN THE System SHALL 定义 BaseResultMap，映射 NodePO 的所有字段
4. WHEN NodeMapper.xml 文件定义时 THEN THE System SHALL 定义根据名称查询的 SQL，只查询未删除的记录
5. WHEN NodeMapper.xml 文件定义时 THEN THE System SHALL 定义根据类型查询的 SQL，只查询未删除的记录，按创建时间降序排序
6. WHEN NodeMapper.xml 文件定义时 THEN THE System SHALL 定义分页查询的 SQL，支持名称模糊查询和类型精确查询，只查询未删除的记录，按创建时间降序排序

#### 验证方法
- 检查 `infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml` 文件是否存在
- 检查 XML 文件的 namespace 是否与 NodeMapper 接口的全限定名一致
- 检查 BaseResultMap 是否映射了所有字段
- 检查所有 SQL 语句是否包含 `deleted = 0` 条件
- 启动应用，检查日志中是否包含 Mapper XML 加载信息
- 执行查询操作，验证 SQL 是否正确执行


### 需求 12：NodeRepositoryImpl 实现类

**用户故事**：作为开发人员，我希望实现 NodeRepository 接口，以便提供具体的数据访问功能和 Entity/PO 转换。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 mysql-impl 模块的 impl 包下提供 NodeRepositoryImpl 类
2. WHEN NodeRepositoryImpl 类定义时 THEN THE System SHALL 实现 NodeRepository 接口的所有方法
3. WHEN 保存节点时 THEN THE System SHALL 转换 Entity 为 PO，设置操作人信息，调用 Mapper 插入，回填生成的 ID 和时间戳到 Entity
4. WHEN 更新节点时 THEN THE System SHALL 转换 Entity 为 PO，设置操作人信息，调用 Mapper 更新，回填更新后的时间戳和版本号到 Entity
5. WHEN 根据 ID 查询节点时 THEN THE System SHALL 调用 Mapper 查询，转换 PO 为 Entity
6. WHEN 根据名称查询节点时 THEN THE System SHALL 调用 Mapper 的自定义查询方法，转换 PO 为 Entity
7. WHEN 根据类型查询节点列表时 THEN THE System SHALL 调用 Mapper 的自定义查询方法，转换 PO 列表为 Entity 列表
8. WHEN 分页查询节点时 THEN THE System SHALL 调用 Mapper 的分页查询方法，转换 MyBatis-Plus 的 IPage 为 PageResult<NodeEntity>
9. WHEN 逻辑删除节点时 THEN THE System SHALL 先查询 PO，设置操作人信息，调用 Mapper 删除（逻辑删除）
10. WHEN NodeRepositoryImpl 类定义时 THEN THE System SHALL 提供 Entity 和 PO 之间的转换方法

#### 验证方法
- 检查 `infrastructure/repository/mysql-impl/src/main/java/com/demo/ordercore/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java` 文件是否存在
- 检查 NodeRepositoryImpl 类是否实现了 NodeRepository 接口
- 检查是否注入了 NodeMapper 依赖
- 检查是否提供了 toEntity 和 toPO 转换方法
- 编写测试代码，验证每个方法的功能
- 检查分页查询方法是否正确转换 IPage 为 PageResult


### 需求 13：数据验证

**用户故事**：作为系统，我希望验证输入数据的合法性，以便确保数据质量。

#### 验收标准

1. WHEN 保存或更新节点时 IF name 为空或超过 100 字符 THEN THE System SHALL 抛出验证异常
2. WHEN 保存或更新节点时 IF type 不在枚举值（DATABASE、APPLICATION、API、REPORT、OTHER）中 THEN THE System SHALL 抛出验证异常
3. WHEN 保存或更新节点时 IF description 超过 500 字符 THEN THE System SHALL 抛出验证异常
4. WHEN 保存或更新节点时 IF properties 不为空且不是有效的 JSON 格式 THEN THE System SHALL 抛出验证异常
5. WHEN 保存节点时 IF name 已存在 THEN THE System SHALL 抛出唯一性约束异常
6. WHEN 更新节点时 IF 新 name 与其他节点重复 THEN THE System SHALL 抛出唯一性约束异常

#### 验证方法
- 编写测试代码，尝试保存 name 为空的节点，验证是否抛出异常
- 编写测试代码，尝试保存 name 超过 100 字符的节点，验证是否抛出异常
- 编写测试代码，尝试保存 type 为非法值的节点，验证是否抛出异常
- 编写测试代码，尝试保存 description 超过 500 字符的节点，验证是否抛出异常
- 编写测试代码，尝试保存 properties 为非法 JSON 的节点，验证是否抛出异常
- 编写测试代码，尝试保存重复 name 的节点，验证是否抛出唯一性约束异常
- 编写测试代码，尝试更新节点 name 为已存在的名称，验证是否抛出唯一性约束异常


### 需求 14：异常处理

**用户故事**：作为开发人员，我希望系统能够优雅地处理异常，以便提供友好的错误信息。

#### 验收标准

1. WHEN 数据库连接失败时 THEN THE System SHALL 抛出 SystemException 并记录错误日志
2. WHEN 唯一性约束冲突时 THEN THE System SHALL 抛出 BusinessException 并包含友好的错误信息（如"节点名称已存在"）
3. WHEN 乐观锁冲突时 THEN THE System SHALL 抛出 BusinessException 并包含友好的错误信息（如"数据已被其他用户修改，请刷新后重试"）
4. WHEN 数据验证失败时 THEN THE System SHALL 抛出 BusinessException 并包含具体的验证错误信息

#### 验证方法
- 编写测试代码，模拟数据库连接失败，验证是否抛出 SystemException
- 编写测试代码，尝试插入重复 name 的节点，验证是否抛出 BusinessException 并包含友好的错误信息
- 编写测试代码，模拟乐观锁冲突，验证是否抛出 BusinessException 并包含友好的错误信息
- 编写测试代码，尝试保存非法数据，验证是否抛出 BusinessException 并包含具体的验证错误信息
- 检查日志输出，确认错误信息已记录


### 需求 15：数据库初始化脚本

**用户故事**：作为开发人员，我希望有数据库初始化脚本，以便快速搭建开发和测试环境。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 提供 schema.sql 脚本，包含 t_node 表的 DDL 语句
2. WHEN 执行 schema.sql 时 THEN THE System SHALL 成功创建 t_node 表及其索引

#### 验证方法
- 检查 `infrastructure/repository/mysql-impl/src/main/resources/db/schema.sql` 文件是否存在
- 在空数据库中执行 schema.sql，验证表是否创建成功
- 执行 `SHOW CREATE TABLE t_node` 命令，检查表结构是否正确
- 执行 `SHOW INDEX FROM t_node` 命令，检查索引是否创建成功


### 需求 16：集成测试

**用户故事**：作为开发人员，我希望编写集成测试验证 NodeRepository 的功能，以便确保数据访问层正常工作。

#### 验收标准

1. WHEN 项目构建时 THEN THE System SHALL 在 bootstrap 模块的 test 目录下提供 NodeRepositoryImplTest 测试类
2. WHEN 测试类定义时 THEN THE System SHALL 配置为 Spring Boot 集成测试，使用 local 环境配置，测试后自动回滚
3. WHEN testSave 测试执行时 THEN THE System SHALL 验证 ID 自动生成、时间自动填充、操作人信息正确、默认值正确
4. WHEN testFindById 测试执行时 THEN THE System SHALL 验证查询成功、字段值正确、查询不存在的 ID 返回 null
5. WHEN testFindByName 测试执行时 THEN THE System SHALL 验证查询成功、字段值正确、查询不存在的名称返回 null
6. WHEN testFindByType 测试执行时 THEN THE System SHALL 验证查询成功、返回列表、按创建时间降序排序
7. WHEN testFindPage 测试执行时 THEN THE System SHALL 验证分页参数正确、总记录数正确、数据列表正确、支持名称和类型过滤
8. WHEN testUpdate 测试执行时 THEN THE System SHALL 验证更新成功、updateTime 自动更新、updateBy 正确、version 自动增加
9. WHEN testDeleteById 测试执行时 THEN THE System SHALL 验证 deleted 设置为 1、查询时不返回已删除的节点
10. WHEN testUniqueConstraint 测试执行时 THEN THE System SHALL 验证创建名称重复的节点抛出异常、更新节点名称为已存在的名称抛出异常
11. WHEN testOptimisticLock 测试执行时 THEN THE System SHALL 验证并发更新同一节点时，后更新的操作抛出异常

#### 验证方法
- 检查 `bootstrap/src/test/java/com/demo/ordercore/bootstrap/repository/NodeRepositoryImplTest.java` 文件是否存在
- 检查测试类是否使用了 @SpringBootTest、@ActiveProfiles("local")、@Transactional 注解
- 执行 `mvn test -Dtest=NodeRepositoryImplTest` 命令，验证所有测试是否通过
- 检查测试覆盖率报告，确认覆盖了所有关键场景


### 需求 17：应用启动验证

**用户故事**：作为开发人员，我希望应用能够成功启动并初始化 MyBatis-Plus，以便验证集成配置正确。

#### 验收标准

1. WHEN 应用启动时 THEN THE System SHALL 成功启动无错误
2. WHEN 应用启动时 THEN THE System SHALL 在日志中输出包含 "MyBatis-Plus" 关键字的初始化信息
3. WHEN 应用启动时 THEN THE System SHALL 成功连接到数据库
4. WHEN 应用启动时 THEN THE System SHALL 成功加载 Mapper 接口
5. WHEN 应用启动时 THEN THE System SHALL 成功加载 Mapper XML 文件
6. WHEN 应用启动时 THEN THE System SHALL 成功注册分页插件、乐观锁插件、防全表更新删除插件
7. WHEN 应用启动时 THEN THE System SHALL 成功注册 CustomMetaObjectHandler

#### 验证方法
- 执行 `mvn spring-boot:run -Dspring-boot.run.profiles=local` 命令启动应用
- 检查控制台日志，确认应用启动成功，无错误信息
- 检查日志中是否包含 MyBatis-Plus 初始化信息
- 检查日志中是否包含数据库连接成功信息
- 检查日志中是否包含 Mapper 加载信息
- 检查日志中是否包含插件注册信息
- 使用 Actuator 健康检查端点，验证应用状态为 UP


### 需求 18：非功能性需求 - 性能

**用户故事**：作为系统管理员，我希望系统具有良好的性能表现，以便满足业务需求。

#### 验收标准

1. WHEN 执行单表查询时 THEN THE System SHALL 在 100 毫秒内返回结果
2. WHEN 执行分页查询时 THEN THE System SHALL 在 200 毫秒内返回结果
3. WHEN 执行并发插入操作时 THEN THE System SHALL 通过乐观锁机制支持并发控制
4. WHEN 执行并发更新操作时 THEN THE System SHALL 通过乐观锁机制支持并发控制

#### 验证方法
- 编写性能测试代码，执行单表查询 100 次，计算平均响应时间
- 编写性能测试代码，执行分页查询 100 次，计算平均响应时间
- 编写并发测试代码，模拟多线程同时插入数据，验证乐观锁是否生效
- 编写并发测试代码，模拟多线程同时更新同一条数据，验证乐观锁是否生效
- 使用 JMeter 或其他性能测试工具，验证性能指标

### 需求 19：非功能性需求 - 安全

**用户故事**：作为系统管理员，我希望系统具有安全防护机制，以便防止数据安全问题。

#### 验收标准

1. WHEN 执行 SQL 查询时 THEN THE System SHALL 使用参数化查询防止 SQL 注入
2. WHEN 尝试执行全表更新操作时 THEN THE System SHALL 通过 BlockAttackInnerInterceptor 拦截并抛出异常
3. WHEN 尝试执行全表删除操作时 THEN THE System SHALL 通过 BlockAttackInnerInterceptor 拦截并抛出异常
4. WHEN 输出日志时 THEN THE System SHALL 不输出敏感字段（如密码）

#### 验证方法
- 检查所有 SQL 语句，确认使用了参数化查询（#{} 而不是 ${}）
- 编写测试代码，尝试执行全表更新操作（不带 WHERE 条件），验证是否抛出异常
- 编写测试代码，尝试执行全表删除操作（不带 WHERE 条件），验证是否抛出异常
- 检查日志输出，确认没有输出敏感字段

### 需求 20：非功能性需求 - 可维护性

**用户故事**：作为开发人员，我希望代码具有良好的可维护性，以便后续维护和扩展。

#### 验收标准

1. WHEN 编写条件查询时 THEN THE System SHALL 在 Mapper XML 文件中定义 SQL 语句
2. WHEN 编写代码时 THEN THE System SHALL 遵循项目的 MyBatis-Plus 最佳实践规范
3. WHEN 编写代码时 THEN THE System SHALL 添加必要的注释和文档
4. WHEN 编写代码时 THEN THE System SHALL 遵循项目的命名规范和编码规范
5. WHEN 定义 Package 结构时 THEN THE System SHALL 使用 mysql 作为 package 名，明确表示 MySQL 实现
6. WHEN 配置 MyBatis-Plus 时 THEN THE System SHALL 将 MybatisPlusConfig 配置类放在 mysql-impl 模块的 config 包下

#### 验证方法
- 检查所有条件查询是否在 Mapper XML 文件中定义
- 代码审查，检查是否遵循 MyBatis-Plus 最佳实践规范
- 代码审查，检查是否添加了必要的注释和文档
- 代码审查，检查是否遵循命名规范和编码规范
- 检查 package 结构，确认使用了 mysql 作为 package 名
- 检查 MybatisPlusConfig 配置类的位置
