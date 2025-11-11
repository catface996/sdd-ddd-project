# 设计文档

## 概述

本文档描述 OrderCore 系统的技术设计方案，包括 Maven 模块结构、依赖配置、核心组件设计、日志配置、监控集成等技术实现细节。

## 架构设计

### 系统架构

OrderCore 采用 DDD 分层架构，通过 Maven 多模块实现模块化和依赖隔离：

```
┌─────────────────────────────────────────────────────────┐
│                      Bootstrap                          │
│                   (启动和配置)                           │
└─────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼──────┐  ┌───────▼────────┐
│  Interface     │  │ Application │  │   Domain       │
│  (HTTP/MQ)     │  │   (编排)    │  │  (业务规则)    │
└───────┬────────┘  └──────┬──────┘  └───────┬────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                  ┌────────▼────────┐
                  │ Infrastructure  │
                  │ (技术实现)      │
                  └─────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼──────┐  ┌───────▼────────┐
│  Repository    │  │    Cache    │  │      MQ        │
│  (MySQL)       │  │   (Redis)   │  │    (SQS)       │
└────────────────┘  └─────────────┘  └────────────────┘
```

### 依赖关系

- **单向依赖**：外层依赖内层，内层不依赖外层
- **接口隔离**：通过 *-api 模块定义接口，*-impl 模块实现
- **依赖倒置**：高层模块依赖抽象接口，不依赖具体实现

## Maven 模块设计

### 父 POM 配置

**文件位置**：`pom.xml`

**关键配置**：

- **groupId**: `com.demo`
- **artifactId**: `order-core-parent`
- **packaging**: `pom`
- **Java 版本**: 21
- **Spring Boot 版本**: 3.3.x（最新稳定版）
- **Spring Cloud 版本**: 2024.0.x (Leyton)

**依赖管理策略**：
1. 使用 `<dependencyManagement>` 导入 Spring Boot 和 Spring Cloud BOM
2. 在 `<properties>` 中定义第三方库版本（MyBatis-Plus、AWS SDK 等）
3. 子模块声明依赖时不指定版本，由父 POM 统一管理

**模块声明顺序**：
1. common - 通用模块（最底层，无业务依赖）
2. interface - 接口层父模块
3. application - 应用层父模块
4. domain - 领域层父模块
5. infrastructure - 基础设施层父模块
6. bootstrap - 启动模块（最顶层，依赖所有其他模块）

**说明**: 模块声明顺序遵循依赖关系，被依赖的模块在前，依赖其他模块的在后

### 模块层次结构

#### 1. Common 模块

**路径**: `common/`
**packaging**: `jar`
**职责**: 提供通用工具类、异常定义、DTO、常量

**包结构**:
- `com.demo.ordercore.common.exception` - 异常类
- `com.demo.ordercore.common.dto` - 通用 DTO
- `com.demo.ordercore.common.constant` - 常量定义
- `com.demo.ordercore.common.util` - 工具类

**依赖**: Lombok

#### 2. Interface 层

**路径**: `interface/`
**packaging**: `pom`

**子模块**:
- **http**: 处理 HTTP 请求
  - packaging: `jar`
  - 依赖: Spring Web, Spring Validation, application-api, common
  - 包含: Controller、全局异常处理器
  
- **consumer**: 处理消息队列消费
  - packaging: `jar`
  - 依赖: application-api, common
  - 包含: 消息监听器、全局异常处理器

#### 3. Application 层

**路径**: `application/`
**packaging**: `pom`

**子模块**:
- **application-api**: 应用服务接口定义
  - packaging: `jar`
  - 依赖: common
  - 包含: 服务接口、Command、Query、DTO
  
- **application-impl**: 应用服务实现
  - packaging: `jar`
  - 依赖: application-api, domain-api, common
  - 包含: 服务实现类、业务编排逻辑

#### 4. Domain 层

**路径**: `domain/`
**packaging**: `pom`

**子模块**:
- **domain-api**: 领域模型定义
  - packaging: `jar`
  - 依赖: common
  - 包含: 实体、值对象、领域事件、仓储接口
  
- **domain-impl**: 领域服务实现
  - packaging: `jar`
  - 依赖: domain-api, repository-api, cache-api, mq-api, common
  - 包含: 领域服务、业务规则实现

#### 5. Infrastructure 层

**路径**: `infrastructure/`
**packaging**: `pom`

**子模块结构**:


- **repository** (pom)
  - **repository-api** (jar): 仓储接口定义，依赖 common
  - **mysql-impl** (jar): MyBatis-Plus 实现，依赖 repository-api, common, MyBatis-Plus

- **cache** (pom)
  - **cache-api** (jar): 缓存接口定义，依赖 common
  - **redis-impl** (jar): Redis 实现，依赖 cache-api, common, Spring Data Redis

- **mq** (pom)
  - **mq-api** (jar): 消息队列接口定义，依赖 common
  - **sqs-impl** (jar): AWS SQS 实现，依赖 mq-api, common, AWS SDK

#### 6. Bootstrap 模块

**路径**: `bootstrap/`
**packaging**: `jar`
**职责**: 应用启动入口

**依赖**: 
- Spring Boot Starter Web
- Spring Boot Actuator
- Micrometer Registry Prometheus
- http, consumer, application-impl, domain-impl
- mysql-impl, redis-impl, sqs-impl, common

**包含**:
- 主启动类: `com.demo.ordercore.bootstrap.OrderCoreApplication`
- 配置文件: application.yml, application-*.yml, bootstrap.yml, logback-spring.xml

## 核心组件设计

### 异常体系

**位置**: `common/src/main/java/com/demo/common/exception/`

**类层次结构**:
```
RuntimeException
    └── BaseException (abstract)
            ├── BusinessException
            └── SystemException
```

**BaseException 设计**:
- 继承自 RuntimeException
- 包含 errorCode 字段（String 类型），用于标识错误类型
- 包含 message 字段（继承自父类），用于描述错误信息
- 定义为抽象类，提供受保护的构造函数供子类使用
- 使用 Lombok @Getter 注解提供字段访问器

**BusinessException 设计**:
- 继承自 BaseException
- 用于表示业务逻辑错误（如参数验证失败、业务规则违反）
- HTTP 接口层捕获后返回 400 状态码

**SystemException 设计**:
- 继承自 BaseException
- 用于表示系统技术错误（如数据库连接失败、外部服务调用失败）
- HTTP 接口层捕获后返回 500 状态码

### 统一响应类

**位置**: `common/src/main/java/com/demo/common/dto/Result.java`

**设计**:
- 泛型类，支持不同类型的响应数据
- 包含以下字段：
  - code (String): 响应码，成功为 "0000"，失败为具体错误码
  - message (String): 响应消息
  - data (T): 响应数据，泛型类型
  - timestamp (Long): 响应时间戳
- 提供静态工厂方法：
  - success(T data): 创建成功响应
  - error(String code, String message): 创建错误响应
- 使用 Lombok @Data 和 @Builder 注解简化代码

### 全局异常处理器

#### HTTP 异常处理器

**位置**: `interface/http/src/main/java/com/demo/http/handler/GlobalExceptionHandler.java`

**设计**:
- 使用 @RestControllerAdvice 注解标记为全局异常处理器
- 使用 @Slf4j 注解提供日志记录能力
- 异常处理方法：
  - handleBusinessException: 处理 BusinessException
    - 记录 WARN 级别日志
    - 返回 HTTP 400 状态码
    - 返回 Result 对象，包含 errorCode、message、timestamp
  - handleSystemException: 处理 SystemException
    - 记录 ERROR 级别日志
    - 返回 HTTP 500 状态码
    - 返回 Result 对象，包含 errorCode、message、timestamp
  - handleException: 处理所有未捕获的异常
    - 记录 ERROR 级别日志，包含完整堆栈信息
    - 返回 HTTP 500 状态码
    - 返回 Result 对象，message 使用通用错误提示，不暴露堆栈信息

#### Consumer 异常处理器

**位置**: `interface/consumer/src/main/java/com/demo/consumer/handler/GlobalExceptionHandler.java`

**设计**:
- 使用 @ControllerAdvice 注解标记为全局异常处理器
- 使用 @Slf4j 注解提供日志记录能力
- 异常处理策略：
  - 捕获所有消息处理过程中的异常
  - 记录 ERROR 级别日志，包含消息内容和异常堆栈
  - 根据异常类型决定是否重试：
    - BusinessException: 不重试，记录日志后丢弃消息
    - SystemException: 可重试，根据重试策略决定
  - 确保异常不会导致消息监听器停止

## 日志与链路追踪设计

### Logback 配置

**位置**: `bootstrap/src/main/resources/logback-spring.xml`

**设计要点**:


1. **使用 Spring Profile 区分环境**:
   - `<springProfile name="local">`: 控制台输出，彩色格式
   - `<springProfile name="dev,test,staging">`: 文件输出，JSON 格式
   - `<springProfile name="prod">`: 文件输出，JSON 格式，异步 Appender

2. **日志级别配置**:
   - local/dev/test/staging: `com.demo` 包 DEBUG 级别
   - prod: 所有包 INFO 级别

3. **日志输出目标**:
   - `logs/application.log`: 所有日志
   - `logs/error.log`: ERROR 级别日志单独记录

4. **滚动策略**:
   - 按日期滚动: `%d{yyyy-MM-dd}`
   - 按大小滚动: 单文件超过 100MB 分割
   - 保留策略: 非生产 30 天，生产 90 天

5. **JSON 格式配置**:
   - 使用 `logstash-logback-encoder`
   - 包含字段: timestamp, level, thread, logger, traceId, spanId, message, exception

### 链路追踪集成

**依赖**: Micrometer Tracing (Spring Cloud Sleuth 的替代品)

**配置位置**: `bootstrap/src/main/resources/application.yml`

**关键配置**:
- 配置链路追踪采样率为 100%（开发和测试环境）
- 生产环境可根据实际情况调整采样率
- 配置路径: management.tracing.sampling.probability

**自动功能**:
- 自动生成 Trace ID 和 Span ID
- 自动传播到日志 MDC
- 自动在 HTTP 请求头中传播

## 监控与指标设计

### Actuator 端点配置

**配置位置**: `bootstrap/src/main/resources/application.yml`

**关键配置**:
- 暴露 Actuator 端点: health 和 prometheus
- health 端点配置：
  - 显示详细健康信息（show-details: always）
  - 包含磁盘空间、数据库连接等检查项
- 配置路径: management.endpoints.web.exposure.include

### Prometheus 集成

**依赖**: Micrometer Registry Prometheus

**暴露端点**: `/actuator/prometheus`

**指标类型**:
1. **JVM 指标**:
   - `jvm_memory_used_bytes`: 内存使用
   - `jvm_gc_pause_seconds`: GC 暂停时间
   - `jvm_threads_live`: 活跃线程数

2. **HTTP 指标**:
   - `http_server_requests_seconds`: 请求响应时间
   - `http_server_requests_seconds_count`: 请求总数
   - `http_server_requests_seconds_sum`: 请求总耗时

3. **自定义指标**:
   - 可通过 `MeterRegistry` 添加业务指标

## 多环境配置设计

### 配置文件结构

**位置**: `bootstrap/src/main/resources/`

**文件清单**:
- `application.yml`: 通用配置
- `application-local.yml`: 本地开发环境
- `application-dev.yml`: 开发环境
- `application-test.yml`: 测试环境
- `application-staging.yml`: 预发布环境
- `application-prod.yml`: 生产环境
- `bootstrap.yml`: 引导配置（预留）

### application.yml 设计

**通用配置内容**:
- 应用名称: order-core
- 默认激活环境: dev
- 服务器端口: 8080
- Actuator 端点暴露: health, prometheus
- 其他通用配置（如字符编码、时区等）

### 环境特定配置

**application-dev.yml 配置内容**:
- 日志级别: com.demo 包使用 DEBUG 级别
- 其他开发环境特定配置

**application-prod.yml 配置内容**:
- 日志级别: 所有包使用 INFO 级别
- 其他生产环境特定配置（如连接池大小、超时时间等）

## 依赖版本管理

### 父 POM 版本定义

**properties 节定义的版本**:
- Java 版本: 21
- Maven 编译器源码和目标版本: 21
- Spring Boot 版本: 3.3.5（或最新 3.3.x 稳定版）
- Spring Cloud 版本: 2024.0.0（Leyton 发布系列）
- MyBatis-Plus 版本: 3.5.5（或最新 3.5.x 稳定版）
- AWS SDK 版本: 2.20.0（或最新 2.x 稳定版）
- Logstash Logback Encoder 版本: 7.4（或最新 7.x 稳定版）

### dependencyManagement 配置

**BOM 导入策略**:
- 导入 Spring Boot Dependencies BOM
  - groupId: org.springframework.boot
  - artifactId: spring-boot-dependencies
  - 使用 import scope 导入
- 导入 Spring Cloud Dependencies BOM
  - groupId: org.springframework.cloud
  - artifactId: spring-cloud-dependencies
  - 使用 import scope 导入
- 第三方库版本管理
  - MyBatis-Plus Boot Starter: 在 dependencyManagement 中声明版本
  - AWS SDK for SQS: 在 dependencyManagement 中声明版本
  - Logstash Logback Encoder: 在 dependencyManagement 中声明版本
  - Lombok: 由 Spring Boot BOM 管理

## 构建配置

### Maven 编译插件

**父 POM 配置**:
- 使用 pluginManagement 统一管理插件版本
- Maven Compiler Plugin 配置:
  - 版本: 3.11.0（或最新稳定版）
  - 源码版本: Java 21
  - 目标版本: Java 21
  - 确保所有子模块使用相同的编译配置

### Spring Boot 打包插件

**Bootstrap 模块配置**:
- 使用 Spring Boot Maven Plugin 打包
- 配置主启动类: com.demo.ordercore.bootstrap.OrderCoreApplication
- 打包类型: 可执行 JAR（包含所有依赖）
- 生成的 JAR 文件命名格式: order-core-bootstrap-{version}.jar

## 技术决策记录 (ADR)

### ADR-001: 选择 Lettuce 作为 Redis 客户端

**状态**: 已接受

**背景**: 需要选择 Redis 客户端实现缓存功能

**决策**: 使用 Spring Data Redis (Lettuce)

**理由**:
- Spring Boot 官方推荐
- 支持异步和响应式编程
- 线程安全，性能优秀
- 与 Spring 生态集成良好

**后果**:
- 正面: 开发效率高，维护成本低
- 负面: 无

### ADR-002: 使用 Micrometer Tracing 替代 Spring Cloud Sleuth

**状态**: 已接受

**背景**: Spring Cloud Sleuth 在 Spring Boot 3.x 中已被 Micrometer Tracing 替代

**决策**: 使用 Micrometer Tracing

**理由**:
- Spring Boot 3.x 官方推荐
- 更好的可观测性支持
- 统一的指标和追踪 API

**后果**:
- 正面: 与 Spring Boot 3.x 完全兼容
- 负面: 需要学习新 API

### ADR-003: 采用 DDD 分层架构

**状态**: 已接受

**背景**: 需要确定系统架构模式

**决策**: 采用 DDD 分层架构（接口层、应用层、领域层、基础设施层）

**理由**:
- 清晰的职责划分
- 业务逻辑与技术实现解耦
- 易于测试和维护
- 支持未来微服务拆分

**后果**:
- 正面: 架构清晰，可维护性强
- 负面: 初期开发需要更多模块配置

## 风险与应对

### 风险 1: Maven 模块依赖复杂度

**风险描述**: 多模块项目依赖关系复杂，可能导致循环依赖

**应对策略**:
- 严格遵循 DDD 分层原则
- 使用 *-api 和 *-impl 分离接口和实现
- 定期执行 `mvn dependency:tree` 检查依赖关系

### 风险 2: 日志配置错误

**风险描述**: 多环境日志配置可能导致生产环境日志过多或过少

**应对策略**:
- 在测试环境充分验证日志配置
- 使用异步日志避免影响性能
- 定期审查日志级别配置

### 风险 3: 版本冲突

**风险描述**: 第三方库版本冲突可能导致运行时错误

**应对策略**:
- 使用 BOM 统一管理版本
- 子模块不指定版本号
- 定期更新依赖版本

## 总结

本设计文档提供了 OrderCore 系统的详细技术实现方案，包括：

1. **模块化设计**: 15 个 Maven 模块，清晰的 DDD 分层
2. **异常处理**: 统一的异常体系和全局异常处理器
3. **日志追踪**: 多环境日志配置和分布式链路追踪
4. **监控集成**: Prometheus 指标暴露和 Actuator 端点
5. **配置管理**: 多环境配置和统一依赖管理

所有设计决策都基于需求文档，确保满足功能和非功能性需求。
