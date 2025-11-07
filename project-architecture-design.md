# 📘 项目架构设计说明书（需求级）

## 一、项目概述

### 1.1 项目名称  
**多模块 DDD 架构工程（基于 Spring Cloud）**

### 1.2 项目目标  
本项目旨在构建一个基于 **Spring Cloud 最新稳定版本** 的多模块 Maven 工程，采用 **领域驱动设计（DDD）** 思想，以实现：
- 清晰的层次划分与模块边界；
- 技术与业务逻辑的解耦；
- 可扩展、可演进、可替换的系统结构；
- 对 Spring Cloud 微服务生态的良好支持。

---

## 二、架构设计原则

| 原则 | 说明 |
|------|------|
| **领域驱动** | 聚焦核心领域模型，将复杂度控制在领域内部。 |
| **分层隔离** | 严格遵循 DDD 的层次边界：接口层、应用层、领域层、基础设施层。 |
| **单向依赖** | 外层依赖内层，内层永不反向依赖。 |
| **可替换性** | 各技术实现模块可独立替换，不影响上层逻辑。 |
| **轻量与扩展性** | 模块拆分合理，支持独立开发与微服务化演进。 |
| **统一依赖管理** | 通过父 POM 统一 Spring、MyBatis-Plus、Jedis 等版本。 |

---

## 三、整体架构概览

### 3.1 模块结构

```
project-root/
├── common/                              # 公共模块（工具类、通用异常、常量）
│
├── bootstrap/                           # 启动模块
│
├── traffic/                             # 接口层
│   ├── http/                            # 提供 HTTP API
│   └── consumer/                        # 消费消息队列
│
├── application/                         # 应用层
│   ├── application-api/                 # 应用服务接口定义
│   └── application-impl/                # 应用服务实现
│
├── domain/                              # 领域层
│   ├── domain-api/                      # 领域模型、聚合根、仓储接口定义
│   └── domain-impl/                     # 领域逻辑实现
│
└── infrastructure/                      # 基础设施层
    ├── repository/
    │   ├── repository-api/              # 仓储接口定义
    │   └── mysql-impl/                  # 基于 MyBatis-Plus 的实现
    │
    ├── cache/
    │   ├── cache-api/                   # 缓存接口定义
    │   └── redis-impl/                  # 基于 Jedis 的实现
    │
    └── mq/
        ├── mq-api/                      # 消息队列接口定义
        └── sqs-impl/                    # 基于 AWS SQS 的实现
```

---

## 四、模块职责说明

| 层级 | 模块 | 职责描述 |
|------|------|----------|
| **通用层** | `common` | 提供项目通用工具类、枚举、异常定义、通用DTO等。 |
| **启动层** | `bootstrap` | 系统启动入口，负责加载配置、装配依赖、启动Spring上下文。 |
| **接口层** | `traffic/http` | 处理外部HTTP请求，进行参数校验与输入输出转换。 |
|  | `traffic/consumer` | 接收并处理来自消息队列的事件或异步任务。 |
| **应用层** | `application-api` | 定义应用服务接口、DTO、Command、Query对象。 |
|  | `application-impl` | 实现业务用例编排，协调领域层完成业务逻辑。 |
| **领域层** | `domain-api` | 定义领域模型、聚合、实体、值对象、仓储接口、领域事件。 |
|  | `domain-impl` | 实现领域服务逻辑，封装核心业务规则。 |
| **基础设施层** | `repository-api` | 定义数据持久化抽象接口。 |
|  | `mysql-impl` | 基于 MyBatis-Plus 的 MySQL 实现。 |
|  | `cache-api` | 定义缓存访问接口。 |
|  | `redis-impl` | 基于 Jedis 的 Redis 实现。 |
|  | `mq-api` | 定义消息通信抽象接口。 |
|  | `sqs-impl` | 基于 AWS SQS 的消息实现。 |

---

## 五、模块依赖关系

| 模块 | 依赖 |
|------|------|
| **bootstrap** | http, consumer, application-impl, domain-impl, mysql-impl, redis-impl, sqs-impl, common |
| **traffic/http** | application-api, common |
| **traffic/consumer** | application-api, common |
| **application-impl** | application-api, domain-api, common |
| **domain-impl** | domain-api, repository-api, cache-api, mq-api, common |
| **mysql-impl** | repository-api, common |
| **redis-impl** | cache-api, common |
| **sqs-impl** | mq-api, common |

---

## 六、技术栈选型

| 技术 | 用途 | 说明 |
|------|------|------|
| **JDK 21** | 开发语言环境 | 使用 LTS 版本 |
| **Spring Boot 3.3.x** | 应用基础框架 | 配置驱动、依赖注入、服务运行 |
| **Spring Cloud 2024.0.x（Leyton）** | 微服务框架 | 提供注册发现、配置中心、调用链、负载均衡 |
| **MyBatis-Plus 3.5.x** | ORM 框架 | 简化 CRUD，增强 MyBatis 功能 |
| **Spring Boot Starter Jedis** | Redis 客户端 | 负责缓存访问逻辑 |
| **Spring Validation / Web** | 参数验证和 HTTP 支持 | 用于接口层 |
| **Lombok** | 代码简化 | 自动生成 getter/setter |
| **Spring Boot Test / JUnit 5** | 测试框架 | 单元测试与集成测试 |
| **OpenFeign（可选）** | 微服务间调用 | 支持服务内的 API 调用 |
| **Micrometer + Prometheus（可选）** | 监控与指标 | 提供可观测性支持 |

---

## 七、配置与依赖管理要求

### 7.1 依赖版本统一管理原则

**核心理念：** 在父 POM 的 `<dependencyManagement>` 节中统一声明所有依赖的版本号，子模块在声明依赖时不指定版本号，从而确保整个项目使用一致的依赖版本。

**最佳实践：**
1. 所有模块共享同一父 `pom.xml` 管理依赖版本
2. 父 POM 在 `<dependencyManagement>` 中统一声明：
   - Spring Boot BOM（Bill of Materials）：通过 `spring-boot-dependencies` 导入
   - Spring Cloud BOM：通过 `spring-cloud-dependencies` 导入
   - 第三方库版本：MyBatis-Plus、Jedis、AWS SDK 等
   - 通用工具库：Lombok、JUnit、Logback 等
3. 子模块在 `<dependencies>` 中声明依赖时，仅指定 `groupId` 和 `artifactId`，不指定 `version`
4. 特殊情况下需要覆盖版本时，在子模块中显式声明版本号


### 7.2 其他配置要求

1. 模块间仅通过 API 层接口依赖，禁止跨层直接访问
2. 启动层 `bootstrap` 管理外部配置文件（如 `application.yml`、`bootstrap.yml`）
3. 使用 Maven 的 `<properties>` 节定义可复用的版本变量  

---

## 八、设计目标与收益

| 目标 | 收益 |
|------|------|
| 清晰的分层结构 | 模块职责单一、易维护、易扩展 |
| 技术与业务解耦 | 基础设施替换不影响领域逻辑 |
| 面向接口编程 | 降低耦合度，提高可测试性 |
| 高扩展性 | 可轻松新增微服务或基础设施实现 |
| 规范依赖关系 | 防止循环依赖与职责混乱 |
| 微服务演进友好 | 可平滑拆分为多个 Spring Cloud 服务 |

---

## 九、其他扩展要求

| 扩展方向 | 目标与说明 |
|-----------|------------|
| **监控与指标体系** | 集成 Prometheus 实现应用指标监控，包括 JVM 指标（内存、GC、线程）、HTTP 请求指标（QPS、延迟、错误率）、数据库连接池指标等。使用 Micrometer 作为指标收集框架，暴露 Prometheus 格式的指标端点（/actuator/prometheus）。 |

---

## 十、日志与追踪体系

### 10.1 设计目标
- 实现跨模块、跨请求的 Trace ID 自动生成与传播；
- 输出结构化 JSON 日志；
- 支持多环境差异化日志配置与级别控制。

### 10.2 架构方案

| 组件 | 功能 | 说明 |
|------|------|------|
| **Spring Cloud Sleuth / Micrometer Tracing** | 分布式链路追踪 | 自动生成 Trace ID / Span ID |
| **Logback + JSON Encoder** | 日志输出框架 | 输出结构化 JSON 格式日志 |
| **MDC（Mapped Diagnostic Context）** | 日志上下文传递 | 记录 traceId、spanId 等信息 |


### 10.3 日志字段规范

| 字段名 | 说明 |
|---------|------|
| timestamp | 日志时间 |
| level | 日志级别 |
| thread | 当前线程 |
| logger | 类名 |
| traceId | 链路追踪ID |
| spanId | 调用链片段ID |
| message | 日志内容 |
| exception | 异常堆栈（可选） |

---

## 十一、异常处理与错误管理

### 11.1 设计目标
- 建立统一的异常处理机制，规范化错误传播和处理流程
- 在通用模块中定义异常体系，供所有模块使用
- 在接口层实现全局异常处理，统一错误响应格式
- 区分业务异常、系统异常、验证异常等不同类型

### 11.2 实现要求
- 通用模块应提供异常基类和常见异常类型
- HTTP 接口层应实现全局异常处理器，捕获并转换异常为标准响应
- 异常信息应包含错误码、错误消息、时间戳等关键信息
- 避免向外部暴露系统内部实现细节

---

## 十二、需求文档编写规范

### 12.1 语言要求
- 需求文档的验收标准必须使用中文表述
- EARS 语法关键字（THE、SHALL、WHEN、WHILE、IF、THEN、WHERE）必须保留英文大写
- 示例：THE System SHALL 创建一个父 POM 文件，其 groupId 为 "com.catface.com"

### 12.2 需要用户额外补充的信息
请在创建需求时，务必与用户澄清以下要补充的信息，确认无误后，再进入下一步
* 系统名称
* 基础包名

## ✅ 总结

该架构方案具备以下特征：
- 完全遵循 DDD 分层原则；
- 模块职责清晰、依赖单向；
- 使用最新 Spring Cloud 技术栈；
- 集成 MyBatis-Plus 与 Jedis；
- 支持链路追踪与结构化日志输出；
- 提供多环境配置与未来微服务演进支持。
