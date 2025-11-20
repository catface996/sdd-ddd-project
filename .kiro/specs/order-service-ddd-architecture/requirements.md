# 需求文档

## 引言

### 项目背景
本项目旨在构建一个基于 Spring Cloud 最新稳定版本的多模块 Maven 工程，采用领域驱动设计（DDD）思想，实现订单服务（Order Service）。该架构将提供清晰的层次划分与模块边界，实现技术与业务逻辑的解耦，支持可扩展、可演进、可替换的系统结构，并为未来的微服务化演进提供良好支持。

### 项目目标
- 建立符合 DDD 分层原则的多模块 Maven 工程结构
- 实现清晰的模块职责划分和单向依赖关系
- 集成 Spring Cloud 生态的链路追踪、监控等能力
- 提供统一的异常处理和日志管理机制
- 支持多环境配置和部署

### 项目范围
本需求文档涵盖订单服务 DDD 架构工程的基础设施搭建，包括：
- Maven 多模块工程结构创建
- 各层模块的创建和配置
- 技术栈集成（Spring Boot、Spring Cloud、MyBatis-Plus、Redis、SQS 等）
- 日志追踪体系配置
- 异常处理机制实现
- 多环境配置支持
- 监控指标集成

## 术语表

- **System**：订单服务系统（Order Service System）
- **DDD**：领域驱动设计（Domain-Driven Design）
- **Maven 模块**：Maven 多模块工程中的独立模块
- **父 POM**：Maven 父项目配置文件，用于统一管理依赖版本
- **BOM**：Bill of Materials，依赖清单，用于统一管理一组相关依赖的版本
- **接口层**：处理外部请求的层，包括 HTTP API 和消息队列消费者
- **应用层**：业务用例编排层，协调领域层完成业务逻辑
- **领域层**：核心业务逻辑层，包含领域模型和领域服务
- **基础设施层**：技术实现层，包括数据库、缓存、消息队列等
- **Trace ID**：分布式链路追踪标识符
- **Span ID**：调用链片段标识符
- **Profile**：Spring 环境配置标识（如 local、dev、test、staging、prod）
- **Actuator**：Spring Boot 监控端点
- **Prometheus**：开源监控系统和时序数据库

---

## 需求

### 需求 1：创建 Maven 父工程

**用户故事：** 作为系统架构师，我希望创建一个 Maven 父工程，以便统一管理所有子模块的依赖版本和构建配置。

#### 验收标准

1. THE System SHALL 创建一个 Maven 父 POM 文件，其 groupId 为 "com.catface.orderservice"，artifactId 为 "order-service-parent"
2. THE System SHALL 在父 POM 中通过 `<dependencyManagement>` 统一声明 Spring Boot BOM（版本 3.4.1）、Spring Cloud BOM（版本 2025.0.0）
3. THE System SHALL 在父 POM 中通过 `<properties>` 定义第三方库版本变量（MyBatis-Plus 3.5.7、Druid 1.2.20、AWS SDK for SQS 2.20.0、Micrometer Tracing 1.3.5、Logstash Logback Encoder 7.4）
4. THE System SHALL 配置 Maven 编译插件使用 JDK 21
5. THE System SHALL 配置父 POM 的打包类型为 pom
6. THE System SHALL 在父 POM 的 `<modules>` 中声明 6 个顶层模块：common、interface、application、domain、infrastructure、bootstrap

---

### 需求 2：创建通用模块（Common）

**用户故事：** 作为开发人员，我希望有一个通用模块来存放项目中共享的工具类、异常定义、常量和通用 DTO，以便在各个模块中复用。

#### 验收标准

1. THE System SHALL 创建 common 模块，其 artifactId 为 "order-service-common"
2. THE System SHALL 在 common 模块中定义异常体系，包括基础异常类（BaseException）、业务异常类（BusinessException）、系统异常类（SystemException）
3. THE System SHALL 在基础异常类中包含错误码（errorCode）和错误消息（errorMessage）字段
4. THE System SHALL 在 common 模块中定义统一响应类（Result），包含 code、message、data 字段
5. THE System SHALL 在 common 模块中创建包结构：exception、dto、constant、util
6. THE System SHALL 配置 common 模块的打包类型为 jar

---

### 需求 3：创建领域层模块

**用户故事：** 作为领域专家，我希望有独立的领域层模块来定义和实现核心业务逻辑，以便将业务规则与技术实现分离。

#### 验收标准

1. THE System SHALL 创建 domain 聚合模块，其打包类型为 pom，用于聚合领域层子模块
2. THE System SHALL 在 domain 聚合模块中声明 domain-api 和 domain-impl 子模块
3. THE System SHALL 创建 domain-api 模块，用于定义领域模型、聚合根、实体、值对象和仓储接口
4. THE System SHALL 创建 domain-impl 模块，用于实现领域服务逻辑
5. THE System SHALL 确保 domain-api 模块仅依赖 common 模块
6. THE System SHALL 确保 domain-impl 模块依赖 domain-api、repository-api、cache-api、mq-api 和 common 模块
7. THE System SHALL 配置 domain-api 和 domain-impl 模块的打包类型为 jar

---

### 需求 4：创建应用层模块

**用户故事：** 作为业务分析师，我希望有应用层模块来编排业务用例，以便协调领域层完成复杂的业务流程。

#### 验收标准

1. THE System SHALL 创建 application 聚合模块，其打包类型为 pom，用于聚合应用层子模块
2. THE System SHALL 在 application 聚合模块中声明 application-api 和 application-impl 子模块
3. THE System SHALL 创建 application-api 模块，用于定义应用服务接口、DTO、Command 和 Query 对象
4. THE System SHALL 创建 application-impl 模块，用于实现业务用例编排逻辑
5. THE System SHALL 确保 application-api 模块仅依赖 common 模块
6. THE System SHALL 确保 application-impl 模块依赖 application-api、domain-api 和 common 模块
7. THE System SHALL 配置 application-api 和 application-impl 模块的打包类型为 jar

---

### 需求 5：创建基础设施层模块

**用户故事：** 作为系统架构师，我希望有基础设施层模块来实现数据持久化、缓存和消息队列等技术能力，以便支持上层业务逻辑。

#### 验收标准

1. THE System SHALL 创建 infrastructure 聚合模块，其打包类型为 pom，用于聚合基础设施层子模块
2. THE System SHALL 在 infrastructure 聚合模块中声明 repository、cache、mq 子模块
3. THE System SHALL 创建 repository 聚合模块，其打包类型为 pom，用于聚合仓储层子模块
4. THE System SHALL 在 repository 聚合模块中声明 repository-api 和 mysql-impl 子模块
5. THE System SHALL 创建 repository-api 模块，用于定义数据持久化抽象接口
6. THE System SHALL 创建 mysql-impl 模块，基于 MyBatis-Plus 实现 MySQL 数据访问
7. THE System SHALL 创建 cache 聚合模块，其打包类型为 pom，用于聚合缓存层子模块
8. THE System SHALL 在 cache 聚合模块中声明 cache-api 和 redis-impl 子模块
9. THE System SHALL 创建 cache-api 模块，用于定义缓存访问接口
10. THE System SHALL 创建 redis-impl 模块，基于 Lettuce（Spring Data Redis）实现 Redis 缓存
11. THE System SHALL 创建 mq 聚合模块，其打包类型为 pom，用于聚合消息队列层子模块
12. THE System SHALL 在 mq 聚合模块中声明 mq-api 和 sqs-impl 子模块
13. THE System SHALL 创建 mq-api 模块，用于定义消息通信接口
14. THE System SHALL 创建 sqs-impl 模块，基于 AWS SDK for SQS 实现消息队列
15. THE System SHALL 确保 mysql-impl 模块依赖 repository-api 和 common 模块，并包含 MyBatis-Plus、Druid 和 MySQL 驱动依赖
16. THE System SHALL 确保 redis-impl 模块依赖 cache-api 和 common 模块，并包含 Spring Data Redis 依赖
17. THE System SHALL 确保 sqs-impl 模块依赖 mq-api 和 common 模块，并包含 AWS SDK for SQS 依赖
18. THE System SHALL 配置所有代码模块（*-api 和 *-impl）的打包类型为 jar

---

### 需求 6：创建接口层模块

**用户故事：** 作为前端开发人员，我希望有接口层模块来处理 HTTP 请求和消息队列消费，以便与系统进行交互。

#### 验收标准

1. THE System SHALL 创建 interface 聚合模块，其打包类型为 pom，用于聚合接口层子模块
2. THE System SHALL 在 interface 聚合模块中声明 interface-http 和 interface-consumer 子模块
3. THE System SHALL 创建 interface-http 模块，用于处理外部 HTTP 请求
4. THE System SHALL 创建 interface-consumer 模块，用于接收和处理消息队列事件
5. THE System SHALL 确保 interface-http 模块依赖 application-api 和 common 模块，并包含 Spring Web 依赖
6. THE System SHALL 确保 interface-consumer 模块依赖 application-api 和 common 模块
7. THE System SHALL 在 interface-http 模块中实现全局异常处理器，使用 @RestControllerAdvice 注解
8. THE System SHALL 在 interface-consumer 模块中实现全局异常处理器，使用 @ControllerAdvice 注解
9. THE System SHALL 配置 interface-http 和 interface-consumer 模块的打包类型为 jar

---

### 需求 7：创建启动模块（Bootstrap）

**用户故事：** 作为运维人员，我希望有一个启动模块来启动整个应用，以便部署和运行系统。

#### 验收标准

1. THE System SHALL 创建 bootstrap 模块，作为应用的启动入口
2. THE System SHALL 确保 bootstrap 模块依赖 interface-http、interface-consumer、application-impl、domain-impl、mysql-impl、redis-impl、sqs-impl 和 common 模块
3. THE System SHALL 在 bootstrap 模块中创建 Spring Boot 主启动类，使用 @SpringBootApplication 注解
4. THE System SHALL 配置 bootstrap 模块使用 Spring Boot Maven 插件，生成可执行 JAR 文件
5. THE System SHALL 配置 bootstrap 模块的打包类型为 jar

---

### 需求 8：配置日志与链路追踪体系

**用户故事：** 作为运维人员，我希望系统能够输出结构化日志并支持分布式链路追踪，以便快速定位和排查问题。

#### 验收标准

1. THE System SHALL 在 bootstrap 模块中集成 Micrometer Tracing 依赖，实现 Trace ID 和 Span ID 的自动生成与传播
2. THE System SHALL 在 bootstrap 模块中集成 Logstash Logback Encoder 依赖，支持 JSON 格式日志输出
3. THE System SHALL 创建 logback-spring.xml 配置文件，支持多环境日志配置
4. WHEN 应用在 local 环境运行 THEN THE System SHALL 输出彩色格式日志到控制台，项目包（com.catface.orderservice）使用 DEBUG 级别，框架包使用 WARN 级别
5. WHEN 应用在 dev、test 环境运行 THEN THE System SHALL 输出 JSON 格式日志到文件，项目包使用 DEBUG 级别，框架包使用 WARN 级别
6. WHEN 应用在 staging 环境运行 THEN THE System SHALL 输出 JSON 格式日志到文件，项目包使用 INFO 级别，框架包使用 WARN 级别
7. WHEN 应用在 prod 环境运行 THEN THE System SHALL 输出 JSON 格式日志到文件，项目包使用 INFO 级别，框架包使用 WARN 级别
8. THE System SHALL 确保所有环境的日志都包含 timestamp、level、thread、logger、traceId、spanId、message 字段
9. THE System SHALL 配置日志文件按日期滚动，单个文件超过 100MB 时自动分割
10. THE System SHALL 配置非生产环境保留最近 30 天的日志，生产环境保留最近 90 天的日志
11. THE System SHALL 配置 ERROR 级别日志单独输出到 error.log 文件

---

### 需求 9：实现异常处理机制

**用户故事：** 作为开发人员，我希望系统有统一的异常处理机制，以便规范化错误传播和处理流程。

#### 验收标准

1. THE System SHALL 在 interface-http 模块中实现全局异常处理器，捕获所有异常并转换为统一的 Result 响应
2. THE System SHALL 在 interface-consumer 模块中实现全局异常处理器，捕获所有异常并记录日志
3. WHEN 接口层捕获 BusinessException THEN THE System SHALL 返回业务错误响应，HTTP 状态码为 200，Result.code 为业务错误码
4. WHEN 接口层捕获 SystemException THEN THE System SHALL 返回系统错误响应，HTTP 状态码为 500，Result.code 为系统错误码
5. WHEN 接口层捕获未知异常 THEN THE System SHALL 返回通用错误响应，HTTP 状态码为 500，不暴露系统内部实现细节
6. THE System SHALL 确保异常响应包含错误码、错误消息和时间戳

---

### 需求 10：配置多环境支持

**用户故事：** 作为运维人员，我希望系统支持多环境配置，以便在不同环境中使用不同的配置参数。

#### 验收标准

1. THE System SHALL 在 bootstrap 模块的 resources 目录下创建 application.yml 作为通用配置文件
2. THE System SHALL 创建 application-local.yml、application-dev.yml、application-test.yml、application-staging.yml、application-prod.yml 作为环境特定配置文件
3. THE System SHALL 在 application.yml 中配置默认激活的 profile 为 local
4. THE System SHALL 支持通过命令行参数 --spring.profiles.active 激活指定环境
5. THE System SHALL 支持通过环境变量 SPRING_PROFILES_ACTIVE 激活指定环境
6. THE System SHALL 确保配置加载优先级为：命令行参数 > 环境变量 > application-{profile}.yml > application.yml

---

### 需求 11：集成 Prometheus 监控

**用户故事：** 作为运维人员，我希望系统能够暴露 Prometheus 格式的监控指标，以便监控应用的运行状态和性能。

#### 验收标准

1. THE System SHALL 在 bootstrap 模块中集成 Spring Boot Actuator 依赖
2. THE System SHALL 在 bootstrap 模块中集成 Micrometer Registry Prometheus 依赖
3. THE System SHALL 配置 Actuator 暴露 prometheus 端点
4. THE System SHALL 配置 Actuator 端点路径为 /actuator
5. WHEN 应用启动后访问 /actuator/prometheus THEN THE System SHALL 返回 Prometheus 格式的指标数据
6. THE System SHALL 暴露 JVM 指标（内存、GC、线程）
7. THE System SHALL 暴露 HTTP 请求指标（QPS、延迟、错误率）
8. THE System SHALL 暴露数据库连接池指标（活跃连接数、空闲连接数）

---

### 需求 12：模块命名规范

**用户故事：** 作为项目管理者，我希望所有 Maven 模块使用统一的命名规范，以便构建日志输出清晰易读。

#### 验收标准

1. THE System SHALL 为所有 Maven 模块的 `<name>` 标签使用首字母大写的英文单词，单词之间用空格分隔
2. THE System SHALL 为 common 模块设置 name 为 "Order Service Common"
3. THE System SHALL 为 domain-api 模块设置 name 为 "Order Service Domain API"
4. THE System SHALL 为 domain-impl 模块设置 name 为 "Order Service Domain Implementation"
5. THE System SHALL 为 application-api 模块设置 name 为 "Order Service Application API"
6. THE System SHALL 为 application-impl 模块设置 name 为 "Order Service Application Implementation"
7. THE System SHALL 为 repository-api 模块设置 name 为 "Order Service Repository API"
8. THE System SHALL 为 mysql-impl 模块设置 name 为 "Order Service MySQL Implementation"
9. THE System SHALL 为 cache-api 模块设置 name 为 "Order Service Cache API"
10. THE System SHALL 为 redis-impl 模块设置 name 为 "Order Service Redis Implementation"
11. THE System SHALL 为 mq-api 模块设置 name 为 "Order Service MQ API"
12. THE System SHALL 为 sqs-impl 模块设置 name 为 "Order Service SQS Implementation"
13. THE System SHALL 为 interface-http 模块设置 name 为 "Order Service HTTP Interface"
14. THE System SHALL 为 interface-consumer 模块设置 name 为 "Order Service Consumer Interface"
15. THE System SHALL 为 bootstrap 模块设置 name 为 "Order Service Bootstrap"

---

### 需求 13：依赖版本管理

**用户故事：** 作为架构师，我希望所有子模块的依赖版本由父 POM 统一管理，以便确保整个项目使用一致的依赖版本。

#### 验收标准

1. THE System SHALL 在父 POM 的 `<dependencyManagement>` 中声明所有依赖的版本
2. THE System SHALL 确保子模块在声明依赖时不指定 version 标签
3. THE System SHALL 通过 Spring Boot BOM 管理 Spring Boot 相关依赖的版本
4. THE System SHALL 通过 Spring Cloud BOM 管理 Spring Cloud 相关依赖的版本
5. THE System SHALL 在父 POM 的 `<properties>` 中定义第三方库的版本变量
6. WHEN 子模块需要覆盖版本时 THEN THE System SHALL 允许在子模块中显式声明 version 标签

---
