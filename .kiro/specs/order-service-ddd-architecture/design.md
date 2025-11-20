# 设计文档

## 概述

### 项目背景

本项目旨在构建一个基于 Spring Cloud 最新稳定版本的订单服务（Order Service），采用领域驱动设计（DDD）思想和多模块 Maven 工程结构。该架构将实现清晰的层次划分与模块边界，确保技术与业务逻辑的解耦，支持可扩展、可演进、可替换的系统结构。

### 设计目标

- 建立符合 DDD 分层原则的多模块 Maven 工程结构
- 实现清晰的模块职责划分和单向依赖关系
- 集成 Spring Cloud 生态的链路追踪、监控等能力
- 提供统一的异常处理和日志管理机制
- 支持多环境配置和部署

### 设计范围

本设计文档涵盖订单服务 DDD 架构工程的基础设施搭建，包括：
- Maven 多模块工程结构设计
- 各层模块的职责定义和依赖关系
- 技术栈选型和集成方案
- 日志追踪体系设计
- 异常处理机制设计
- 多环境配置方案
- 监控指标集成方案

### 关键约束

- JDK 版本：21（LTS 版本）
- Spring Boot 版本：3.4.1
- Spring Cloud 版本：2025.0.0
- 构建工具：Maven
- 数据库：MySQL
- 缓存：Redis
- 消息队列：AWS SQS

---

## 架构设计

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Bootstrap Layer                          │
│                    (Application Entry Point)                     │
└───────────────────────────────┬─────────────────────────────────┘
                                │
        ┌───────────────────────┴───────────────────────┐
        │                                               │
┌───────▼──────────┐                          ┌────────▼─────────┐
│ Interface Layer  │                          │ Interface Layer  │
│   (HTTP API)     │                          │   (Consumer)     │
└───────┬──────────┘                          └────────┬─────────┘
        │                                               │
        └───────────────────────┬───────────────────────┘
                                │
                        ┌───────▼──────────┐
                        │ Application Layer │
                        │  (Use Case)       │
                        └───────┬──────────┘
                                │
                        ┌───────▼──────────┐
                        │   Domain Layer    │
                        │ (Business Logic)  │
                        └───────┬──────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
┌───────▼──────────┐   ┌───────▼──────────┐   ┌───────▼──────────┐
│ Infrastructure   │   │ Infrastructure   │   │ Infrastructure   │
│   (Repository)   │   │     (Cache)      │   │      (MQ)        │
└──────────────────┘   └──────────────────┘   └──────────────────┘
```


### 模块划分

#### 1. 父工程（order-service-parent）
- **职责**：统一管理所有子模块的依赖版本和构建配置
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：common、interface、application、domain、infrastructure、bootstrap

#### 2. 通用模块（common）
- **职责**：提供项目中共享的工具类、异常定义、常量和通用 DTO
- **打包类型**：jar
- **依赖**：无
- **包结构**：
  - `exception`：异常体系
  - `dto`：通用数据传输对象
  - `constant`：常量定义
  - `util`：工具类

#### 3. 领域层（Domain Layer）

**domain 聚合模块**
- **职责**：聚合领域层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：domain-api、domain-impl

**domain-api 模块**
- **职责**：定义领域模型、聚合根、实体、值对象和仓储接口
- **打包类型**：jar
- **依赖**：common
- **包结构**：
  - `model`：领域模型
  - `repository`：仓储接口定义
  - `service`：领域服务接口

**domain-impl 模块**
- **职责**：实现领域服务逻辑
- **打包类型**：jar
- **依赖**：domain-api, repository-api, cache-api, mq-api, common
- **包结构**：
  - `service.impl`：领域服务实现

#### 4. 应用层（Application Layer）

**application 聚合模块**
- **职责**：聚合应用层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：application-api、application-impl

**application-api 模块**
- **职责**：定义应用服务接口、DTO、Command 和 Query 对象
- **打包类型**：jar
- **依赖**：common
- **包结构**：
  - `service`：应用服务接口
  - `dto`：应用层数据传输对象
  - `command`：命令对象
  - `query`：查询对象

**application-impl 模块**
- **职责**：实现业务用例编排逻辑
- **打包类型**：jar
- **依赖**：application-api, domain-api, common
- **包结构**：
  - `service.impl`：应用服务实现


#### 5. 基础设施层（Infrastructure Layer）

**infrastructure 聚合模块**
- **职责**：聚合基础设施层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：repository、cache、mq

**repository 聚合模块**
- **职责**：聚合仓储层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：repository-api、mysql-impl

**repository-api 模块**
- **职责**：定义数据持久化抽象接口
- **打包类型**：jar
- **依赖**：common
- **包结构**：
  - `repository`：仓储接口定义

**mysql-impl 模块**
- **职责**：基于 MyBatis-Plus 实现 MySQL 数据访问
- **打包类型**：jar
- **依赖**：repository-api, common, MyBatis-Plus, Druid, MySQL Driver
- **包结构**：
  - `repository.impl`：仓储实现
  - `mapper`：MyBatis Mapper 接口
  - `entity`：数据库实体

**cache 聚合模块**
- **职责**：聚合缓存层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：cache-api、redis-impl

**cache-api 模块**
- **职责**：定义缓存访问接口
- **打包类型**：jar
- **依赖**：common
- **包结构**：
  - `cache`：缓存接口定义

**redis-impl 模块**
- **职责**：基于 Lettuce（Spring Data Redis）实现 Redis 缓存
- **打包类型**：jar
- **依赖**：cache-api, common, Spring Data Redis
- **包结构**：
  - `cache.impl`：缓存实现

**mq 聚合模块**
- **职责**：聚合消息队列层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：mq-api、sqs-impl

**mq-api 模块**
- **职责**：定义消息通信接口
- **打包类型**：jar
- **依赖**：common
- **包结构**：
  - `mq`：消息队列接口定义

**sqs-impl 模块**
- **职责**：基于 AWS SDK for SQS 实现消息队列
- **打包类型**：jar
- **依赖**：mq-api, common, AWS SDK for SQS
- **包结构**：
  - `mq.impl`：消息队列实现

#### 6. 接口层（Interface Layer）

**interface 聚合模块**
- **职责**：聚合接口层子模块
- **打包类型**：pom
- **依赖**：无
- **聚合模块**：interface-http、interface-consumer

**interface-http 模块**
- **职责**：处理外部 HTTP 请求
- **打包类型**：jar
- **依赖**：application-api, common, Spring Web
- **包结构**：
  - `controller`：REST 控制器
  - `handler`：全局异常处理器
  - `interceptor`：拦截器

**interface-consumer 模块**
- **职责**：接收和处理消息队列事件
- **打包类型**：jar
- **依赖**：application-api, common
- **包结构**：
  - `consumer`：消息消费者
  - `handler`：全局异常处理器

#### 7. 启动模块（Bootstrap）

**bootstrap 模块**
- **职责**：应用启动入口，整合所有模块
- **打包类型**：jar（可执行）
- **依赖**：interface-http, interface-consumer, application-impl, domain-impl, mysql-impl, redis-impl, sqs-impl, common
- **包结构**：
  - `config`：配置类
  - `Application.java`：主启动类


### 依赖关系图

```
                    ┌─────────────┐
                    │  bootstrap  │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼──────┐  ┌───────▼────────┐
│ interface-http │  │interface-   │  │application-impl│
│                │  │consumer     │  │                │
└───────┬────────┘  └──────┬──────┘  └───────┬────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                    ┌──────▼──────┐
                    │application- │
                    │api          │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼──────┐  ┌───────▼────────┐
│  domain-impl   │  │ domain-api  │  │    common      │
└───────┬────────┘  └──────┬──────┘  └────────────────┘
        │                  │
        └──────────────────┘
                │
    ┌───────────┼───────────┐
    │           │           │
┌───▼────┐ ┌───▼────┐ ┌───▼────┐
│repo-api│ │cache-  │ │mq-api  │
│        │ │api     │ │        │
└───┬────┘ └───┬────┘ └───┬────┘
    │          │          │
┌───▼────┐ ┌───▼────┐ ┌───▼────┐
│mysql-  │ │redis-  │ │sqs-    │
│impl    │ │impl    │ │impl    │
└────────┘ └────────┘ └────────┘
```

**依赖原则**：
- 依赖方向：从上到下，从外到内
- 接口层依赖应用层 API
- 应用层依赖领域层 API
- 领域层依赖基础设施层 API
- 实现模块依赖对应的 API 模块
- 所有模块都可以依赖 common 模块

---

## 技术栈选型

### 核心框架

| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| JDK | 21 | 运行环境 | LTS 版本，Spring Boot 3.4.1 完全支持 |
| Spring Boot | 3.4.1 | 应用框架 | 最新稳定版本，生态成熟 |
| Spring Cloud | 2025.0.0 | 微服务框架 | 与 Spring Boot 3.4.1 兼容 |
| Maven | 3.9+ | 构建工具 | 成熟的依赖管理和构建工具 |

### 数据访问

| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| MyBatis-Plus | 3.5.7 | ORM 框架 | 支持 Spring Boot 3，提供强大的 CRUD 能力 |
| Druid | 1.2.20 | 数据库连接池 | 阿里巴巴开源，支持 Spring Boot 3 |
| MySQL Connector/J | 8.x | MySQL 驱动 | 由 Spring Boot BOM 管理 |
| Spring Data Redis | 3.x | Redis 客户端 | Spring 官方支持，基于 Lettuce |

### 消息队列

| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| AWS SDK for SQS | 2.20.0 | 消息队列客户端 | AWS 官方 SDK，支持 SQS |

### 监控与追踪

| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| Micrometer Tracing | 1.3.5 | 分布式追踪 | Spring Boot 3 官方支持 |
| Logstash Logback Encoder | 7.4 | 日志格式化 | 支持 JSON 格式日志输出 |
| Spring Boot Actuator | 3.4.1 | 监控端点 | Spring Boot 官方监控组件 |
| Micrometer Registry Prometheus | 1.x | Prometheus 集成 | 由 Spring Boot BOM 管理 |


---

## 组件和接口设计

### 接口设计规范

#### 1. 应用层服务接口规范

**接口命名规范**:
- 接口名称使用业务动词 + Service 后缀
- 示例: `OrderApplicationService`、`PaymentApplicationService`

**方法命名规范**:
- 使用业务动词开头,清晰表达业务意图
- 命令操作: `create`、`update`、`delete`、`cancel`、`submit` 等
- 查询操作: `get`、`find`、`list`、`query`、`search` 等
- 示例: `createOrder()`、`cancelOrder()`、`getOrderById()`、`listOrders()`

**参数规范**:
- 命令操作使用 Command 对象作为参数
- Command 对象命名: 动词 + 实体名 + Command
- 示例: `CreateOrderCommand`、`CancelOrderCommand`
- 查询操作使用 Query 对象作为参数
- Query 对象命名: 动词 + 实体名 + Query
- 示例: `GetOrderQuery`、`ListOrdersQuery`

**返回值规范**:
- 返回应用层 DTO 对象,不直接返回领域对象
- DTO 命名: 实体名 + DTO
- 示例: `OrderDTO`、`OrderListDTO`
- 单个对象返回: `OrderDTO`
- 列表返回: `List<OrderDTO>` 或 `Page<OrderDTO>`
- 无返回值操作: `void`

**示例**:
```
接口: OrderApplicationService
方法: OrderDTO createOrder(CreateOrderCommand command)
方法: void cancelOrder(CancelOrderCommand command)
方法: OrderDTO getOrderById(GetOrderQuery query)
方法: Page<OrderDTO> listOrders(ListOrdersQuery query)
```

#### 2. 领域层服务接口规范

**接口命名规范**:
- 接口名称使用领域概念 + DomainService 后缀
- 示例: `OrderDomainService`、`PricingDomainService`

**方法命名规范**:
- 使用领域语言,体现业务规则
- 示例: `calculateOrderAmount()`、`validateOrderStatus()`、`applyDiscount()`

**参数规范**:
- 使用领域对象(实体、值对象)作为参数
- 不使用 DTO 或技术对象
- 示例: `Order`、`OrderItem`、`Money`

**返回值规范**:
- 返回领域对象或基本类型
- 不返回 DTO 或技术对象
- 示例: `Order`、`Money`、`boolean`

**示例**:
```
接口: OrderDomainService
方法: Money calculateOrderAmount(Order order)
方法: boolean validateOrderStatus(Order order, OrderStatus targetStatus)
方法: Order applyDiscount(Order order, Discount discount)
```

#### 3. 仓储接口规范

**接口命名规范**:
- 接口名称使用实体名 + Repository 后缀
- 示例: `OrderRepository`、`OrderItemRepository`

**方法命名规范**:
- 基础 CRUD 操作:
  - 保存: `save(Entity entity)`
  - 更新: `update(Entity entity)`
  - 删除: `delete(Entity entity)` 或 `deleteById(Long id)`
  - 根据 ID 查询: `findById(Long id)`
  - 查询所有: `findAll()`
- 条件查询:
  - 单个结果: `findByXxx(参数)`
  - 多个结果: `findAllByXxx(参数)`
  - 存在性检查: `existsByXxx(参数)`
  - 计数: `countByXxx(参数)`

**参数规范**:
- 使用领域对象(实体)或基本类型
- 不使用 DTO 或技术对象

**返回值规范**:
- 返回领域对象(实体)或基本类型
- 单个对象: `Optional<Entity>` 或 `Entity`
- 多个对象: `List<Entity>` 或 `Page<Entity>`
- 存在性检查: `boolean`
- 计数: `long`

**示例**:
```
接口: OrderRepository
方法: void save(Order order)
方法: void update(Order order)
方法: void deleteById(Long id)
方法: Optional<Order> findById(Long id)
方法: List<Order> findAll()
方法: List<Order> findAllByUserId(Long userId)
方法: Optional<Order> findByOrderNumber(String orderNumber)
方法: boolean existsByOrderNumber(String orderNumber)
方法: long countByStatus(OrderStatus status)
```

#### 4. 缓存接口规范

**接口命名规范**:
- 接口名称使用 Cache 或 CacheService
- 示例: `CacheService`、`RedisCacheService`

**方法命名规范**:
- 获取: `get(String key)` 或 `get(String key, Class<T> type)`
- 设置: `set(String key, Object value)` 或 `set(String key, Object value, long ttl)`
- 删除: `delete(String key)` 或 `delete(Collection<String> keys)`
- 存在性检查: `exists(String key)`
- 设置过期时间: `expire(String key, long ttl)`

**参数规范**:
- key: String 类型,使用业务前缀 + 业务 ID 的格式
- value: Object 类型,支持任意可序列化对象
- ttl: long 类型,单位为秒

**返回值规范**:
- get 操作: `Optional<T>` 或 `T`
- set 操作: `void` 或 `boolean`
- delete 操作: `void` 或 `boolean`
- exists 操作: `boolean`

**示例**:
```
接口: CacheService
方法: <T> Optional<T> get(String key, Class<T> type)
方法: void set(String key, Object value, long ttl)
方法: void delete(String key)
方法: void delete(Collection<String> keys)
方法: boolean exists(String key)
方法: void expire(String key, long ttl)
```

#### 5. 消息队列接口规范

**接口命名规范**:
- 发送接口: MessageSender 或 MessagePublisher
- 接收接口: MessageListener 或 MessageConsumer

**方法命名规范**:
- 发送消息: `send(String queue, Message message)` 或 `publish(String topic, Message message)`
- 发送延迟消息: `sendDelayed(String queue, Message message, long delaySeconds)`
- 批量发送: `sendBatch(String queue, List<Message> messages)`

**参数规范**:
- queue/topic: String 类型,队列或主题名称
- message: 统一的消息对象,包含消息头和消息体
- delaySeconds: long 类型,延迟时间(秒)

**返回值规范**:
- 发送操作: `void` 或 `String`(消息 ID)
- 批量发送: `List<String>`(消息 ID 列表)

**消息对象规范**:
- 消息头: 包含消息 ID、消息类型、时间戳、追踪 ID 等
- 消息体: 业务数据,使用 JSON 格式序列化

**示例**:
```
接口: MessageSender
方法: String send(String queue, Message message)
方法: String sendDelayed(String queue, Message message, long delaySeconds)
方法: List<String> sendBatch(String queue, List<Message> messages)

接口: MessageListener
方法: void onMessage(Message message)
```

#### 6. 接口层 Controller 规范

**类命名规范**:
- 使用实体名 + Controller 后缀
- 示例: `OrderController`、`PaymentController`

**方法命名规范**:
- RESTful 风格:
  - 创建: `create` + 实体名
  - 更新: `update` + 实体名
  - 删除: `delete` + 实体名
  - 查询单个: `get` + 实体名
  - 查询列表: `list` + 实体名复数

**URL 路径规范**:
- 使用小写字母和连字符
- 资源使用复数形式
- 示例: `/api/orders`、`/api/orders/{id}`、`/api/orders/{id}/items`

**HTTP 方法规范**:
- POST: 创建资源
- PUT: 完整更新资源
- PATCH: 部分更新资源
- DELETE: 删除资源
- GET: 查询资源

**参数规范**:
- 路径参数: 使用 @PathVariable,用于资源 ID
- 查询参数: 使用 @RequestParam,用于过滤、排序、分页
- 请求体: 使用 @RequestBody,用于创建和更新操作

**返回值规范**:
- 统一使用 Result<T> 包装
- 成功: Result.success(data)
- 失败: Result.error(code, message)

**示例**:
```
Controller: OrderController
方法: Result<OrderDTO> createOrder(@RequestBody CreateOrderRequest request)
路径: POST /api/orders

方法: Result<OrderDTO> getOrder(@PathVariable Long id)
路径: GET /api/orders/{id}

方法: Result<Page<OrderDTO>> listOrders(@RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size)
路径: GET /api/orders
```

#### 7. 通用设计原则

**依赖方向**:
- 上层依赖下层的接口,不依赖实现
- 接口定义在 API 模块,实现在 Impl 模块

**参数传递**:
- 避免使用 Map 传递参数,使用明确的对象
- 参数超过 3 个时,封装为对象

**返回值处理**:
- 可能为空的返回值使用 Optional
- 集合类型不返回 null,返回空集合

**异常处理**:
- 接口方法不声明受检异常(checked exception)
- 使用运行时异常(RuntimeException)
- 业务异常使用 BusinessException
- 系统异常使用 SystemException

**命名一致性**:
- 同一概念在不同层使用一致的命名
- 避免使用缩写,使用完整的单词
- 使用业务语言,避免技术术语

### 异常体系设计

#### 异常类层次结构

异常体系采用三层结构:
- **BaseException**: 抽象基类,包含错误码(errorCode)和错误消息(errorMessage)字段,继承自 RuntimeException
- **BusinessException**: 业务异常类,继承自 BaseException,用于表示业务规则验证失败等场景
- **SystemException**: 系统异常类,继承自 BaseException,用于表示系统级错误(如数据库连接失败)

### 统一响应类设计

**Result 类**

统一响应类用于封装所有 API 的响应结果,包含以下字段:
- **code**: 响应码(成功或错误码)
- **message**: 响应消息
- **data**: 响应数据(泛型,支持任意类型)
- **timestamp**: 响应时间戳

提供静态工厂方法:
- **success(T data)**: 创建成功响应
- **error(String code, String message)**: 创建错误响应

### 全局异常处理器设计

#### HTTP 接口异常处理器

使用 @RestControllerAdvice 注解实现全局异常处理器,捕获以下异常:

**BusinessException 处理**:
- 返回 HTTP 状态码 200
- 返回 Result 对象,code 为业务错误码,message 为业务错误消息
- 日志级别为 WARN

**SystemException 处理**:
- 返回 HTTP 状态码 500
- 返回 Result 对象,code 为系统错误码,message 为系统错误消息
- 日志级别为 ERROR

**未知异常处理**:
- 返回 HTTP 状态码 500
- 返回 Result 对象,code 为通用错误码,message 为通用错误消息
- 不暴露内部实现细节
- 日志级别为 ERROR

#### 消息消费者异常处理器

使用 @ControllerAdvice 注解实现全局异常处理器:
- 捕获所有异常
- 记录详细的错误日志
- 不向外抛出异常,避免影响消息队列的正常运行


---

## 数据模型

### Maven 模块结构

```
order-service-parent/
├── pom.xml (父 POM，聚合 6 个顶层模块)
├── common/
│   ├── pom.xml
│   └── src/main/java/com/catface/orderservice/common/
│       ├── exception/
│       │   ├── BaseException.java
│       │   ├── BusinessException.java
│       │   └── SystemException.java
│       ├── dto/
│       │   └── Result.java
│       ├── constant/
│       └── util/
├── domain/
│   ├── pom.xml (聚合模块，packaging=pom)
│   ├── domain-api/
│   │   ├── pom.xml
│   │   └── src/main/java/com/catface/orderservice/domain/
│   │       ├── model/
│   │       ├── repository/
│   │       └── service/
│   └── domain-impl/
│       ├── pom.xml
│       └── src/main/java/com/catface/orderservice/domain/service/impl/
├── application/
│   ├── pom.xml (聚合模块，packaging=pom)
│   ├── application-api/
│   │   ├── pom.xml
│   │   └── src/main/java/com/catface/orderservice/application/
│   │       ├── service/
│   │       ├── dto/
│   │       ├── command/
│   │       └── query/
│   └── application-impl/
│       ├── pom.xml
│       └── src/main/java/com/catface/orderservice/application/service/impl/
├── infrastructure/
│   ├── pom.xml (聚合模块，packaging=pom)
│   ├── repository/
│   │   ├── pom.xml (聚合模块，packaging=pom)
│   │   ├── repository-api/
│   │   │   ├── pom.xml
│   │   │   └── src/main/java/com/catface/orderservice/infrastructure/repository/
│   │   └── mysql-impl/
│   │       ├── pom.xml
│   │       └── src/main/java/com/catface/orderservice/infrastructure/repository/
│   │           ├── impl/
│   │           ├── mapper/
│   │           └── entity/
│   ├── cache/
│   │   ├── pom.xml (聚合模块，packaging=pom)
│   │   ├── cache-api/
│   │   │   ├── pom.xml
│   │   │   └── src/main/java/com/catface/orderservice/infrastructure/cache/
│   │   └── redis-impl/
│   │       ├── pom.xml
│   │       └── src/main/java/com/catface/orderservice/infrastructure/cache/impl/
│   └── mq/
│       ├── pom.xml (聚合模块，packaging=pom)
│       ├── mq-api/
│       │   ├── pom.xml
│       │   └── src/main/java/com/catface/orderservice/infrastructure/mq/
│       └── sqs-impl/
│           ├── pom.xml
│           └── src/main/java/com/catface/orderservice/infrastructure/mq/impl/
├── interface/
│   ├── pom.xml (聚合模块，packaging=pom)
│   ├── interface-http/
│   │   ├── pom.xml
│   │   └── src/main/java/com/catface/orderservice/interfaces/http/
│   │       ├── controller/
│   │       ├── handler/
│   │       └── interceptor/
│   └── interface-consumer/
│       ├── pom.xml
│       └── src/main/java/com/catface/orderservice/interfaces/consumer/
│           ├── consumer/
│           └── handler/
└── bootstrap/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/catface/orderservice/
        │   │   ├── Application.java
        │   │   └── config/
        │   └── resources/
        │       ├── application.yml
        │       ├── application-local.yml
        │       ├── application-dev.yml
        │       ├── application-test.yml
        │       ├── application-staging.yml
        │       ├── application-prod.yml
        │       └── logback-spring.xml
        └── test/
```


### 配置文件结构

#### application.yml（通用配置）

通用配置文件包含以下内容:
- **应用名称**: order-service
- **默认激活的 profile**: local
- **服务端口**: 8080
- **Actuator 端点配置**: 暴露 prometheus、health、info 端点,基础路径为 /actuator
- **Prometheus 指标导出**: 启用 Prometheus 指标导出

#### application-{profile}.yml（环境特定配置）

每个环境的配置文件包含以下内容:

**数据库配置**:
- 数据库 URL(支持环境变量)
- 用户名和密码(通过环境变量注入)
- 驱动类名: com.mysql.cj.jdbc.Driver

**Redis 配置**:
- Redis 主机地址(支持环境变量)
- Redis 端口(支持环境变量)
- Redis 密码(通过环境变量注入)

**AWS SQS 配置**:
- AWS 区域(支持环境变量)
- SQS 队列 URL(支持环境变量)

#### logback-spring.xml（日志配置）

日志配置按环境分为三种模式:

**local 环境**:
- 输出到控制台
- 彩色日志格式
- 项目包（com.catface.orderservice）使用 DEBUG 级别
- 框架包使用 WARN 级别

**dev、test 环境**:
- 输出到文件(logs/application.log)
- JSON 格式日志(使用 Logstash Logback Encoder)
- 包含 traceId 和 spanId
- 按日期滚动,单个文件超过 100MB 时分割
- 保留最近 30 天的日志
- 项目包使用 DEBUG 级别
- 框架包使用 WARN 级别

**staging 环境**:
- 输出到文件(logs/application.log)
- JSON 格式日志(使用 Logstash Logback Encoder)
- 包含 traceId 和 spanId
- 按日期滚动,单个文件超过 100MB 时分割
- 保留最近 30 天的日志
- 项目包使用 INFO 级别
- 框架包使用 WARN 级别

**prod 环境**:
- 输出到文件(logs/application.log)
- JSON 格式日志(使用 Logstash Logback Encoder)
- 包含 traceId 和 spanId
- 按日期滚动,单个文件超过 100MB 时分割
- 保留最近 90 天的日志
- 项目包使用 INFO 级别
- 框架包使用 WARN 级别
- ERROR 级别日志单独输出到 logs/error.log


---

## 错误处理

### 错误码设计

错误码采用 6 位数字格式：`XXYZZZ`
- `XX`：模块代码（01-99）
- `Y`：错误类型（1-业务错误，5-系统错误）
- `ZZZ`：具体错误序号（001-999）

**示例**：
- `011001`：订单模块业务错误 001
- `015001`：订单模块系统错误 001

### 异常处理流程

```
请求 → Controller → Application Service → Domain Service
                                              ↓
                                         抛出异常
                                              ↓
                                    GlobalExceptionHandler
                                              ↓
                                         转换为 Result
                                              ↓
                                         返回响应
```

### 异常处理策略

#### 业务异常（BusinessException）
- **场景**：业务规则验证失败、数据不存在等
- **处理**：返回 HTTP 200，Result.code 为业务错误码
- **日志级别**：WARN
- **示例**：订单不存在、库存不足

#### 系统异常（SystemException）
- **场景**：数据库连接失败、外部服务调用失败等
- **处理**：返回 HTTP 500，Result.code 为系统错误码
- **日志级别**：ERROR
- **示例**：数据库连接超时、Redis 连接失败

#### 未知异常（Exception）
- **场景**：未预期的异常
- **处理**：返回 HTTP 500，Result.code 为通用错误码
- **日志级别**：ERROR
- **安全**：不暴露内部实现细节

### 日志记录规范

**日志字段**：
- `timestamp`：时间戳
- `level`：日志级别
- `thread`：线程名
- `logger`：日志记录器
- `traceId`：链路追踪 ID
- `spanId`：调用片段 ID
- `message`：日志消息
- `exception`：异常堆栈（如有）

**日志级别使用**：
- `DEBUG`：开发调试信息（仅 local、dev、test、staging 环境）
- `INFO`：重要业务流程节点
- `WARN`：业务异常、降级处理
- `ERROR`：系统异常、未知异常

---

## 测试策略

### 单元测试

**测试范围**：
- 领域服务逻辑
- 应用服务编排逻辑
- 工具类方法

**测试框架**：
- JUnit 5
- Mockito
- AssertJ

**测试原则**：
- 每个公共方法都应有对应的单元测试
- 测试覆盖率目标：80%
- 使用 Mock 隔离外部依赖

### 集成测试

**测试范围**：
- HTTP 接口
- 数据库访问
- 缓存访问
- 消息队列

**测试框架**：
- Spring Boot Test
- Testcontainers（用于 MySQL、Redis）
- LocalStack（用于 SQS）

**测试原则**：
- 使用真实的外部依赖（通过容器）
- 测试完整的请求-响应流程
- 验证数据持久化和状态变更


---

## 非功能性设计

### 性能设计

**目标指标**：
- API 响应时间：P95 < 200ms，P99 < 500ms
- 数据库查询时间：P95 < 50ms
- 缓存命中率：> 80%
- 系统吞吐量：> 1000 TPS

**优化策略**：
- 使用 Redis 缓存热点数据
- 数据库连接池优化（Druid）
- 异步处理非关键业务
- 合理使用索引

### 安全设计

**数据安全**：
- 敏感数据加密存储
- 数据库连接使用 SSL
- Redis 连接使用密码认证

**接口安全**：
- API 认证和授权（预留扩展点）
- 请求参数校验
- SQL 注入防护（MyBatis-Plus 参数化查询）
- XSS 防护

**日志安全**：
- 敏感信息脱敏
- 不记录密码、密钥等敏感数据

### 可观测性设计

**日志**：
- 结构化日志（JSON 格式）
- 统一日志格式
- 日志分级存储
- 日志轮转和归档

**监控**：
- JVM 指标（内存、GC、线程）
- HTTP 请求指标（QPS、延迟、错误率）
- 数据库连接池指标
- 缓存命中率指标
- 业务指标（订单量、成功率等）

**链路追踪**：
- 分布式链路追踪（Micrometer Tracing）
- Trace ID 和 Span ID 自动生成和传播
- 跨服务调用追踪

**告警**：
- 错误率告警
- 响应时间告警
- 资源使用率告警

### 可扩展性设计

**水平扩展**：
- 无状态设计
- 支持多实例部署
- 负载均衡

**模块扩展**：
- 接口与实现分离
- 依赖倒置原则
- 易于替换实现

**配置扩展**：
- 多环境配置支持
- 外部化配置
- 动态配置（预留扩展点）


---

## 技术决策记录（ADR）

### ADR-001：选择 DDD 分层架构

**状态**：已接受

**背景**：需要选择系统架构模式，确保业务逻辑与技术实现解耦，支持未来的微服务化演进。

**决策**：采用 DDD 分层架构，将系统划分为接口层、应用层、领域层和基础设施层。

**理由**：
- 清晰的职责划分，便于理解和维护
- 业务逻辑与技术实现解耦，易于测试
- 支持领域模型的演进
- 为未来的微服务化提供良好基础
- 团队对 DDD 有一定了解

**后果**：
- 正面：代码结构清晰，易于维护和扩展
- 负面：初期需要更多的模块和接口定义，学习成本较高

---

### ADR-002：选择 Maven 多模块工程

**状态**：已接受

**背景**：需要选择项目构建方式，确保模块间的依赖关系清晰，支持独立构建和测试。

**决策**：采用 Maven 多模块工程，每个层次的 API 和实现分别作为独立模块。

**理由**：
- Maven 是 Java 生态中成熟的构建工具
- 多模块工程支持清晰的依赖管理
- 可以独立构建和测试每个模块
- 便于版本管理和发布
- 团队熟悉 Maven

**后果**：
- 正面：依赖关系清晰，构建灵活，易于管理
- 负面：模块数量较多，初期配置工作量较大

---

### ADR-003：选择 MyBatis-Plus 作为 ORM 框架

**状态**：已接受

**背景**：需要选择数据访问技术，平衡开发效率和灵活性。

**决策**：选择 MyBatis-Plus 作为 ORM 框架。

**理由**：
- 支持 Spring Boot 3（使用 mybatis-plus-spring-boot3-starter）
- 提供强大的 CRUD 能力，减少样板代码
- 支持 Lambda 查询，类型安全
- 支持复杂 SQL（通过 XML）
- 团队有 MyBatis 使用经验
- 性能优秀

**后果**：
- 正面：开发效率高，代码简洁，性能好
- 负面：需要学习 MyBatis-Plus 特性，复杂查询仍需编写 XML

---

### ADR-004：选择 Micrometer Tracing 实现链路追踪

**状态**：已接受

**背景**：需要实现分布式链路追踪，支持问题排查和性能分析。

**决策**：选择 Micrometer Tracing 作为链路追踪实现。

**理由**：
- Spring Boot 3 官方支持
- 统一的追踪抽象，支持多种后端（Zipkin、Jaeger 等）
- 自动生成和传播 Trace ID 和 Span ID
- 与 Spring Cloud 生态集成良好
- 配置简单

**后果**：
- 正面：开箱即用，配置简单，生态成熟
- 负面：需要额外的追踪后端（如 Zipkin）来存储和查询追踪数据

---

### ADR-005：选择 Logstash Logback Encoder 实现结构化日志

**状态**：已接受

**背景**：需要输出结构化日志，便于日志收集和分析。

**决策**：选择 Logstash Logback Encoder 实现 JSON 格式日志输出。

**理由**：
- 支持 JSON 格式日志输出
- 自动包含 MDC 上下文（Trace ID、Span ID）
- 与 ELK 栈集成良好
- 配置灵活
- 社区活跃

**后果**：
- 正面：日志格式统一，易于解析和分析
- 负面：本地开发时 JSON 日志可读性较差（通过环境配置解决）

---

### ADR-006：接口与实现分离

**状态**：已接受

**背景**：需要确保系统的可扩展性和可替换性。

**决策**：每个层次都分为 API 模块和实现模块，上层依赖下层的 API 模块。

**理由**：
- 依赖倒置原则，上层不依赖下层的具体实现
- 易于替换实现（如从 MySQL 切换到 PostgreSQL）
- 支持多种实现共存
- 便于单元测试（使用 Mock）
- 清晰的模块边界

**后果**：
- 正面：系统灵活性高，易于扩展和替换
- 负面：模块数量增加，初期开发工作量较大


---

## 风险和应对

### 风险 1：模块数量过多，初期配置复杂

**风险等级**：中

**影响**：
- 初期搭建工作量较大
- 开发人员需要理解模块结构
- 可能影响开发效率

**应对策略**：
- 提供清晰的模块结构文档
- 使用脚手架工具快速创建模块
- 提供示例代码和最佳实践
- 逐步搭建，先搭建核心模块

**备选方案**：
- 初期可以合并部分模块（如 API 和实现）
- 待系统稳定后再拆分

---

### 风险 2：DDD 学习曲线

**风险等级**：中

**影响**：
- 团队需要学习 DDD 概念和实践
- 可能出现理解偏差
- 初期开发速度较慢

**应对策略**：
- 提供 DDD 培训和文档
- 代码审查确保实践正确
- 从简单场景开始，逐步深入
- 定期回顾和总结

**备选方案**：
- 初期采用简化的分层架构
- 待团队熟悉后再引入完整的 DDD 实践

---

### 风险 3：依赖版本兼容性

**风险等级**：低

**影响**：
- Spring Boot 3 与某些第三方库可能不兼容
- 升级依赖可能引入 Breaking Changes

**应对策略**：
- 使用 Spring Boot BOM 管理依赖版本
- 选择官方支持 Spring Boot 3 的库
- 充分测试依赖升级
- 关注社区动态

**备选方案**：
- 使用兼容的旧版本库
- 自行适配或等待官方支持

---

### 风险 4：性能瓶颈

**风险等级**：中

**影响**：
- 分层架构可能增加调用链路
- 多模块可能影响启动速度

**应对策略**：
- 使用缓存减少数据库访问
- 异步处理非关键业务
- 性能测试和优化
- 监控关键指标

**备选方案**：
- 合并部分层次减少调用链路
- 使用 GraalVM Native Image 提升启动速度

---

### 风险 5：日志和追踪数据量过大

**风险等级**：低

**影响**：
- 日志文件占用大量磁盘空间
- 追踪数据存储成本高

**应对策略**：
- 配置日志轮转和归档
- 合理设置日志级别
- 采样追踪数据（生产环境）
- 定期清理历史数据

**备选方案**：
- 使用日志收集系统（如 ELK）
- 使用追踪采样策略

---

## 部署架构

### 本地开发环境

```
┌─────────────────┐
│  Developer PC   │
│                 │
│  ┌───────────┐  │
│  │ Order     │  │
│  │ Service   │  │
│  └─────┬─────┘  │
│        │        │
│  ┌─────▼─────┐  │
│  │  MySQL    │  │
│  │  (Docker) │  │
│  └───────────┘  │
│                 │
│  ┌───────────┐  │
│  │  Redis    │  │
│  │  (Docker) │  │
│  └───────────┘  │
│                 │
│  ┌───────────┐  │
│  │LocalStack │  │
│  │  (Docker) │  │
│  └───────────┘  │
└─────────────────┘
```

### 生产环境（示例）

```
┌─────────────────────────────────────────────────────────┐
│                      Load Balancer                       │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼───────┐
│ Order Service│ │Order Service│ │Order Service│
│  Instance 1  │ │ Instance 2  │ │ Instance 3  │
└───────┬──────┘ └──────┬──────┘ └─────┬───────┘
        │               │               │
        └───────────────┼───────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼───────┐
│    MySQL     │ │    Redis    │ │   AWS SQS   │
│   (RDS)      │ │ (ElastiCache│ │             │
└──────────────┘ └─────────────┘ └─────────────┘
```

---

## 实施计划

### 阶段 1：基础设施搭建（优先级：高）

1. 创建 Maven 父工程和模块结构
2. 配置依赖管理
3. 创建通用模块（异常、响应类）
4. 配置日志和链路追踪

### 阶段 2：核心层次实现（优先级：高）

1. 创建领域层模块（API 和实现）
2. 创建应用层模块（API 和实现）
3. 创建基础设施层模块（API 和实现）
4. 创建接口层模块

### 阶段 3：集成和配置（优先级：高）

1. 创建启动模块
2. 配置多环境支持
3. 实现全局异常处理
4. 集成 Prometheus 监控

### 阶段 4：测试和优化（优先级：中）

1. 编写单元测试
2. 编写集成测试
3. 性能测试和优化
4. 文档完善

---

## 总结

本设计文档定义了订单服务 DDD 架构的完整技术方案，包括：

1. **清晰的模块划分**：采用 DDD 分层架构，每层分为 API 和实现模块
2. **合理的技术选型**：基于 Spring Boot 3.4.1 和 Spring Cloud 2025.0.0
3. **完善的基础设施**：日志、追踪、监控、异常处理
4. **灵活的配置管理**：支持多环境配置
5. **可扩展的架构**：接口与实现分离，易于替换和扩展

该架构为订单服务的开发提供了坚实的基础，支持未来的业务演进和技术升级。

