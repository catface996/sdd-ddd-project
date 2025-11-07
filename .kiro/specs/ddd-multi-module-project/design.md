# 设计文档

## 概述

本设计文档描述了基于 Spring Cloud 和 DDD 架构的多模块 Maven 工程 order-service 的详细设计方案。该系统采用严格的分层架构，通过模块化设计实现技术与业务的解耦，支持微服务演进。

### 设计目标

- 建立清晰的 DDD 分层结构（接口层、应用层、领域层、基础设施层）
- 实现单向依赖关系，防止循环依赖
- 通过接口抽象实现技术组件的可替换性
- 统一管理依赖版本，确保项目一致性
- 支持多环境部署配置

### 技术栈

- JDK 21
- Spring Boot 3.3.5
- Spring Cloud 2024.0.0 (Leyton)
- MyBatis-Plus 3.5.9
- Jedis 5.2.0
- AWS SDK for SQS 1.12.x
- Maven 3.8+

## 架构设计

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Bootstrap Layer                       │
│                    (启动与配置管理)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Traffic Layer                         │
│                         (接口层)                             │
│  ┌──────────────────┐              ┌──────────────────┐    │
│  │   HTTP Module    │              │ Consumer Module  │    │
│  │  (REST API)      │              │  (MQ Consumer)   │    │
│  └──────────────────┘              └──────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Application Layer                       │
│                        (应用层)                              │
│  ┌──────────────────┐              ┌──────────────────┐    │
│  │ Application-API  │◄─────────────│Application-Impl  │    │
│  │   (接口定义)     │              │   (业务编排)     │    │
│  └──────────────────┘              └──────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Domain Layer                          │
│                         (领域层)                             │
│  ┌──────────────────┐              ┌──────────────────┐    │
│  │   Domain-API     │◄─────────────│  Domain-Impl     │    │
│  │  (领域模型定义)  │              │  (领域逻辑)      │    │
│  └──────────────────┘              └──────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                       │
│                      (基础设施层)                            │
│  ┌────────────┐    ┌────────────┐    ┌────────────┐        │
│  │ Repository │    │   Cache    │    │     MQ     │        │
│  ├────────────┤    ├────────────┤    ├────────────┤        │
│  │ Repo-API   │    │ Cache-API  │    │  MQ-API    │        │
│  │ MySQL-Impl │    │ Redis-Impl │    │  SQS-Impl  │        │
│  └────────────┘    └────────────┘    └────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │  Common Module   │
                    │   (通用组件)     │
                    └──────────────────┘
```

### 分层职责

#### 1. Bootstrap Layer (启动层)
- 作为应用程序的唯一入口点
- 负责 Spring 容器的初始化和配置加载
- 聚合所有实现模块的依赖
- 管理多环境配置文件
- 包含主启动类 `OrderServiceApplication`（位于包 `com.catface.com.orderservice`）

**依赖说明：**
- http：HTTP 接口模块
- consumer：消息消费模块
- application-impl：应用层实现
- domain-impl：领域层实现
- mysql-impl：MySQL 仓储实现
- redis-impl：Redis 缓存实现
- sqs-impl：SQS 消息队列实现
- common：通用组件
- spring-boot-starter：Spring Boot 核心启动器

**主启动类示例：**
```java
package com.catface.com.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

#### 2. Traffic Layer (接口层)
- HTTP Module: 处理 RESTful API 请求，进行参数验证和数据转换
- Consumer Module: 消费消息队列事件，触发异步业务处理
- 仅依赖 Application-API 和 Common，不直接访问领域层或基础设施层

#### 3. Application Layer (应用层)
- Application-API: 定义应用服务接口、DTO、Command、Query 对象
- Application-Impl: 实现业务用例编排，协调领域服务完成业务流程
- 不包含业务规则，仅负责流程控制和事务管理

#### 4. Domain Layer (领域层)
- Domain-API: 定义领域模型、聚合根、实体、值对象、仓储接口、领域事件
- Domain-Impl: 实现领域服务，封装核心业务规则和业务逻辑
- 依赖基础设施接口（Repository-API、Cache-API、MQ-API），不依赖具体实现

#### 5. Infrastructure Layer (基础设施层)
- Repository: 数据持久化抽象及 MySQL 实现（MyBatis-Plus）
- Cache: 缓存抽象及 Redis 实现（Jedis）
- MQ: 消息队列抽象及 AWS SQS 实现
- 各实现模块相互独立，可独立替换

#### 6. Common Module (通用模块)
- 提供工具类、通用异常、常量定义、通用 DTO
- 被所有其他模块依赖

## 组件和接口设计

### 模块依赖关系矩阵

| 模块 | 依赖模块 |
|------|----------|
| common | 无 |
| domain-api | common |
| repository-api | common |
| cache-api | common |
| mq-api | common |
| domain-impl | domain-api, repository-api, cache-api, mq-api, common |
| application-api | common |
| application-impl | application-api, domain-api, common |
| mysql-impl | repository-api, common |
| redis-impl | cache-api, common |
| sqs-impl | mq-api, common |
| http | application-api, common |
| consumer | application-api, common |
| bootstrap | http, consumer, application-impl, domain-impl, mysql-impl, redis-impl, sqs-impl, common |

### Maven 模块结构设计

```
order-service/
├── pom.xml                                    (父 POM，packaging=pom)
├── common/
│   └── pom.xml                                (通用模块，packaging=jar)
├── bootstrap/
│   └── pom.xml                                (启动模块，packaging=jar)
├── traffic/
│   ├── pom.xml                                (接口层父模块，packaging=pom)
│   ├── http/
│   │   └── pom.xml                            (HTTP 模块，packaging=jar)
│   └── consumer/
│       └── pom.xml                            (消费者模块，packaging=jar)
├── application/
│   ├── pom.xml                                (应用层父模块，packaging=pom)
│   ├── application-api/
│   │   └── pom.xml                            (应用接口，packaging=jar)
│   └── application-impl/
│       └── pom.xml                            (应用实现，packaging=jar)
├── domain/
│   ├── pom.xml                                (领域层父模块，packaging=pom)
│   ├── domain-api/
│   │   └── pom.xml                            (领域接口，packaging=jar)
│   └── domain-impl/
│       └── pom.xml                            (领域实现，packaging=jar)
└── infrastructure/
    ├── pom.xml                                (基础设施父模块，packaging=pom)
    ├── repository/
    │   ├── pom.xml                            (仓储父模块，packaging=pom)
    │   ├── repository-api/
    │   │   └── pom.xml                        (仓储接口，packaging=jar)
    │   └── mysql-impl/
    │       └── pom.xml                        (MySQL 实现，packaging=jar)
    ├── cache/
    │   ├── pom.xml                            (缓存父模块，packaging=pom)
    │   ├── cache-api/
    │   │   └── pom.xml                        (缓存接口，packaging=jar)
    │   └── redis-impl/
    │       └── pom.xml                        (Redis 实现，packaging=jar)
    └── mq/
        ├── pom.xml                            (消息队列父模块，packaging=pom)
        ├── mq-api/
        │   └── pom.xml                        (MQ 接口，packaging=jar)
        └── sqs-impl/
            └── pom.xml                        (SQS 实现，packaging=jar)
```

#### Infrastructure 父模块配置

infrastructure/pom.xml 作为基础设施层的父模块，仅用于组织子模块，不包含任何依赖：

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.catface.com</groupId>
        <artifactId>order-service</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>infrastructure</artifactId>
    <packaging>pom</packaging>
    
    <modules>
        <module>repository</module>
        <module>cache</module>
        <module>mq</module>
    </modules>
</project>
```

### 父 POM 配置设计

#### 基本信息和模块声明

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.catface.com</groupId>
    <artifactId>order-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>common</module>
        <module>bootstrap</module>
        <module>traffic</module>
        <module>application</module>
        <module>domain</module>
        <module>infrastructure</module>
    </modules>
    
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

#### 依赖管理

父 POM 通过 `<dependencyManagement>` 统一管理所有依赖版本：

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot BOM -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.3.5</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- Spring Cloud BOM -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2024.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.9</version>
        </dependency>
        
        <!-- Jedis -->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>5.2.0</version>
        </dependency>
        
        <!-- AWS SDK SQS -->
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-java-sdk-sqs</artifactId>
            <version>1.12.772</version>
        </dependency>
        
        <!-- MySQL Connector -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
        </dependency>
        
        <!-- JUnit -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.3</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 数据模型设计

### 包结构设计

每个模块遵循统一的包结构规范，基础包名为 `com.catface.com.orderservice`：

#### Common Module
```
com.catface.com.orderservice.common
├── constants/          # 常量定义
├── dto/               # 通用数据传输对象
├── exceptions/        # 通用异常类
└── utils/             # 工具类
```

#### Domain-API Module
```
com.catface.com.orderservice.domain
├── model/             # 领域模型
├── aggregate/         # 聚合根
├── entity/            # 实体
├── valueobject/       # 值对象
└── repository/        # 仓储接口
```

**依赖说明：**
- common：通用组件（仅此一项依赖，不依赖任何基础设施）

#### Application-API Module
```
com.catface.com.orderservice.application
├── service/           # 应用服务接口
├── dto/              # 应用层 DTO
├── command/          # 命令对象
└── query/            # 查询对象
```

**依赖说明：**
- common：通用组件（仅此一项依赖）

#### Traffic/HTTP Module
```
com.catface.com.orderservice.traffic.http
├── controller/        # REST 控制器
└── converter/         # 数据转换器
```

**依赖说明：**
- spring-boot-starter-web：提供 Web 功能
- spring-boot-starter-validation：提供参数验证功能
- application-api：应用服务接口
- common：通用组件

#### Repository-API Module
```
com.catface.com.orderservice.infrastructure.repository
└── (仓储接口定义)
```

#### MySQL-Impl Module
```
com.catface.com.orderservice.infrastructure.repository.mysql
├── mapper/            # MyBatis Mapper 接口
├── entity/            # 数据库实体（DO）
└── impl/             # 仓储接口实现
```

**依赖说明：**
- mybatis-plus-boot-starter：提供 MyBatis-Plus 功能
- mysql-connector-j：MySQL JDBC 驱动（scope: runtime）
- repository-api：仓储接口定义
- common：通用组件

#### Cache-API Module
```
com.catface.com.orderservice.infrastructure.cache
└── (缓存接口定义)
```

#### Redis-Impl Module
```
com.catface.com.orderservice.infrastructure.cache.redis
├── config/            # Redis 配置
└── impl/             # 缓存接口实现
```

**依赖说明：**
- jedis：Redis 客户端
- cache-api：缓存接口定义
- common：通用组件

#### MQ-API Module
```
com.catface.com.orderservice.infrastructure.mq
└── (消息队列接口定义)
```

#### SQS-Impl Module
```
com.catface.com.orderservice.infrastructure.mq.sqs
├── config/            # SQS 配置
└── impl/             # MQ 接口实现
```

**依赖说明：**
- aws-java-sdk-sqs：AWS SQS SDK（版本 1.12.x）
- mq-api：消息队列接口定义
- common：通用组件

### 接口设计示例

#### 仓储接口（Repository-API）
```java
package com.catface.com.orderservice.infrastructure.repository;

public interface OrderRepository {
    void save(Order order);
    Order findById(String orderId);
    List<Order> findByUserId(String userId);
}
```

#### 缓存接口（Cache-API）
```java
package com.catface.com.orderservice.infrastructure.cache;

public interface CacheService {
    void set(String key, Object value, long ttl);
    <T> T get(String key, Class<T> clazz);
    void delete(String key);
}
```

#### 消息队列接口（MQ-API）
```java
package com.catface.com.orderservice.infrastructure.mq;

public interface MessagePublisher {
    void publish(String topic, Object message);
}
```

## 错误处理设计

### 异常层次结构

```
com.catface.com.orderservice.common.exceptions
├── BaseException                    # 基础异常类
├── BusinessException                # 业务异常
├── SystemException                  # 系统异常
├── ValidationException              # 验证异常
└── InfrastructureException          # 基础设施异常
```

### 全局异常处理

在 HTTP 模块中实现全局异常处理器：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        // 处理业务异常
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        // 处理验证异常
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // 处理未知异常
    }
}
```

## 配置管理设计

### 多环境配置

Bootstrap 模块包含以下配置文件：

1. **application.yml** - 默认配置
```yaml
spring:
  application:
    name: order-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

2. **application-dev.yml** - 开发环境
```yaml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/order_dev
    username: dev_user
    password: dev_password
  data:
    redis:
      host: localhost
      port: 6379
aws:
  sqs:
    endpoint: http://localhost:4566
    region: us-east-1
```

3. **application-staging.yml** - 预发布环境
```yaml
spring:
  config:
    activate:
      on-profile: staging
  datasource:
    url: jdbc:mysql://staging-db:3306/order_staging
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: staging-redis
      port: 6379
aws:
  sqs:
    endpoint: https://sqs.us-east-1.amazonaws.com
    region: us-east-1
```

4. **application-prod.yml** - 生产环境
```yaml
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://prod-db:3306/order_prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: prod-redis
      port: 6379
      password: ${REDIS_PASSWORD}
aws:
  sqs:
    endpoint: https://sqs.us-east-1.amazonaws.com
    region: us-east-1
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
```

### 配置加载优先级

1. 命令行参数
2. 环境变量
3. application-{profile}.yml
4. application.yml

## 部署设计

### 构建产物

执行 `mvn clean package` 后，在 `bootstrap/target/` 目录生成可执行 JAR：
- `order-service-1.0.0-SNAPSHOT.jar`

### 启动命令

```bash
# 开发环境
java -jar bootstrap/target/order-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev

# 预发布环境
java -jar bootstrap/target/order-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=staging

# 生产环境
java -jar bootstrap/target/order-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## 日志与追踪体系

### 设计目标
- 实现跨模块、跨请求的 Trace ID 自动生成与传播
- 输出结构化 JSON 日志
- 支持多环境差异化日志配置与级别控制

### 架构方案

| 组件 | 功能 | 说明 |
|------|------|------|
| **Spring Cloud Sleuth / Micrometer Tracing** | 分布式链路追踪 | 自动生成 Trace ID / Span ID |
| **Logback + JSON Encoder** | 日志输出框架 | 输出结构化 JSON 格式日志 |
| **MDC（Mapped Diagnostic Context）** | 日志上下文传递 | 记录 traceId、spanId 等信息 |
| **日志聚合系统（ELK / Loki）** | 日志集中化 | 实现跨模块日志检索与分析 |

### 日志字段规范

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

### 实现说明

**日志配置：**
- 使用 Logback 作为日志框架
- 配置 JSON 格式输出（使用 Logstash Encoder）
- 通过 MDC 自动注入 traceId 和 spanId

**链路追踪：**
- 集成 Spring Cloud Sleuth 或 Micrometer Tracing
- 自动为每个请求生成唯一的 Trace ID
- 在服务调用链中传递 Trace ID 和 Span ID
- 支持跨模块、跨请求的链路追踪

**多环境配置：**
- 开发环境：输出到控制台，日志级别 DEBUG
- 预发布环境：输出到文件，日志级别 INFO
- 生产环境：输出到文件和日志聚合系统，日志级别 WARN

## 监控与指标体系

### 设计目标
- 集成 Prometheus 实现应用指标监控
- 暴露标准的 Prometheus 指标端点
- 收集 JVM、HTTP 请求、数据库连接池等关键指标

### 架构方案

**核心组件：**
- **Micrometer**：作为指标收集框架，提供统一的指标 API
- **Prometheus Registry**：将指标转换为 Prometheus 格式
- **Spring Boot Actuator**：暴露指标端点

**指标类型：**

1. **JVM 指标**
   - 内存使用情况（堆内存、非堆内存）
   - GC 统计信息（GC 次数、GC 耗时）
   - 线程状态（活跃线程数、守护线程数）

2. **HTTP 请求指标**
   - 请求总数和 QPS
   - 请求延迟分布（P50、P95、P99）
   - HTTP 状态码分布
   - 错误率统计

3. **数据库连接池指标**
   - 活跃连接数
   - 空闲连接数
   - 等待连接数
   - 连接超时次数

4. **自定义业务指标**
   - 支持通过 Micrometer API 添加自定义指标
   - 支持 Counter、Gauge、Timer、Summary 等指标类型

### 配置说明

**依赖配置：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**应用配置：**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

**指标端点：**
- 访问路径：`/actuator/prometheus`
- 返回格式：Prometheus 文本格式
- 访问方式：HTTP GET 请求

### 集成 Prometheus Server

**Prometheus 配置示例：**
```yaml
scrape_configs:
  - job_name: 'order-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

**监控流程：**
1. 应用通过 Micrometer 收集指标
2. Prometheus Registry 将指标转换为 Prometheus 格式
3. Actuator 暴露 `/actuator/prometheus` 端点
4. Prometheus Server 定期拉取指标数据
5. 通过 Grafana 可视化展示指标

## 总结

本设计方案完整实现了基于 DDD 的多模块 Maven 工程架构，具备以下特点：

1. **清晰的分层结构**：严格遵循 DDD 分层原则，职责明确
2. **单向依赖关系**：外层依赖内层，防止循环依赖
3. **技术可替换性**：通过接口抽象，基础设施实现可独立替换
4. **统一依赖管理**：父 POM 统一管理版本，确保一致性
5. **多环境支持**：支持 dev/staging/prod 多环境配置
6. **统一异常处理**：建立完整的异常体系和全局异常处理机制
7. **规范的包结构**：每个模块都有清晰的包组织结构
8. **完整的配置管理**：支持多环境配置文件和配置加载优先级
9. **日志与追踪体系**：集成链路追踪和结构化日志输出
10. **监控与指标体系**：集成 Prometheus 实现应用指标监控

该设计为后续的实现提供了清晰的指导，确保项目能够按照最佳实践进行开发。
