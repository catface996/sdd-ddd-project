# MyBatis-Plus 集成与 NodeEntity 实现设计文档

## 1. 概述

### 1.1 设计目标

本设计文档描述了在现有 DDD 分层架构的多模块 Maven 工程中集成 MyBatis-Plus 3.5.7 ORM 框架，并实现 NodeEntity 业务实体持久化功能的技术方案。

### 1.2 设计范围

- MyBatis-Plus 框架集成和配置
- NodeEntity 领域实体和持久化对象设计
- Repository 仓储层设计和实现
- 数据库表结构设计
- 通用分页结果类设计
- 数据验证和异常处理机制
- 集成测试方案

### 1.3 关键约束

- 必须使用 MyBatis-Plus 3.5.7（Spring Boot 3 专用启动器）
- 必须遵循 DDD 分层架构和项目的 MyBatis-Plus 最佳实践
- 必须使用 MySQL 8.x 数据库和 Druid 连接池
- 必须支持逻辑删除和乐观锁
- 必须确保 Entity 和 PO 分离，业务层不依赖持久化框架

### 1.4 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | LTS 版本 |
| Spring Boot | 3.4.1 | 最新稳定版本 |
| MyBatis-Plus | 3.5.7 | Spring Boot 3 专用 |
| Druid | 1.2.20 | 数据库连接池 |
| MySQL | 8.x | 数据库 |
| Lombok | - | 简化代码 |


## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Application Layer                       │
│                    (业务逻辑层 - 未来扩展)                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Repository API Layer                      │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │  NodeRepository  │         │   NodeEntity     │          │
│  │   (Interface)    │         │  (Pure POJO)     │          │
│  └──────────────────┘         └──────────────────┘          │
│         (定义契约)                  (领域模型)                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│              Repository MySQL Implementation                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              MybatisPlusConfig                       │   │
│  │  (配置类：插件、扫描、自动填充)                       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │NodeRepositoryImpl│  │    NodeMapper    │                │
│  │  (实现类)        │→ │  (继承BaseMapper)│                │
│  └──────────────────┘  └──────────────────┘                │
│         ↓ ↑                     ↓                            │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │     NodePO       │  │ NodeMapper.xml   │                │
│  │ (持久化对象)     │  │  (SQL定义)       │                │
│  └──────────────────┘  └──────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                          │
│                    MySQL 8.x + Druid                         │
│                       t_node 表                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

#### 2.2.1 common 模块
- **职责**：提供通用的 DTO 类和工具类
- **关键类**：
  - `PageResult<T>`：通用分页结果封装类

#### 2.2.2 repository-api 模块
- **职责**：定义仓储接口和领域实体，不依赖任何持久化框架
- **关键类**：
  - `NodeEntity`：节点领域实体（纯 POJO）
  - `NodeRepository`：节点仓储接口

#### 2.2.3 mysql-impl 模块
- **职责**：基于 MyBatis-Plus 实现仓储接口
- **关键类**：
  - `MybatisPlusConfig`：MyBatis-Plus 配置类
  - `CustomMetaObjectHandler`：元数据自动填充处理器
  - `NodePO`：节点持久化对象（包含 MyBatis-Plus 注解）
  - `NodeMapper`：节点 Mapper 接口
  - `NodeRepositoryImpl`：节点仓储实现类
  - `NodeMapper.xml`：SQL 定义文件

#### 2.2.4 bootstrap 模块
- **职责**：应用启动和配置
- **关键配置**：
  - `application.yml`：MyBatis-Plus 全局配置
  - `application-local.yml`：本地数据源配置


### 2.3 Package 结构设计

```
com.demo.ordercore
├── common
│   └── dto
│       └── PageResult.java                    # 通用分页结果类
│
├── infrastructure
│   └── repository
│       ├── api
│       │   └── NodeRepository.java            # 仓储接口
│       ├── entity
│       │   └── NodeEntity.java                # 领域实体
│       └── mysql
│           ├── config
│           │   ├── MybatisPlusConfig.java     # MyBatis-Plus 配置
│           │   └── CustomMetaObjectHandler.java # 自动填充处理器
│           ├── impl
│           │   └── NodeRepositoryImpl.java    # 仓储实现
│           ├── mapper
│           │   └── NodeMapper.java            # Mapper 接口
│           └── po
│               └── NodePO.java                # 持久化对象
│
└── bootstrap
    └── resources
        ├── application.yml                     # 全局配置
        ├── application-local.yml               # 本地环境配置
        ├── db
        │   └── schema.sql                      # 数据库表结构
        └── mapper
            └── NodeMapper.xml                  # SQL 定义
```

### 2.4 数据流转设计

#### 2.4.1 保存节点流程

```
1. 业务层调用 NodeRepository.save(entity, operator)
   ↓
2. NodeRepositoryImpl.save()
   - 转换 NodeEntity → NodePO
   - 设置 createBy = operator
   - 设置 updateBy = operator
   - 设置 deleted = 0（如果为 null）
   - 设置 version = 0（如果为 null）
   ↓
3. NodeMapper.insert(po)
   - MyBatis-Plus 自动生成 ID（雪花算法）
   - CustomMetaObjectHandler 自动填充 createTime、updateTime
   ↓
4. 回填数据到 NodeEntity
   - 回填 id
   - 回填 createTime
   - 回填 updateTime
   ↓
5. 返回给业务层
```

#### 2.4.2 查询节点流程

```
1. 业务层调用 NodeRepository.findByName(name)
   ↓
2. NodeRepositoryImpl.findByName()
   ↓
3. NodeMapper.selectByName(name)
   - 执行 NodeMapper.xml 中定义的 SQL
   - 只查询 deleted = 0 的记录
   ↓
4. 转换 NodePO → NodeEntity
   ↓
5. 返回给业务层
```

#### 2.4.3 分页查询流程

```
1. 业务层调用 NodeRepository.findPage(current, size, name, type)
   ↓
2. NodeRepositoryImpl.findPage()
   - 创建 Page<NodePO> 对象
   ↓
3. NodeMapper.selectPageByCondition(page, name, type)
   - 执行 NodeMapper.xml 中定义的 SQL
   - MyBatis-Plus 分页插件自动处理分页
   - 只查询 deleted = 0 的记录
   ↓
4. 转换 IPage<NodePO> → PageResult<NodeEntity>
   - 转换每个 NodePO → NodeEntity
   - 封装分页信息（current, size, total, pages, records）
   ↓
5. 返回给业务层
```


## 3. 详细设计

### 3.1 数据模型设计

#### 3.1.1 NodeEntity（领域实体）

**设计原则**：
- 纯 POJO，不包含任何持久化框架注解
- 表示业务概念，与数据库表结构解耦
- 实现 Serializable 接口，支持序列化

**字段设计**：

| 字段名 | Java 类型 | 说明 | 约束 |
|--------|----------|------|------|
| id | Long | 主键 ID | 自动生成（雪花算法） |
| name | String | 节点名称 | 唯一，最大 100 字符 |
| type | String | 节点类型 | 枚举值（DATABASE、APPLICATION、API、REPORT、OTHER） |
| description | String | 节点描述 | 可选，最大 500 字符 |
| properties | String | 节点属性 | 可选，JSON 格式字符串 |
| createTime | LocalDateTime | 创建时间 | 自动填充 |
| updateTime | LocalDateTime | 更新时间 | 自动填充 |
| createBy | String | 创建人 | 通过方法参数传递 |
| updateBy | String | 更新人 | 通过方法参数传递 |
| deleted | Integer | 逻辑删除标记 | 0=未删除，1=已删除 |
| version | Integer | 版本号 | 乐观锁，默认 0 |

**注解使用**：
- `@Data`：Lombok 注解，生成 getter/setter
- `@Builder`：Lombok 注解，支持构建器模式
- `@NoArgsConstructor`：Lombok 注解，生成无参构造函数
- `@AllArgsConstructor`：Lombok 注解，生成全参构造函数

#### 3.1.2 NodePO（持久化对象）

**设计原则**：
- 包含 MyBatis-Plus 注解，映射数据库表结构
- 与 NodeEntity 字段一致，但职责不同
- 实现 Serializable 接口，支持序列化

**字段设计**：

| 字段名 | Java 类型 | MyBatis-Plus 注解 | 说明 |
|--------|----------|------------------|------|
| id | Long | @TableId(type = IdType.ASSIGN_ID) | 主键，雪花算法生成 |
| name | String | - | 节点名称 |
| type | String | - | 节点类型 |
| description | String | - | 节点描述 |
| properties | String | - | 节点属性（JSON 字符串） |
| createTime | LocalDateTime | @TableField(fill = FieldFill.INSERT) | 创建时间，插入时自动填充 |
| updateTime | LocalDateTime | @TableField(fill = FieldFill.INSERT_UPDATE) | 更新时间，插入和更新时自动填充 |
| createBy | String | - | 创建人 |
| updateBy | String | - | 更新人 |
| deleted | Integer | @TableLogic | 逻辑删除标记 |
| version | Integer | @Version | 乐观锁版本号 |

**注解说明**：
- `@TableName("t_node")`：指定表名
- `@TableId(type = IdType.ASSIGN_ID)`：主键，雪花算法生成
- `@TableField(fill = FieldFill.INSERT)`：插入时自动填充
- `@TableField(fill = FieldFill.INSERT_UPDATE)`：插入和更新时自动填充
- `@TableLogic`：逻辑删除标记
- `@Version`：乐观锁版本号

#### 3.1.3 数据库表设计

**表名**：t_node

**字段定义**：

| 字段名 | 类型 | 长度 | 约束 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | PRIMARY KEY, NOT NULL | - | 主键 ID |
| name | VARCHAR | 100 | NOT NULL, UNIQUE | - | 节点名称 |
| type | VARCHAR | 50 | NOT NULL | - | 节点类型 |
| description | VARCHAR | 500 | NULL | - | 节点描述 |
| properties | TEXT | - | NULL | - | 节点属性（JSON） |
| create_time | DATETIME | - | NOT NULL | - | 创建时间 |
| update_time | DATETIME | - | NOT NULL | - | 更新时间 |
| create_by | VARCHAR | 100 | NOT NULL | - | 创建人 |
| update_by | VARCHAR | 100 | NOT NULL | - | 更新人 |
| deleted | TINYINT | - | NOT NULL | 0 | 逻辑删除标记 |
| version | INT | - | NOT NULL | 0 | 版本号 |

**索引设计**：

| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| uk_name | 唯一索引 | name | 保证节点名称唯一 |
| idx_type | 普通索引 | type | 提升按类型查询性能 |
| idx_deleted | 普通索引 | deleted | 提升逻辑删除查询性能 |

**表属性**：
- 字符集：UTF8MB4
- 存储引擎：InnoDB
- 排序规则：utf8mb4_general_ci

**说明**：
- properties 字段虽然是 TEXT 类型，但存储的是 JSON 格式的字符串
- 应用层负责 JSON 的序列化和反序列化
- 数据库层只负责存储字符串


### 3.2 接口设计

#### 3.2.1 NodeRepository 接口

**职责**：定义节点数据访问契约

**方法设计**：

```java
public interface NodeRepository {
    /**
     * 保存节点
     * @param entity 节点实体
     * @param operator 操作人
     */
    void save(NodeEntity entity, String operator);
    
    /**
     * 更新节点
     * @param entity 节点实体
     * @param operator 操作人
     */
    void update(NodeEntity entity, String operator);
    
    /**
     * 根据 ID 查询节点
     * @param id 节点 ID
     * @return 节点实体，不存在返回 null
     */
    NodeEntity findById(Long id);
    
    /**
     * 根据名称查询节点
     * @param name 节点名称
     * @return 节点实体，不存在返回 null
     */
    NodeEntity findByName(String name);
    
    /**
     * 根据类型查询节点列表
     * @param type 节点类型
     * @return 节点列表
     */
    List<NodeEntity> findByType(String type);
    
    /**
     * 分页查询节点
     * @param current 当前页
     * @param size 每页大小
     * @param name 节点名称（模糊查询，可选）
     * @param type 节点类型（精确查询，可选）
     * @return 分页结果
     */
    PageResult<NodeEntity> findPage(Integer current, Integer size, 
                                     String name, String type);
    
    /**
     * 逻辑删除节点
     * @param id 节点 ID
     * @param operator 操作人
     */
    void deleteById(Long id, String operator);
}
```

#### 3.2.2 NodeMapper 接口

**职责**：定义数据库操作方法

**方法设计**：

```java
@Mapper
public interface NodeMapper extends BaseMapper<NodePO> {
    /**
     * 根据名称查询节点
     * @param name 节点名称
     * @return 节点 PO，不存在返回 null
     */
    NodePO selectByName(@Param("name") String name);
    
    /**
     * 根据类型查询节点列表
     * @param type 节点类型
     * @return 节点 PO 列表
     */
    List<NodePO> selectByType(@Param("type") String type);
    
    /**
     * 分页查询节点
     * @param page 分页对象
     * @param name 节点名称（模糊查询，可选）
     * @param type 节点类型（精确查询，可选）
     * @return 分页结果
     */
    IPage<NodePO> selectPageByCondition(Page<?> page, 
                                        @Param("name") String name, 
                                        @Param("type") String type);
}
```

**说明**：
- 继承 `BaseMapper<NodePO>` 获得基础 CRUD 能力
- 自定义方法在 NodeMapper.xml 中定义 SQL
- 使用 `@Param` 注解标注参数名称


### 3.3 组件设计

#### 3.3.1 PageResult 通用分页结果类

**设计原则**：
- 泛型类，支持任意数据类型
- 实现 Serializable 接口
- 提供 convert 方法支持类型转换

**字段设计**：

```java
public class PageResult<T> implements Serializable {
    private Long current;           // 当前页
    private Long size;              // 每页大小
    private Long total;             // 总记录数
    private Long pages;             // 总页数
    private List<T> records;        // 数据列表
}
```

**方法设计**：
- `convert(Function<T, R> converter)`：类型转换方法，接收转换函数，返回转换后的分页结果

**使用场景**：
- Repository 层：`PageResult<NodeEntity>`
- Application 层：`PageResult<NodeDTO>`（未来扩展）
- HTTP 层：`PageResult<NodeVO>`（未来扩展）

#### 3.3.2 MybatisPlusConfig 配置类

**设计原则**：
- 配置内聚：放在 mysql-impl 模块
- 插件顺序：分页插件必须在第一位
- 合理配置：单页最大 100 条

**配置内容**：

**Mapper 扫描**：
- 使用 `@MapperScan` 注解
- 扫描路径：`com.demo.ordercore.infrastructure.repository.mysql.mapper`

**插件配置**：
1. **分页插件**（PaginationInnerInterceptor）
   - 必须放在第一位
   - 数据库类型：MySQL
   - 单页最大数量：100 条
   - 溢出处理：不处理

2. **乐观锁插件**（OptimisticLockerInnerInterceptor）
   - 支持 @Version 注解的版本号并发控制

3. **防全表更新删除插件**（BlockAttackInnerInterceptor）
   - 拦截不带 WHERE 条件的 UPDATE 和 DELETE 操作

#### 3.3.3 CustomMetaObjectHandler 自动填充处理器

**设计原则**：
- 只填充时间戳字段
- createBy 和 updateBy 通过方法参数传递

**填充逻辑**：

**插入时填充**（insertFill）：
- createTime：当前时间
- updateTime：当前时间
- deleted：0
- version：0

**更新时填充**（updateFill）：
- updateTime：当前时间

**说明**：
- 只在字段为 null 时填充
- createBy 和 updateBy 不使用自动填充的原因：
  - 需要从方法参数获取操作人信息
  - 不同操作可能有不同的操作人
  - 无法从全局上下文获取（如 Spring Security）
- createBy 和 updateBy 的设置位置：
  - 在 NodeRepositoryImpl 的 save 方法中设置 createBy 和 updateBy
  - 在 NodeRepositoryImpl 的 update 方法中设置 updateBy
  - 在 NodeRepositoryImpl 的 deleteById 方法中设置 updateBy


#### 3.3.4 NodeRepositoryImpl 实现类

**设计原则**：
- 实现 NodeRepository 接口
- 负责 Entity 和 PO 之间的转换
- 简单操作使用 MyBatis-Plus API
- 条件查询使用 Mapper XML

**核心职责**：

##### save 方法职责
1. 转换 Entity → PO
2. 设置操作人信息（createBy、updateBy）
3. 设置默认值（deleted = 0、version = 0）
4. 调用 Mapper.insert 插入数据
5. 回填生成的 ID 和时间戳到 Entity

##### update 方法职责
1. 转换 Entity → PO
2. 设置操作人信息（updateBy）
3. 将 updateTime 设置为 null，让自动填充生效
4. 调用 Mapper.updateById 更新数据
5. 回填更新后的时间戳和版本号到 Entity

##### findById 方法职责
1. 调用 Mapper.selectById 查询数据
2. 转换 PO → Entity
3. 返回 Entity（不存在返回 null）

##### findByName 方法职责
1. 调用 Mapper.selectByName 查询数据（XML 中定义）
2. 转换 PO → Entity
3. 返回 Entity（不存在返回 null）

##### findByType 方法职责
1. 调用 Mapper.selectByType 查询数据（XML 中定义）
2. 转换 PO 列表 → Entity 列表
3. 返回 Entity 列表

##### findPage 方法职责
1. 创建 Page<NodePO> 分页对象
2. 调用 Mapper.selectPageByCondition 分页查询（XML 中定义）
3. 转换 IPage<NodePO> → PageResult<NodeEntity>
   - 遍历 IPage.getRecords()，将每个 NodePO 转换为 NodeEntity
   - 复制分页信息（current、size、total、pages）
   - 封装为 PageResult<NodeEntity>
4. 返回 PageResult<NodeEntity>

##### deleteById 方法职责
1. 先查询 PO（验证是否存在）
2. 设置操作人信息（updateBy）
3. 调用 Mapper.deleteById 逻辑删除

##### 转换方法职责
- `toEntity(NodePO)`：将 PO 转换为 Entity
- `toPO(NodeEntity)`：将 Entity 转换为 PO
- 转换逻辑封装在 RepositoryImpl 内部，对外只暴露 Entity


### 3.4 SQL 设计

#### 3.4.1 NodeMapper.xml 结构

**文件位置**：`infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml`

**namespace**：`com.demo.ordercore.infrastructure.repository.mysql.mapper.NodeMapper`

#### 3.4.2 ResultMap 定义

**ResultMap ID**：BaseResultMap

**映射类型**：NodePO 的全限定名

**字段映射**：
- id → id（主键）
- name → name
- type → type
- description → description
- properties → properties
- create_time → createTime（驼峰转换）
- update_time → updateTime（驼峰转换）
- create_by → createBy（驼峰转换）
- update_by → updateBy（驼峰转换）
- deleted → deleted
- version → version

#### 3.4.3 SQL 语句定义

##### selectByName

**查询条件**：
- name = #{name}（精确匹配）
- deleted = 0（只查询未删除的记录）

**返回结果**：BaseResultMap

##### selectByType

**查询条件**：
- type = #{type}（精确匹配）
- deleted = 0（只查询未删除的记录）

**排序**：按 create_time 降序

**返回结果**：BaseResultMap

##### selectPageByCondition

**查询条件**：
- deleted = 0（必须）
- name LIKE '%#{name}%'（可选，模糊查询）
- type = #{type}（可选，精确查询）

**排序**：按 create_time 降序

**返回结果**：BaseResultMap

**分页支持**：MyBatis-Plus 分页插件自动处理

**SQL 设计原则**：
- 所有查询都包含 `deleted = 0` 条件
- 使用参数化查询（`#{}` 而不是 `${}`）防止 SQL 注入
- 分页查询按 create_time 降序排序
- 名称查询使用模糊匹配（LIKE）
- 类型查询使用精确匹配（=）


## 4. 配置设计

### 4.1 依赖配置

#### 4.1.1 父 POM 配置

**文件位置**：`pom.xml`

**版本定义**：
- mybatis-plus.version：3.5.7
- druid.version：1.2.20

**依赖管理**：
- MyBatis-Plus：com.baomidou:mybatis-plus-spring-boot3-starter:${mybatis-plus.version}
- Druid：com.alibaba:druid-spring-boot-starter:${druid.version}

#### 4.1.2 mysql-impl 模块配置

**文件位置**：`infrastructure/repository/mysql-impl/pom.xml`

**依赖声明**：
- repository-api 模块（com.demo.ordercore:repository-api）
- common 模块（com.demo.ordercore:common）
- MyBatis-Plus（com.baomidou:mybatis-plus-spring-boot3-starter）
- Druid（com.alibaba:druid-spring-boot-starter）
- MySQL 驱动（com.mysql:mysql-connector-j，scope=runtime）
- Lombok（org.projectlombok:lombok，optional=true）

**说明**：
- 不指定版本号，从父 POM 继承
- MySQL 驱动 scope 为 runtime
- Lombok 设置为 optional

### 4.2 application.yml 配置

**文件位置**：`bootstrap/src/main/resources/application.yml`

**配置内容**：

**Mapper XML 文件位置**：
- mapper-locations: classpath*:/mapper/**/*.xml

**类型别名包路径**：
- type-aliases-package: com.demo.ordercore.infrastructure.repository.mysql.po

**全局配置**：
- 逻辑删除字段：deleted
- 逻辑删除值：1
- 逻辑未删除值：0

**MyBatis 原生配置**：
- 驼峰命名转换：启用（map-underscore-to-camel-case: true）
- 日志实现：SLF4J（log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl）

### 4.3 application-local.yml 配置

**文件位置**：`bootstrap/src/main/resources/application-local.yml`

**配置内容**：

**数据源基本配置**：
- 数据源类型：Druid（com.alibaba.druid.pool.DruidDataSource）
- 驱动类名：com.mysql.cj.jdbc.Driver
- JDBC URL：从本地 Docker 环境中查找（包含字符集、时区等参数）
- 用户名和密码：从本地 Docker 环境中查找

**Druid 连接池配置**：
- 初始连接数：5
- 最小空闲连接数：5
- 最大活跃连接数：20
- 获取连接最大等待时间：60000 毫秒
- 空闲连接检测间隔：60000 毫秒
- 连接最小生存时间：300000 毫秒
- 连接有效性验证：启用（test-while-idle: true）
- 验证 SQL：SELECT 1

**说明**：
- 数据库连接信息从本地 Docker 环境中查找
- Druid 连接池参数根据实际情况调整
- 建议开启连接有效性检测


## 5. 错误处理设计

### 5.1 异常类型

#### 5.1.1 数据验证异常

**场景**：
- name 为空或超过 100 字符
- type 不在枚举值中
- description 超过 500 字符
- properties 不是有效的 JSON 格式

**处理方式**：
- 抛出 `BusinessException`
- 错误码：`VALIDATION_ERROR`
- 错误信息：具体的验证错误信息

#### 5.1.2 唯一性约束异常

**场景**：
- 保存节点时 name 已存在
- 更新节点时新 name 与其他节点重复

**处理方式**：
- 捕获 `DuplicateKeyException`
- 转换为 `BusinessException`
- 错误码：`DUPLICATE_KEY`
- 错误信息："节点名称已存在"

#### 5.1.3 乐观锁冲突异常

**场景**：
- 并发更新同一节点时，version 不匹配

**处理方式**：
- MyBatis-Plus 自动抛出 `OptimisticLockerException`
- 转换为 `BusinessException`
- 错误码：`OPTIMISTIC_LOCK_ERROR`
- 错误信息："数据已被其他用户修改，请刷新后重试"

#### 5.1.4 数据库连接异常

**场景**：
- 数据库连接失败
- SQL 执行超时

**处理方式**：
- 抛出 `SystemException`
- 错误码：`DATABASE_ERROR`
- 记录详细的错误日志

### 5.2 异常处理流程

```
1. Repository 层捕获底层异常
   ↓
2. 转换为业务异常或系统异常
   ↓
3. 记录错误日志（包含 traceId）
   ↓
4. 抛出异常给上层
   ↓
5. 全局异常处理器统一处理
   ↓
6. 返回友好的错误信息给客户端
```

**详细流程**：

**在 NodeRepositoryImpl 中**：
1. 使用 try-catch 捕获 MyBatis-Plus 和数据库异常
2. 根据异常类型进行转换：
   - DuplicateKeyException → BusinessException（错误码：DUPLICATE_KEY，消息："节点名称已存在"）
   - OptimisticLockerException → BusinessException（错误码：OPTIMISTIC_LOCK_ERROR，消息："数据已被其他用户修改，请刷新后重试"）
   - DataAccessException → SystemException（错误码：DATABASE_ERROR，消息："数据库操作失败"）
3. 记录错误日志：
   - 日志级别：ERROR（系统异常）或 WARN（业务异常）
   - 日志内容：异常类型、错误消息、操作参数、traceId
4. 抛出转换后的异常给上层

**在全局异常处理器中**：
1. 捕获 BusinessException 和 SystemException
2. 构造统一的错误响应（Result 对象）
3. 返回给客户端

### 5.3 数据验证设计

#### 5.3.1 验证规则

| 字段 | 验证规则 |
|------|---------|
| name | 非空，长度 1-100 字符 |
| type | 非空，枚举值（DATABASE、APPLICATION、API、REPORT、OTHER） |
| description | 可选，最大 500 字符 |
| properties | 可选，有效的 JSON 格式 |

#### 5.3.2 验证实现

**验证逻辑**：

**name 字段验证**：
- 非空验证
- 长度验证（1-100 字符）

**type 字段验证**：
- 非空验证
- 枚举值验证（DATABASE、APPLICATION、API、REPORT、OTHER）

**description 字段验证**：
- 长度验证（最大 500 字符）

**properties 字段验证**：
- JSON 格式验证（如果不为空）

**验证位置**：
- 在 NodeRepositoryImpl 的 save 和 update 方法中进行验证
- 保持 NodeEntity 的纯粹性，不添加验证注解

**验证触发时机**：
- save 方法：在转换 Entity 为 PO 之前进行验证
- update 方法：在转换 Entity 为 PO 之前进行验证
- 验证失败时抛出 BusinessException，包含具体的验证错误信息


## 6. 测试策略

### 6.1 测试框架

- **Spring Boot Test**：集成测试框架
- **JUnit 5**：单元测试框架
- **Mockito**：Mock 框架（如需要）

### 6.2 测试配置

**测试类设计**：

**测试类路径**：
- `bootstrap/src/test/java/com/demo/ordercore/bootstrap/repository/NodeRepositoryImplTest.java`

**测试类注解**：
- `@SpringBootTest`：加载完整的 Spring 上下文
- `@ActiveProfiles("local")`：使用 local 环境配置
- `@Transactional`：测试后自动回滚，不污染数据库

**依赖注入**：
- 注入 NodeRepository 接口（不是实现类）
- 通过接口调用方法，验证功能

**说明**：
- 测试类位于 bootstrap 模块，因为需要加载完整的 Spring 上下文
- 使用 @Transactional 确保测试数据不会保留在数据库中
- 每个测试方法独立，不依赖其他测试方法的执行顺序

### 6.3 测试场景

#### 6.3.1 保存节点测试

**测试目标**：验证节点保存功能

**测试步骤**：
1. 创建 NodeEntity 对象
2. 调用 `nodeRepository.save(entity, "test-user")`
3. 验证 ID 自动生成（不为 null）
4. 验证 createTime 和 updateTime 自动填充
5. 验证 createBy 和 updateBy 为 "test-user"
6. 验证 deleted 默认值为 0
7. 验证 version 默认值为 0

#### 6.3.2 查询节点测试

**测试目标**：验证节点查询功能

**测试步骤**：
1. 保存一个测试节点
2. 根据 ID 查询，验证查询成功
3. 根据名称查询，验证查询成功
4. 根据类型查询，验证返回列表
5. 查询不存在的 ID，验证返回 null
6. 查询不存在的名称，验证返回 null

#### 6.3.3 分页查询测试

**测试目标**：验证分页查询功能

**测试步骤**：
1. 保存多个测试节点（不同类型）
2. 分页查询所有节点，验证分页参数正确
3. 按名称过滤查询，验证过滤生效
4. 按类型过滤查询，验证过滤生效
5. 验证按 createTime 降序排序

#### 6.3.4 更新节点测试

**测试目标**：验证节点更新功能

**测试步骤**：
1. 保存一个测试节点
2. 修改节点信息
3. 调用 `nodeRepository.update(entity, "test-user2")`
4. 验证更新成功
5. 验证 updateTime 自动更新
6. 验证 updateBy 为 "test-user2"
7. 验证 version 自动增加

#### 6.3.5 逻辑删除测试

**测试目标**：验证逻辑删除功能

**测试步骤**：
1. 保存一个测试节点
2. 调用 `nodeRepository.deleteById(id, "test-user")`
3. 根据 ID 查询，验证返回 null（已被逻辑删除）
4. 直接查询数据库，验证 deleted 字段为 1

#### 6.3.6 唯一性约束测试

**测试目标**：验证节点名称唯一性

**测试步骤**：
1. 保存一个测试节点（name = "test-node"）
2. 尝试保存另一个节点（name = "test-node"）
3. 验证抛出 BusinessException
4. 验证错误信息包含"节点名称已存在"

#### 6.3.7 乐观锁测试

**测试目标**：验证乐观锁并发控制

**测试步骤**：
1. 保存一个测试节点
2. 查询两次，得到两个 Entity 对象（version 相同）
3. 更新第一个 Entity，成功
4. 尝试更新第二个 Entity，验证抛出异常

### 6.4 测试数据准备

**原则**：测试数据在测试方法中动态创建，不使用 data.sql

**准备方式**：
- 在测试方法中使用 NodeEntity.builder() 创建测试数据
- 调用 nodeRepository.save() 保存到数据库
- 使用 @Transactional 注解确保测试后自动回滚

### 6.5 测试覆盖率目标

- **行覆盖率**：≥ 80%
- **分支覆盖率**：≥ 70%
- **核心业务逻辑**：100%


## 7. 非功能性设计

### 7.1 性能设计

#### 7.1.1 性能目标

| 操作类型 | 响应时间目标 |
|---------|-------------|
| 单表查询 | < 100ms |
| 分页查询 | < 200ms |
| 插入操作 | < 50ms |
| 更新操作 | < 50ms |

#### 7.1.2 性能优化措施

**数据库层面**：
- 在 name 字段上创建唯一索引
- 在 type 字段上创建普通索引
- 在 deleted 字段上创建普通索引
- 使用 InnoDB 存储引擎，支持事务和行锁

**连接池层面**：
- 使用 Druid 连接池
- 初始连接数：5
- 最小空闲连接数：5
- 最大活跃连接数：20
- 连接有效性检测

**查询层面**：
- 分页查询限制单页最大 100 条
- 只查询未删除的记录（deleted = 0）
- 使用参数化查询，避免 SQL 注入

**并发控制**：
- 使用乐观锁（version 字段）
- 避免长事务

### 7.2 安全设计

#### 7.2.1 SQL 注入防护

**措施**：
- 所有 SQL 使用参数化查询（`#{}` 而不是 `${}`）
- 不拼接 SQL 字符串
- 使用 MyBatis-Plus 的 BaseMapper 方法

#### 7.2.2 防止全表操作

**措施**：
- 配置 BlockAttackInnerInterceptor 插件
- 拦截不带 WHERE 条件的 UPDATE 和 DELETE 操作
- 抛出异常，防止误操作

#### 7.2.3 敏感信息保护

**措施**：
- 数据库密码不明文存储在配置文件中（可使用加密）
- 日志中不输出敏感字段（如密码）
- 使用 HTTPS 传输数据（未来扩展）

### 7.3 可观测性设计

#### 7.3.1 日志设计

**日志级别**：
- DEBUG：SQL 语句和参数
- INFO：关键业务操作（保存、更新、删除）
- WARN：异常情况（唯一性冲突、乐观锁冲突）
- ERROR：系统错误（数据库连接失败）

**日志内容**：
- 操作类型
- 操作参数
- 操作结果
- 执行时间
- traceId 和 spanId（链路追踪）

**日志格式**：
- 包含操作类型、操作参数、操作结果、执行时间
- 包含 traceId 和 spanId（链路追踪）
- 使用结构化日志格式（JSON）

#### 7.3.2 监控指标

**数据库监控**：
- 连接池使用情况
- SQL 执行时间
- 慢查询统计
- 异常统计

**业务监控**：
- 节点创建数量
- 节点查询次数
- 节点更新次数
- 节点删除次数

### 7.4 可维护性设计

#### 7.4.1 代码规范

**命名规范**：
- 类名：大驼峰（NodeEntity、NodeRepository）
- 方法名：小驼峰（findById、findByName）
- 常量：全大写下划线（MAX_PAGE_SIZE）
- Package：全小写（com.demo.ordercore.infrastructure.repository）

**注释规范**：
- 类注释：说明类的职责
- 方法注释：说明方法的功能、参数、返回值
- 复杂逻辑：添加行内注释

**代码组织**：
- 一个类一个文件
- 相关类放在同一个 package
- 接口和实现分离

#### 7.4.2 配置管理

**原则**：
- 配置集中管理
- 配置分层（全局配置、环境配置）
- 配置外部化（不硬编码）

**配置文件**：
- `application.yml`：全局配置
- `application-local.yml`：本地环境配置
- `application-dev.yml`：开发环境配置（未来扩展）

#### 7.4.3 SQL 管理

**原则**：
- 所有条件查询在 Mapper XML 中定义
- SQL 语句格式化，易于阅读
- 添加注释说明 SQL 用途

**SQL 注释规范**：
- 每个 SQL 语句添加注释说明用途
- 注释格式：`<!-- 功能描述 -->`
- 示例：`<!-- 根据名称查询节点（精确匹配） -->`


## 8. 技术决策记录（ADR）

### ADR-001：选择 MyBatis-Plus 作为 ORM 框架

**状态**：已接受

**背景**：
- 项目需要一个 ORM 框架来简化数据库操作
- 需要支持 Spring Boot 3 和 JDK 21
- 需要支持逻辑删除、乐观锁、分页等功能

**决策**：选择 MyBatis-Plus 3.5.7

**理由**：
- 基于 MyBatis，学习成本低
- 提供丰富的增强功能（BaseMapper、分页插件、乐观锁插件等）
- 支持 Spring Boot 3（使用 mybatis-plus-spring-boot3-starter）
- 社区活跃，文档完善
- 性能优秀，损耗小

**后果**：
- 正面：开发效率高，代码简洁，功能强大
- 负面：需要学习 MyBatis-Plus 的特性和最佳实践

### ADR-002：Entity 和 PO 分离

**状态**：已接受

**背景**：
- 需要遵循 DDD 分层架构
- 需要保持领域层的纯粹性
- 需要隔离持久化框架的影响

**决策**：将 Entity 和 PO 分离，Entity 放在 repository-api 模块，PO 放在 mysql-impl 模块

**理由**：
- Entity 是纯 POJO，不依赖任何持久化框架
- PO 包含 MyBatis-Plus 注解，专门用于数据库映射
- 业务层只依赖 Entity，不依赖 PO
- 便于切换持久化实现（如从 MyBatis-Plus 切换到 JPA）

**后果**：
- 正面：架构清晰，职责分离，易于维护和扩展
- 负面：需要在 RepositoryImpl 中进行 Entity 和 PO 的转换

### ADR-003：使用 PageResult 作为通用分页结果类

**状态**：已接受

**背景**：
- 需要在各层之间传递分页数据
- MyBatis-Plus 提供了 IPage 接口
- 需要一个统一的分页结果封装类

**决策**：创建 PageResult 泛型类，放在 common 模块

**理由**：
- 分页结构在所有层都是一致的
- 避免在各层之间进行无意义的转换
- 与 Result<T> 的设计理念一致
- 提供 convert 方法支持类型转换
- 业界主流做法（Spring Data 的 Page<T>、MyBatis-Plus 的 IPage<T> 都是跨层共享的）

**后果**：
- 正面：简单实用，减少重复代码，易于使用
- 负面：分页类跨层共享，但这是合理的设计

### ADR-004：条件查询使用 Mapper XML 而不是 Wrapper

**状态**：已接受

**背景**：
- MyBatis-Plus 提供了 Wrapper 构造查询条件
- 项目需要统一管理 SQL 语句
- 需要便于 DBA 进行性能分析和优化

**决策**：所有条件查询在 Mapper XML 中定义 SQL，不使用 Wrapper

**理由**：
- SQL 语句集中管理，便于维护和优化
- 便于代码审查，及时发现潜在问题
- 便于 DBA 进行性能分析和索引优化
- 避免动态 SQL 难以追踪和调试
- 提高 SQL 的可读性和可维护性

**后果**：
- 正面：SQL 管理规范，易于维护和优化
- 负面：需要编写 XML 文件，开发效率略低

### ADR-005：使用 Druid 作为数据库连接池

**状态**：已接受

**背景**：
- 需要一个高性能的数据库连接池
- 需要支持连接监控和统计
- 需要支持 Spring Boot 3

**决策**：选择 Druid 1.2.20

**理由**：
- 阿里巴巴开源，性能优秀
- 提供丰富的监控和统计功能
- 支持 SQL 防火墙，防止 SQL 注入
- 支持 Spring Boot 3
- 社区活跃，文档完善

**后果**：
- 正面：性能优秀，监控功能强大，安全性高
- 负面：配置相对复杂，需要学习 Druid 的特性

### ADR-006：只配置 local 环境，不配置多环境

**状态**：已接受

**背景**：
- 原始需求要求支持 5 个环境（local、dev、test、staging、prod）
- 用户反馈不需要多环境配置

**决策**：只配置 local 环境，简化配置

**理由**：
- 当前阶段只需要本地开发环境
- 简化配置，降低复杂度
- 未来如需要，可以随时添加其他环境配置

**后果**：
- 正面：配置简单，聚焦本地开发
- 负面：未来如需要多环境，需要补充配置

### ADR-007：测试数据在测试用例中编写，不使用 data.sql

**状态**：已接受

**背景**：
- 需要为集成测试准备测试数据
- 可以使用 data.sql 文件或在测试用例中动态创建

**决策**：测试数据在测试用例中动态创建，不使用 data.sql

**理由**：
- 测试数据更灵活，可以根据测试场景动态调整
- 避免测试数据污染数据库
- 使用 @Transactional 注解，测试后自动回滚
- 测试数据与测试逻辑在一起，易于理解

**后果**：
- 正面：测试数据灵活，不污染数据库
- 负面：每个测试方法需要准备测试数据


## 9. 风险和应对

### 9.1 技术风险

#### 风险 1：MyBatis-Plus 版本兼容性问题

**描述**：MyBatis-Plus 3.5.7 与 Spring Boot 3.4.1 可能存在兼容性问题

**影响**：应用启动失败或运行时异常

**概率**：低

**应对策略**：
- 使用官方推荐的 mybatis-plus-spring-boot3-starter
- 参考官方文档和社区案例
- 编写集成测试验证兼容性
- 如有问题，考虑降级或升级版本

#### 风险 2：数据库连接失败

**描述**：本地 Docker 环境中的数据库连接信息不正确

**影响**：应用启动失败

**概率**：中

**应对策略**：
- 提供清晰的数据库连接配置说明
- 在启动前检查数据库是否可用
- 提供数据库初始化脚本（schema.sql）
- 记录详细的错误日志

#### 风险 3：性能问题

**描述**：分页查询或复杂查询性能不达标

**影响**：响应时间超过目标

**概率**：低

**应对策略**：
- 在关键字段上创建索引
- 使用 EXPLAIN 分析 SQL 执行计划
- 限制单页最大数量（100 条）
- 使用 Druid 监控 SQL 执行时间
- 如有问题，优化 SQL 或添加缓存

### 9.2 业务风险

#### 风险 4：数据一致性问题

**描述**：并发更新导致数据不一致

**影响**：数据错误

**概率**：低

**应对策略**：
- 使用乐观锁（version 字段）
- 编写并发测试验证
- 记录详细的操作日志
- 提供数据修复机制

#### 风险 5：数据丢失风险

**描述**：逻辑删除后数据无法恢复

**影响**：业务数据丢失

**概率**：低

**应对策略**：
- 使用逻辑删除而不是物理删除
- 记录删除操作人和删除时间
- 提供数据恢复功能（未来扩展）
- 定期备份数据库

### 9.3 运维风险

#### 风险 6：配置错误

**描述**：配置文件中的参数配置错误

**影响**：应用启动失败或功能异常

**概率**：中

**应对策略**：
- 提供配置文件模板和说明
- 使用配置验证机制
- 记录详细的启动日志
- 提供配置检查工具

#### 风险 7：数据库表结构变更

**描述**：数据库表结构需要变更

**影响**：需要修改代码和配置

**概率**：中

**应对策略**：
- 使用数据库迁移工具（如 Flyway、Liquibase）
- 提供表结构变更脚本
- 编写兼容性测试
- 记录变更历史

## 10. 实施计划

### 10.1 开发阶段

**阶段 1：基础设施搭建**
- 配置依赖（父 POM、mysql-impl 模块）
- 配置 MyBatis-Plus（MybatisPlusConfig、CustomMetaObjectHandler）
- 配置数据源（application.yml、application-local.yml）
- 创建数据库表（schema.sql）

**schema.sql 设计要点**：
- 文件位置：`infrastructure/repository/mysql-impl/src/main/resources/db/schema.sql`
- 表创建语句：
  - 使用 `CREATE TABLE IF NOT EXISTS` 避免重复创建
  - 指定字符集：`DEFAULT CHARSET=utf8mb4`
  - 指定存储引擎：`ENGINE=InnoDB`
  - 指定排序规则：`COLLATE=utf8mb4_general_ci`
- 索引创建语句：
  - 主键索引：`PRIMARY KEY (id)`
  - 唯一索引：`UNIQUE KEY uk_name (name)`
  - 普通索引：`KEY idx_type (type)`、`KEY idx_deleted (deleted)`
- 字段定义：
  - 所有字段使用 `COMMENT` 添加注释
  - 时间字段使用 `DATETIME` 类型
  - 逻辑删除字段使用 `TINYINT` 类型，默认值 0
  - 版本号字段使用 `INT` 类型，默认值 0

**阶段 2：核心功能实现**
- 创建 PageResult 通用分页类
- 创建 NodeEntity 领域实体
- 创建 NodePO 持久化对象
- 创建 NodeRepository 接口
- 创建 NodeMapper 接口和 XML
- 实现 NodeRepositoryImpl

**阶段 3：数据验证和异常处理**
- 实现数据验证逻辑
- 实现异常处理逻辑
- 集成全局异常处理器

**阶段 4：测试**
- 编写集成测试
- 执行测试并修复问题
- 验证性能指标

**阶段 5：文档和交付**
- 完善代码注释
- 编写使用文档
- 代码审查
- 交付

### 10.2 验收标准

- [ ] 项目可以成功编译和打包
- [ ] 应用可以成功启动
- [ ] 所有集成测试通过
- [ ] 性能指标达标（单表查询 < 100ms，分页查询 < 200ms）
- [ ] 代码符合规范，通过代码审查
- [ ] 文档完整，易于理解

## 11. 参考资料

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Druid 官方文档](https://github.com/alibaba/druid)
- [MySQL 官方文档](https://dev.mysql.com/doc/)
- 项目 MyBatis-Plus 最佳实践：`.kiro/steering/06-mybatis-plus-best-practice.md`
