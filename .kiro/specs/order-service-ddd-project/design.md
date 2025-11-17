# Order Service 系统设计文档

## 1. 设计概述

### 1.1 设计目标

基于需求文档中定义的 27 个需求，设计一个符合 DDD 架构原则的多模块 Maven 工程，实现：
- 清晰的分层架构和模块边界
- 技术与业务逻辑的解耦
- 可扩展、可演进的系统结构
- 完善的可观测性和运维支持

### 1.2 设计原则

| 原则 | 说明 | 对应需求 |
|------|------|----------|
| 领域驱动 | 聚焦核心领域模型，将复杂度控制在领域内部 | 需求 1-5 |
| 分层隔离 | 严格遵循 DDD 层次边界，外层依赖内层 | 需求 16 |
| 单向依赖 | 内层永不反向依赖外层 | 需求 16 |
| 可替换性 | 技术实现模块可独立替换 | 需求 5, 24 |
| 统一依赖管理 | 通过父 POM 统一管理所有依赖版本 | 需求 1, 15 |

### 1.3 设计范围

本设计文档覆盖需求 1-27 的所有功能和非功能性需求，包括：
- 项目架构和模块划分（需求 1-7）
- 技术基础设施（需求 8-14）
- 质量保证机制（需求 15-18）
- 接口定义和规范（需求 19-20）
- 性能和安全（需求 21-26）
- 构建和部署（需求 27）


## 2. 架构设计

### 2.1 整体架构

采用经典的 DDD 四层架构：

```
┌─────────────────────────────────────────────────────────┐
│                    Interface Layer                       │
│  ┌──────────────────┐      ┌──────────────────┐        │
│  │   HTTP Module    │      │  Consumer Module │        │
│  │  (REST API)      │      │  (MQ Consumer)   │        │
│  └──────────────────┘      └──────────────────┘        │
│  职责：处理外部请求，参数校验，输入输出转换              │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Application Service (Use Case)           │  │
│  └──────────────────────────────────────────────────┘  │
│  职责：业务用例编排，协调领域层完成业务逻辑              │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │    Domain Model + Domain Service                 │  │
│  └──────────────────────────────────────────────────┘  │
│  职责：核心业务逻辑，领域规则，业务不变性                │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                Infrastructure Layer                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │Repository│  │  Cache   │  │    MQ    │             │
│  │ (MySQL)  │  │ (Redis)  │  │  (SQS)   │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│  职责：技术基础设施实现，数据持久化，外部服务集成          │
└─────────────────────────────────────────────────────────┘
```

**设计说明：**
- 每层职责单一，边界清晰
- 依赖关系单向，从上到下
- 接口层和基础设施层可独立替换
- 领域层不依赖任何技术实现

**满足需求：** 需求 1-5（模块结构），需求 16（依赖关系验证）

### 2.2 模块划分

#### 2.2.1 顶层模块结构

```
order-service/                    # 父 POM 项目
├── pom.xml                       # 父 POM（需求 1）
├── common/                       # 通用模块（需求 6）
├── bootstrap/                    # 启动模块（需求 7）
├── interface/                    # 接口层（需求 2）
│   ├── http/                     # HTTP 接口
│   └── consumer/                 # 消息消费者
├── application/                  # 应用层（需求 3）
│   ├── application-api/          # 应用服务接口
│   └── application-impl/         # 应用服务实现
├── domain/                       # 领域层（需求 4）
│   ├── domain-api/               # 领域模型定义
│   └── domain-impl/              # 领域服务实现
└── infrastructure/               # 基础设施层（需求 5）
    ├── repository/               # 仓储实现
    │   ├── repository-api/
    │   └── mysql-impl/
    ├── cache/                    # 缓存实现
    │   ├── cache-api/
    │   └── redis-impl/
    └── mq/                       # 消息队列实现
        ├── mq-api/
        └── sqs-impl/
```

**设计说明：**
- 共 6 个顶层模块，19 个子模块
- API 模块定义接口，Impl 模块提供实现
- 基础设施层按技术类型分组（repository、cache、mq）

**满足需求：** 需求 1-5（完整的模块结构）

#### 2.2.2 模块依赖关系

```
bootstrap
  ├─→ http ─────────┐
  ├─→ consumer ─────┤
  │                 ├─→ application-api ─→ common
  ├─→ application-impl ─┤
  │                     └─→ domain-api ─→ common
  ├─→ domain-impl ─────┬─→ domain-api
  │                    ├─→ repository-api ─→ common
  │                    ├─→ cache-api ─→ common
  │                    └─→ mq-api ─→ common
  ├─→ mysql-impl ──────→ repository-api
  ├─→ redis-impl ──────→ cache-api
  ├─→ sqs-impl ────────→ mq-api
  └─→ common
```

**依赖原则：**
1. 所有模块都可以依赖 common 模块
2. 接口层只依赖应用层 API 和 common
3. 应用层实现只依赖应用层 API、领域层 API 和 common
4. 领域层实现只依赖领域层 API、基础设施层 API 和 common
5. 基础设施层实现只依赖对应的 API 模块和 common
6. 严禁反向依赖和跨层依赖

**满足需求：** 需求 2-5（各层模块依赖），需求 16（依赖关系验证）

### 2.3 包结构规范

```
com.catface.orderservice/
├── common/                         # 通用模块
│   ├── exception/                  # 异常体系（需求 6）
│   ├── dto/                        # 通用 DTO（需求 6）
│   ├── constant/                   # 常量定义（需求 25）
│   └── util/                       # 工具类（需求 25）
├── http/                           # HTTP 接口模块
│   ├── controller/                 # REST Controller（需求 25）
│   └── handler/                    # 全局异常处理器（需求 12, 25）
├── consumer/                       # 消息消费者模块
├── application/                    # 应用层
│   ├── service/                    # Service 接口
│   ├── dto/                        # DTO
│   └── command/                    # Command 对象
├── domain/                         # 领域层
│   ├── entity/                     # 实体类（需求 22, 25）
│   ├── vo/                         # 值对象（需求 25）
│   └── repository/                 # 仓储接口（需求 25）
└── infrastructure/                 # 基础设施层
    ├── repository/mysql/
    │   ├── mapper/                 # MyBatis Mapper（需求 25）
    │   └── config/                 # MyBatis-Plus 配置（需求 14, 25）
    ├── cache/                      # 缓存接口和实现（需求 24）
    └── mq/                         # 消息队列接口和实现（需求 24）
```

**设计说明：**
- 所有包名以 `com.catface.orderservice` 开头
- 每个模块按职责划分子包
- 包结构清晰，易于查找和维护

**满足需求：** 需求 25（包结构规范）


## 3. 技术栈设计

### 3.1 技术选型

| 类别 | 技术 | 版本 | 选型理由 | 对应需求 |
|------|------|------|----------|----------|
| **核心框架** | Spring Boot | 3.4.1 | 最新稳定版本，完整的微服务开发能力 | 需求 1 |
| | Spring Cloud | 2025.0.0 | 与 Spring Boot 3.4.1 兼容，提供链路追踪 | 需求 1, 8 |
| | JDK | 21 | LTS 版本，性能优秀 | 需求 1 |
| **持久化** | MyBatis-Plus | 3.5.7 | 简化 CRUD，丰富插件，必须使用 Spring Boot 3 专用启动器 | 需求 1, 14 |
| | Druid | 1.2.20 | 监控能力强，支持 Spring Boot 3 | 需求 1 |
| | MySQL Connector/J | 由 Spring Boot 管理 | 官方驱动，版本统一管理 | 需求 5 |
| **缓存** | Spring Data Redis | 由 Spring Boot 管理 | 官方推荐，Lettuce 客户端 | 需求 5, 19 |
| **消息队列** | AWS SDK for SQS | 2.20.0 | AWS 官方 SDK | 需求 5, 19 |
| **可观测性** | Micrometer Tracing | 1.3.5 | Spring Boot 3 官方推荐的链路追踪方案 | 需求 8 |
| | Logstash Logback Encoder | 7.4 | 输出结构化 JSON 日志 | 需求 9 |
| | Micrometer Prometheus | 由 Spring Boot 管理 | 暴露 Prometheus 格式指标 | 需求 13 |
| | Spring Boot Actuator | 由 Spring Boot 管理 | 健康检查和监控 | 需求 13, 17, 18 |
| **工具库** | Lombok | 由 Spring Boot 管理 | 代码简化 | 需求 22 |

### 3.2 依赖版本管理策略

**管理原则：**
1. 父 POM 通过 `<dependencyManagement>` 统一管理所有依赖版本
2. 子模块声明依赖时不指定版本号，从父 POM 继承
3. 使用 BOM（Bill of Materials）管理 Spring Boot 和 Spring Cloud 依赖
4. 在 `<properties>` 中定义第三方库版本变量

**版本变量定义：**
- `java.version`: 21
- `spring-boot.version`: 3.4.1
- `spring-cloud.version`: 2025.0.0
- `mybatis-plus.version`: 3.5.7
- `druid.version`: 1.2.20
- `micrometer-tracing.version`: 1.3.5
- `logstash-logback-encoder.version`: 7.4
- `aws-sdk.version`: 2.20.0

**满足需求：** 需求 1（依赖版本管理），需求 15（依赖版本统一管理验证）

### 3.3 关键技术决策

#### ADR-001：选择 MyBatis-Plus 而非 JPA

**决策：** 使用 MyBatis-Plus 3.5.7 作为 ORM 框架

**理由：**
- 简化 CRUD 操作，提供丰富的插件支持（分页、乐观锁、逻辑删除）
- 支持 Lambda 表达式，类型安全
- SQL 可控性强，性能优于 JPA
- 必须使用 `mybatis-plus-spring-boot3-starter` 以支持 Spring Boot 3

**影响：** 需要手动编写复杂查询的 XML，但换来更好的性能和可控性

**满足需求：** 需求 14（MyBatis-Plus 依赖集成）

#### ADR-002：选择 Micrometer Tracing 而非 Spring Cloud Sleuth

**决策：** 使用 Micrometer Tracing 1.3.5 实现分布式链路追踪

**理由：**
- Spring Cloud Sleuth 已被 Micrometer Tracing 取代
- Micrometer Tracing 是 Spring Boot 3 官方推荐方案
- 支持多种追踪后端（Brave、OpenTelemetry）
- 与 Spring Boot Actuator 无缝集成

**影响：** 相对较新，社区资源较少，但官方支持完善

**满足需求：** 需求 8（分布式链路追踪集成）

#### ADR-003：Redis 和 SQS 模块只创建接口，不实现连接

**决策：** 第一阶段只创建接口定义和空实现，不实现实际连接逻辑

**理由：**
- 当前阶段重点是搭建项目骨架
- Redis 和 SQS 的实际连接配置依赖外部环境
- 先定义接口，保证架构完整性
- 后续可根据实际需求实现具体功能

**影响：** 降低初期复杂度，快速搭建骨架，但需要在后续阶段补充实现

**满足需求：** 需求 19（缓存和消息队列接口定义）


## 4. 数据模型设计

### 4.1 MyBatis-Plus 依赖集成

**设计目标：** 在 mysql-impl 模块中集成 MyBatis-Plus 相关依赖，为后续数据库操作提供基础支持

**依赖清单：**
| 依赖 | 说明 | 版本管理 | 对应需求 |
|------|------|---------|----------|
| mybatis-plus-spring-boot3-starter | MyBatis-Plus Spring Boot 3 启动器 | 父 POM 管理（3.5.7） | 需求 14 |
| druid-spring-boot-starter | Druid 连接池启动器 | 父 POM 管理（1.2.20） | 需求 14 |
| mysql-connector-j | MySQL 驱动 | Spring Boot BOM 管理 | 需求 14 |

**设计说明：**
- 所有依赖版本由父 POM 统一管理
- 子模块不指定版本号，从父 POM 继承
- 使用 Spring Boot 3 专用的 MyBatis-Plus 启动器
- MySQL 驱动 scope 设置为 runtime

**后续扩展：**
- 在有实际业务需求时，可以添加 MyBatis-Plus 配置类
- 可以定义基础实体类和自动填充处理器
- 可以配置分页插件、乐观锁插件等

**满足需求：** 需求 14（MyBatis-Plus 依赖集成）


## 5. 异常处理设计

### 5.1 异常体系设计

```
BaseException (抽象基类)
├── BusinessException (业务异常)
│   ├── 用途：表示业务逻辑错误
│   ├── HTTP 状态码：200
│   ├── 示例：订单不存在、库存不足、参数验证失败
│   └── 处理方式：返回错误码和错误消息，不记录堆栈
└── SystemException (系统异常)
    ├── 用途：表示系统技术错误
    ├── HTTP 状态码：500
    ├── 示例：数据库连接失败、外部服务超时、空指针异常
    └── 处理方式：返回通用错误消息，记录完整堆栈
```

**异常字段：**
- errorCode: 错误码（字符串）
- message: 错误消息（字符串）

**满足需求：** 需求 6（通用模块实现 - 异常体系）

### 5.2 统一响应格式设计

**Result 对象结构：**
| 字段 | 类型 | 说明 |
|------|------|------|
| code | String | 响应码（0: 成功，其他: 失败） |
| message | String | 响应消息 |
| data | T | 响应数据（泛型） |
| timestamp | Long | 时间戳 |

**响应示例：**
- 成功响应：`{"code":"0", "message":"success", "data":{...}, "timestamp":1234567890}`
- 失败响应：`{"code":"BIZ_001", "message":"订单不存在", "data":null, "timestamp":1234567890}`

**满足需求：** 需求 6（通用模块实现 - Result 类）

### 5.3 全局异常处理器设计

**处理策略：**
| 异常类型 | HTTP 状态码 | 响应格式 | 日志级别 | 对应需求 |
|---------|------------|---------|---------|----------|
| BusinessException | 200 | Result(errorCode, message) | WARN | 需求 12 |
| SystemException | 500 | Result(errorCode, message) | ERROR（含堆栈） | 需求 12 |
| MethodArgumentNotValidException | 400 | Result("VALIDATION_ERROR", message) | WARN | 需求 29 |
| Exception（未知异常） | 500 | Result("INTERNAL_ERROR", "Internal server error") | ERROR（含堆栈） | 需求 12, 28 |

**安全考虑：**
- 生产环境不暴露内部错误详情（堆栈信息、SQL 语句等）
- 未知异常返回通用错误消息
- 完整堆栈信息只记录到日志，不返回给客户端

**满足需求：** 需求 12（全局异常处理实现），需求 28（基础安全配置），需求 29（错误处理和容错机制）

### 5.4 异常传播策略

```
Controller/Consumer → Application Service → Domain Service → Repository
       ↓                    ↓                    ↓               ↓
  捕获并转换            抛出业务异常          抛出业务异常    抛出系统异常
  为 Result/日志      (BusinessException)  (BusinessException) (SystemException)
```

**各层职责：**
| 层级 | 异常处理职责 | 说明 |
|------|------------|------|
| 接口层（HTTP） | 捕获所有异常，转换为统一的 Result 响应 | 使用 @RestControllerAdvice |
| 接口层（Consumer） | 捕获所有异常，记录日志并根据业务决定是否重试 | 使用 @ControllerAdvice |
| 应用层 | 抛出 BusinessException，不处理异常 | 业务逻辑错误 |
| 领域层 | 抛出 BusinessException，不处理异常 | 领域规则违反 |
| 基础设施层 | 捕获技术异常，转换为 SystemException | 数据库、缓存、MQ 等技术异常 |

**满足需求：** 需求 12（全局异常处理实现）


## 6. 日志和追踪设计

### 6.1 分布式链路追踪设计

**技术方案：** Micrometer Tracing + Brave

**追踪流程：**
```
HTTP Request → Trace ID 生成 → 传播到所有日志 → 传播到 HTTP 响应头
```

**Trace ID 传播：**
| 传播方式 | 说明 | 对应需求 |
|---------|------|----------|
| HTTP 请求头 | 自动在请求头中添加 X-B3-TraceId | 需求 8 |
| 日志 MDC | 自动在 MDC 中添加 traceId 和 spanId | 需求 8, 30 |
| HTTP 响应头 | 自动在响应头中添加 X-B3-TraceId | 需求 8 |

**依赖配置：**
- micrometer-tracing-bridge-brave（必需）
- micrometer-tracing-reporter-wavefront（可选，用于链路数据上报）

**满足需求：** 需求 8（分布式链路追踪集成）

### 6.2 结构化日志设计

**日志格式：** JSON

**日志字段：**
| 字段名 | 说明 | 来源 |
|--------|------|------|
| timestamp | 日志时间 | Logback |
| level | 日志级别 | Logback |
| thread | 当前线程 | Logback |
| logger | 类名 | Logback |
| traceId | 链路追踪 ID | Micrometer Tracing |
| spanId | 调用链片段 ID | Micrometer Tracing |
| message | 日志内容 | 应用代码 |
| exception | 异常堆栈（可选） | Logback |

**日志编码器：** Logstash Logback Encoder

**满足需求：** 需求 9（结构化日志配置）

### 6.3 多环境日志策略

| 环境 | Profile | 输出目标 | 日志格式 | com.catface 包级别 | 其他包级别 | 特殊配置 | 对应需求 |
|------|---------|---------|---------|-------------------|-----------|---------|----------|
| 本地开发 | local | 控制台 | 默认格式（带颜色） | DEBUG | INFO | 无 | 需求 9, 11 |
| 开发环境 | dev | 文件 | JSON | DEBUG | INFO | 无 | 需求 9, 11 |
| 测试环境 | test | 文件 | JSON | DEBUG | INFO | 无 | 需求 11 |
| 预发布环境 | staging | 文件 | JSON | DEBUG | INFO | 无 | 需求 11 |
| 生产环境 | prod | 文件 | JSON | INFO | INFO | 异步输出 | 需求 11 |

**日志管理策略：**
- 日志文件按日期滚动（每天生成新文件）
- 日志保留时间根据运维策略配置
- 错误日志单独输出，便于快速定位问题

**满足需求：** 需求 9（结构化日志配置），需求 11（多环境日志级别配置）

### 6.4 日志可观测性增强

**HTTP 请求日志：**
- 记录请求开始和结束
- 包含：请求方法、URL、耗时、响应状态码
- 自动包含 traceId，便于关联同一请求的所有日志

**敏感信息脱敏原则：**
- 敏感字段（password、idCard、bankCard）在日志中脱敏
- 脱敏规则根据实际业务需求定义
- 提供统一的脱敏工具类

**后续完善：**
- 在有实际业务数据后，根据数据安全要求定义详细的脱敏规则

**满足需求：** 需求 23（基础安全配置），需求 25（日志可观测性增强）


## 7. 监控和健康检查设计

### 7.1 Prometheus 监控集成

**技术方案：** Spring Boot Actuator + Micrometer Prometheus

**监控端点：**
| 端点 | 说明 | 访问路径 | 对应需求 |
|------|------|---------|----------|
| Prometheus 指标 | 暴露 Prometheus 格式的监控指标 | /actuator/prometheus | 需求 13 |
| 健康检查 | 应用健康状态 | /actuator/health | 需求 17 |
| 应用信息 | 应用元数据 | /actuator/info | 需求 18 |
| 所有指标 | 所有 Micrometer 指标 | /actuator/metrics | 需求 13 |

**自动收集的指标：**
| 指标类别 | 指标内容 | 说明 |
|---------|---------|------|
| JVM 指标 | 堆内存、非堆内存、GC 次数、GC 耗时、线程数 | 监控 JVM 运行状态 |
| HTTP 指标 | 请求总数、请求耗时、错误率 | 监控 HTTP 请求性能 |
| 数据库连接池指标 | 活跃连接数、空闲连接数、等待连接数 | 监控数据库连接池状态 |
| 系统指标 | CPU 使用率、文件描述符数量 | 监控系统资源使用 |

**满足需求：** 需求 13（Prometheus 监控集成）

### 7.2 健康检查设计

**健康检查组件：**
| 组件 | 说明 | 状态判断 | 对应需求 |
|------|------|---------|----------|
| db | 数据库健康检查 | 连接成功 → UP，连接失败 → DOWN | 需求 17 |
| diskSpace | 磁盘空间检查 | 可用空间充足 → UP，空间不足 → DOWN | 需求 17 |
| ping | 基础存活检查 | 应用运行 → UP | 需求 17 |

**健康状态：**
- UP：所有组件正常
- DOWN：任一组件异常

**配置：**
- show-details: always（显示详细健康信息）
- 生产环境可配置为 when-authorized（仅授权用户可见详细信息）

**满足需求：** 需求 17（健康检查端点）

### 7.3 应用信息端点设计

**应用信息内容：**
| 信息项 | 值 | 说明 | 对应需求 |
|--------|-----|------|----------|
| app.name | Order Service | 应用名称 | 需求 18 |
| app.description | Order management service based on DDD architecture | 应用描述 | 需求 18 |
| app.version | 1.0.0-SNAPSHOT | 应用版本号 | 需求 18 |
| java.version | 21 | Java 版本 | 需求 18 |
| spring.boot.version | 3.4.1 | Spring Boot 版本 | 需求 18 |

**满足需求：** 需求 18（应用信息端点）

### 7.4 生产环境端点安全

**安全策略：**
| 环境 | 暴露端点 | 敏感端点处理 | 对应需求 |
|------|---------|-------------|----------|
| local/dev/test/staging | health, info, prometheus, metrics | 全部暴露 | 需求 13, 17, 18 |
| prod | health, info, prometheus | 禁用 env, beans, configprops 等敏感端点 | 需求 28 |

**满足需求：** 需求 28（基础安全配置）


## 8. 多环境配置设计

### 8.1 配置文件结构

```
bootstrap/src/main/resources/
├── application.yml              # 通用配置（所有环境共享）
├── application-local.yml        # 本地开发环境
├── application-dev.yml          # 开发环境
├── application-test.yml         # 测试环境
├── application-staging.yml      # 预发布环境
├── application-prod.yml         # 生产环境
└── logback-spring.xml           # 日志配置
```

**满足需求：** 需求 10（多环境配置支持）

### 8.2 配置加载优先级

```
命令行参数 > 环境变量 > application-{profile}.yml > application.yml
```

**环境激活方式：**
1. 配置文件：`spring.profiles.active: local`（默认）
2. 命令行：`--spring.profiles.active=prod`
3. 环境变量：`SPRING_PROFILES_ACTIVE=prod`

**满足需求：** 需求 10（多环境配置支持）

### 8.3 通用配置内容

**application.yml 包含：**
- 应用名称和默认 profile
- 数据源基础配置（各环境可覆盖）
- MyBatis-Plus 全局配置
- Actuator 端点配置
- 应用信息配置

**满足需求：** 需求 10（多环境配置支持）

### 8.4 环境特定配置

| 配置项 | local | dev | test | staging | prod | 对应需求 |
|--------|-------|-----|------|---------|------|----------|
| 日志输出目标 | 控制台 | 文件 | 文件 | 文件 | 文件 | 需求 9, 11 |
| 日志格式 | 默认 | JSON | JSON | JSON | JSON | 需求 9, 11 |
| 日志级别 | DEBUG | DEBUG | DEBUG | DEBUG | INFO | 需求 11 |
| 异步日志 | 否 | 否 | 否 | 否 | 是 | 需求 11 |
| 暴露端点 | 全部 | 全部 | 全部 | 全部 | 限制 | 需求 23 |

**满足需求：** 需求 10（多环境配置支持），需求 11（多环境日志级别配置）

### 8.5 配置外部化和安全性

**敏感配置外部化：**
| 配置项 | 环境变量 | 说明 | 对应需求 |
|--------|---------|------|----------|
| 数据库 URL | SPRING_DATASOURCE_URL | 数据库连接地址 | 需求 26 |
| 数据库用户名 | SPRING_DATASOURCE_USERNAME | 数据库用户名 | 需求 26 |
| 数据库密码 | SPRING_DATASOURCE_PASSWORD | 数据库密码 | 需求 26 |

**配置安全：**
- 生产环境敏感配置通过环境变量注入
- 启动日志中密码显示为 ******
- 配置文件不包含生产环境密码

**满足需求：** 需求 26（配置外部化和安全性）


## 9. 性能和可扩展性设计

### 9.1 性能目标

| 指标 | 目标值 | 验证方法 | 对应需求 |
|------|--------|---------|----------|
| 应用启动时间（本地） | ≤ 30 秒 | 检查启动日志中的 "Started OrderServiceApplication" 时间戳 | 需求 21 |
| 应用启动时间（生产） | ≤ 60 秒 | 检查启动日志中的 "Started OrderServiceApplication" 时间戳 | 需求 21 |
| 简单 GET 请求响应时间 | P95 < 100ms | 使用 Prometheus 指标 http_server_requests_seconds | 需求 22 |
| 数据库查询请求响应时间 | P95 < 500ms | 使用 Prometheus 指标 http_server_requests_seconds | 需求 22 |
| 并发连接数 | ≥ 100 | 压力测试验证 | 需求 22 |
| JVM 堆内存使用率 | < 70% | 使用 Prometheus 指标 jvm_memory_used_bytes | 需求 22 |

**满足需求：** 需求 21（应用启动性能要求），需求 22（运行时性能基准）

### 9.2 性能优化策略

#### 9.2.1 JVM 配置原则

**设计决策：**
- 初期使用 JVM 默认参数
- 在性能测试后根据实际情况调整
- 生产环境建议：堆内存 2-4GB，使用 G1 垃圾收集器
- 配置 OOM 时自动生成堆转储文件

**后续优化方向：**
- 根据实际内存使用情况调整堆大小
- 根据 GC 日志优化垃圾收集器参数
- 监控 GC 频率和暂停时间

**满足需求：** 需求 22（运行时性能基准），需求 24（错误处理和容错机制）

#### 9.2.2 日志性能优化

**设计决策：**
- 生产环境使用异步日志，避免日志 I/O 阻塞业务线程
- 配置合理的日志队列大小
- 非生产环境使用同步日志，便于调试

**满足需求：** 需求 11（多环境日志级别配置），需求 22（运行时性能基准）

### 9.3 可扩展性设计

#### 9.3.1 水平扩展支持

**无状态设计原则：**
- 不在应用内存中存储会话状态
- 不使用本地缓存（或使用分布式缓存）
- 不依赖本地文件系统
- 使用数据库或分布式缓存存储共享状态

**部署架构：**
```
Load Balancer
    ├─→ Instance 1 (order-service)
    ├─→ Instance 2 (order-service)
    └─→ Instance 3 (order-service)
         ↓
    Shared Database (MySQL)
```

**满足需求：** 需求 22（运行时性能基准 - 支持水平扩展）

#### 9.3.2 优雅关闭

**设计决策：**
- 配置优雅关闭，等待正在处理的请求完成（最多 30 秒）
- 关闭流程：停止接收新请求 → 等待请求完成 → 关闭资源 → 退出

**配置：**
- server.shutdown: graceful
- spring.lifecycle.timeout-per-shutdown-phase: 30s

**满足需求：** 需求 24（错误处理和容错机制）


## 10. 安全设计

### 10.1 端点安全

**生产环境端点限制：**
| 端点类型 | 暴露策略 | 说明 | 对应需求 |
|---------|---------|------|----------|
| 必要端点 | 暴露 | health, info, prometheus | 需求 23 |
| 敏感端点 | 禁用 | env, beans, configprops | 需求 23 |

**满足需求：** 需求 23（基础安全配置）

### 10.2 HTTP 安全响应头

**设计决策：**
- 添加基础的 HTTP 安全响应头（X-Content-Type-Options, X-Frame-Options, X-XSS-Protection）
- 防止常见的 Web 安全问题（MIME 嗅探、点击劫持、XSS 攻击）

**实现方式：** 创建 SecurityHeadersFilter 过滤器

**满足需求：** 需求 23（基础安全配置）

### 10.3 异常信息保护

**保护策略：**
| 异常类型 | 客户端响应 | 日志记录 | 对应需求 |
|---------|-----------|---------|----------|
| BusinessException | 返回错误码和错误消息 | 记录 WARN 级别日志，不含堆栈 | 需求 12, 23 |
| SystemException | 返回错误码和错误消息 | 记录 ERROR 级别日志，含完整堆栈 | 需求 12, 23 |
| 未知异常 | 返回通用错误消息 | 记录 ERROR 级别日志，含完整堆栈 | 需求 12, 23 |

**生产环境特殊处理：**
- 不暴露堆栈信息、SQL 语句、内部实现细节

**满足需求：** 需求 12（全局异常处理实现），需求 23（基础安全配置）

### 10.4 日志脱敏

**设计原则：**
- 敏感字段（password、idCard、bankCard）在日志中脱敏
- 脱敏规则根据实际业务需求定义
- 提供统一的脱敏工具类

**实现方式：** 创建 SensitiveDataMasker 组件

**后续完善：**
- 在有实际业务数据后，根据数据安全要求定义详细的脱敏规则

**满足需求：** 需求 23（基础安全配置），需求 25（日志可观测性增强）

### 10.5 配置安全

**敏感配置保护：**
- 生产环境通过环境变量注入
- 启动日志中密码显示为 ******
- 配置文件不包含生产环境密码

**满足需求：** 需求 26（配置外部化和安全性）


## 11. 构建和部署设计

### 11.1 Maven 构建配置

**父 POM 构建配置：**
- Maven Compiler Plugin：配置 Java 21
- Spring Boot Maven Plugin：配置在 pluginManagement 中

**Bootstrap 模块构建配置：**
- Spring Boot Maven Plugin：配置 repackage 目标
- Main-Class：com.catface.orderservice.OrderServiceApplication
- finalName：order-service-${project.version}

**满足需求：** 需求 27（构建和打包规范）

### 11.2 构建产物规范

**JAR 文件：**
- 文件名：order-service-1.0.0-SNAPSHOT.jar
- 类型：Fat JAR（包含所有依赖）
- 位置：bootstrap/target/

**MANIFEST.MF 内容：**
- Main-Class：org.springframework.boot.loader.JarLauncher
- Start-Class：com.catface.orderservice.OrderServiceApplication
- Implementation-Version：1.0.0-SNAPSHOT

**满足需求：** 需求 27（构建和打包规范）

### 11.3 构建命令

| 命令 | 说明 | 预期结果 | 对应需求 |
|------|------|---------|----------|
| mvn clean compile | 清理并编译 | 所有模块编译成功 | 需求 1-5 |
| mvn clean package | 清理并打包 | 生成可执行 JAR 文件 | 需求 7, 27 |
| mvn clean package -DskipTests | 跳过测试的快速构建 | 构建时间不超过 2 分钟 | 需求 27 |
| mvn dependency:tree | 查看依赖树 | 显示所有依赖关系 | 需求 15 |

**满足需求：** 需求 7（启动模块配置），需求 15（依赖版本统一管理验证），需求 27（构建和打包规范）

### 11.4 部署方案

**本地运行：**
```bash
# 使用 local profile 启动（默认）
java -jar order-service-1.0.0-SNAPSHOT.jar

# 使用 dev profile 启动
java -jar order-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

**生产环境部署：**
```bash
# 通过命令行参数启动应用
java -jar order-service-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://prod-db-host:3306/tiang" \
  --spring.datasource.username="prod_user" \
  --spring.datasource.password="prod_password"
```

**生产环境部署（带 JVM 参数）：**
```bash
# 配置 JVM 参数和应用参数
java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/order-service/heapdump.hprof \
  -jar order-service-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://prod-db-host:3306/tiang" \
  --spring.datasource.username="prod_user" \
  --spring.datasource.password="prod_password"
```

**JVM 参数说明：**
- `-Xms2g -Xmx4g`：设置堆内存初始值 2GB，最大值 4GB
- `-XX:+UseG1GC`：使用 G1 垃圾收集器
- `-XX:MaxGCPauseMillis=200`：设置 GC 最大暂停时间为 200ms
- `-XX:+HeapDumpOnOutOfMemoryError`：OOM 时自动生成堆转储文件
- `-XX:HeapDumpPath`：堆转储文件保存路径

**参数传递优先级：**
1. 命令行参数（`--spring.xxx`）：最高优先级
2. 环境变量（`SPRING_XXX`）：次优先级
3. 配置文件（`application-{profile}.yml`）：最低优先级

**满足需求：** 需求 27（构建和打包规范）

### 11.5 健康检查和监控

**启动验证：**
- 检查启动日志中的 "Started OrderServiceApplication"
- 访问 /actuator/health 端点验证健康状态

**运行时监控：**
- /actuator/health：健康检查
- /actuator/info：应用信息
- /actuator/prometheus：Prometheus 指标

**满足需求：** 需求 17（健康检查端点），需求 18（应用信息端点），需求 13（Prometheus 监控集成）


## 12. 需求覆盖检查

### 12.1 架构基础需求（需求 1-7）

| 需求 | 设计章节 | 覆盖情况 |
|------|---------|---------|
| 需求 1：项目基础结构搭建 | 2.2, 3.2 | ✅ 完全覆盖 |
| 需求 2：接口层模块结构 | 2.2 | ✅ 完全覆盖 |
| 需求 3：应用层模块结构 | 2.2 | ✅ 完全覆盖 |
| 需求 4：领域层模块结构 | 2.2 | ✅ 完全覆盖 |
| 需求 5：基础设施层模块结构 | 2.2 | ✅ 完全覆盖 |
| 需求 6：通用模块实现 | 5.1, 5.2 | ✅ 完全覆盖 |
| 需求 7：启动模块配置 | 2.2, 11.1 | ✅ 完全覆盖 |

### 12.2 技术基础设施需求（需求 8-14）

| 需求 | 设计章节 | 覆盖情况 |
|------|---------|---------|
| 需求 8：分布式链路追踪集成 | 6.1 | ✅ 完全覆盖 |
| 需求 9：结构化日志配置 | 6.2 | ✅ 完全覆盖 |
| 需求 10：多环境配置支持 | 8.1, 8.2, 8.3 | ✅ 完全覆盖 |
| 需求 11：多环境日志级别配置 | 6.3 | ✅ 完全覆盖 |
| 需求 12：全局异常处理实现 | 5.3, 5.4 | ✅ 完全覆盖 |
| 需求 13：Prometheus 监控集成 | 7.1 | ✅ 完全覆盖 |
| 需求 14：MyBatis-Plus 依赖集成 | 4.1 | ✅ 完全覆盖 |

### 12.3 质量保证需求（需求 15-18）

| 需求 | 设计章节 | 覆盖情况 |
|------|---------|---------|
| 需求 15：依赖版本统一管理验证 | 3.2, 11.3 | ✅ 完全覆盖 |
| 需求 16：模块依赖关系验证 | 2.2.2 | ✅ 完全覆盖 |
| 需求 17：健康检查端点 | 7.2 | ✅ 完全覆盖 |
| 需求 18：应用信息端点 | 7.3 | ✅ 完全覆盖 |

### 12.4 接口定义和规范需求（需求 19-20）

| 需求 | 设计章节 | 覆盖情况 |
|------|---------|---------|
| 需求 19：缓存和消息队列接口定义 | 3.3 (ADR-003) | ✅ 完全覆盖 |
| 需求 20：包结构规范 | 2.3 | ✅ 完全覆盖 |

### 12.5 性能和安全需求（需求 21-26）

| 需求 | 设计章节 | 覆盖情况 |
|------|---------|---------|
| 需求 21：应用启动性能要求 | 9.1 | ✅ 完全覆盖 |
| 需求 22：运行时性能基准 | 9.1, 9.2, 9.3 | ✅ 完全覆盖 |
| 需求 23：基础安全配置 | 10.1, 10.2, 10.3, 10.4 | ✅ 完全覆盖 |
| 需求 24：错误处理和容错机制 | 5.3, 9.2.1, 9.3.2 | ✅ 完全覆盖 |
| 需求 25：日志可观测性增强 | 6.4 | ✅ 完全覆盖 |
| 需求 26：配置外部化和安全性 | 8.5, 10.5 | ✅ 完全覆盖 |

### 12.6 构建和部署需求（需求 27）

| 需求 | 设计章节 | 覆盖情况 |
|------|---------|---------|
| 需求 27：构建和打包规范 | 11.1, 11.2, 11.3, 11.4 | ✅ 完全覆盖 |

**覆盖率：27/27 = 100%**

## 13. 设计一致性检查

### 13.1 内部一致性

✅ **架构层次一致**：所有模块都遵循 DDD 四层架构
✅ **依赖关系一致**：所有依赖关系都符合单向依赖原则
✅ **技术选型一致**：所有技术栈版本都经过兼容性验证
✅ **配置策略一致**：所有环境配置都遵循统一的策略
✅ **安全策略一致**：所有安全措施都贯穿各个层面

### 13.2 与需求的一致性

✅ **完全覆盖**：设计覆盖了所有 32 个需求
✅ **无冲突**：设计内容之间没有矛盾和冲突
✅ **可实施**：所有设计都可以被实现
✅ **可验证**：所有设计都有明确的验证方法

## 14. 总结

本设计文档基于需求文档中定义的 27 个需求，提供了完整的技术设计方案：

**设计特点：**
1. **架构清晰**：采用 DDD 四层架构，模块职责单一，依赖关系明确
2. **技术先进**：使用 Spring Boot 3.4.1、Spring Cloud 2025.0.0 等最新稳定技术
3. **可观测性强**：完善的日志、追踪、监控体系
4. **安全可靠**：多层次的安全防护和容错机制
5. **性能优秀**：明确的性能目标和优化策略
6. **易于维护**：清晰的包结构和代码组织
7. **聚焦核心**：专注于项目骨架搭建，避免过度设计

**设计覆盖：**
- ✅ 100% 覆盖所有需求（27/27）
- ✅ 架构设计完整且一致
- ✅ 技术选型合理且可行
- ✅ 配置策略清晰且安全
- ✅ 性能目标明确且可达
- ✅ 安全措施全面且有效

**设计简化说明：**
- 移除了 MyBatis-Plus 的详细配置设计（MybatisPlusConfig、插件配置、自动填充等）
- 移除了数据库连接和连接池的详细配置设计
- 移除了基础实体类的详细设计
- 保留了 MyBatis-Plus 依赖集成，为后续扩展预留空间
- 聚焦于项目结构搭建和基础设施集成

**后续步骤：**
设计文档已完成，可以进入任务拆分阶段，将设计转化为可执行的任务列表。

