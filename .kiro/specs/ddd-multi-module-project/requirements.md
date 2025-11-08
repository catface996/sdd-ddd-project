# 需求文档

## 简介

本文档定义了基于Spring Cloud和领域驱动设计（DDD）的多模块Maven工程的需求。该系统名为order-service，采用清晰的分层架构，实现技术与业务逻辑的解耦，支持微服务演进。

## 术语表

- **System**: order-service系统，基于Spring Cloud的多模块Maven工程
- **Parent POM**: Maven父项目配置文件，统一管理所有子模块的依赖版本
- **Module**: Maven子模块，代表系统中的一个独立组件
- **DDD Layer**: 领域驱动设计的分层结构（接口层、应用层、领域层、基础设施层）
- **API Module**: 定义接口和契约的模块，不包含实现
- **Implementation Module**: 实现具体业务逻辑或技术功能的模块
- **Bootstrap Module**: 系统启动入口模块
- **Traffic Layer**: 接口层，处理外部请求（HTTP、消息队列）
- **Application Layer**: 应用层，编排业务用例
- **Domain Layer**: 领域层，封装核心业务规则
- **Infrastructure Layer**: 基础设施层，提供技术实现（数据库、缓存、消息队列）
- **Repository**: 仓储接口，定义数据持久化抽象
- **Cache Interface**: 缓存接口，定义缓存访问抽象
- **MQ Interface**: 消息队列接口，定义消息通信抽象
- **Exception Hierarchy**: 异常层次结构，定义系统中所有自定义异常的继承关系
- **Global Exception Handler**: 全局异常处理器，统一处理HTTP请求中抛出的异常
- **Prometheus**: 开源监控系统，用于收集和存储时间序列指标数据
- **Micrometer**: 应用指标收集框架，提供统一的指标API
- **Actuator**: Spring Boot提供的生产就绪特性，用于暴露应用运行时信息

## 需求

### 需求 1：项目结构初始化

**用户故事：** 作为开发人员，我希望创建符合DDD架构的多模块Maven项目结构，以便建立清晰的模块边界和依赖关系

#### 验收标准

1. THE System SHALL 在项目根目录创建 pom.xml 文件，其 groupId 为 "com.catface.order"，artifactId 为 "order-service"，packaging 类型为 "pom"
2. THE System SHALL 创建以下模块目录结构：common、bootstrap、traffic、traffic/http、traffic/consumer、application、application/application-api、application/application-impl、domain、domain/domain-api、domain/domain-impl、infrastructure、infrastructure/repository、infrastructure/cache、infrastructure/mq
3. THE System SHALL 在 infrastructure/repository 目录下创建 repository-api 和 mysql-impl 子模块目录
4. THE System SHALL 在 infrastructure/cache 目录下创建 cache-api 和 redis-impl 子模块目录
5. THE System SHALL 在 infrastructure/mq 目录下创建 mq-api 和 sqs-impl 子模块目录

### 需求 2：依赖管理配置

**用户故事：** 作为开发人员，我希望在父POM中统一管理所有依赖版本，以便确保模块间的依赖一致性

#### 验收标准

1. THE Parent POM SHALL 在 dependencyManagement 节中声明 spring-boot-dependencies 版本 3.3.5，type 为 "pom"，scope 为 "import"
2. THE Parent POM SHALL 在 dependencyManagement 节中声明 spring-cloud-dependencies 版本 2024.0.0，type 为 "pom"，scope 为 "import"
3. THE Parent POM SHALL 在 dependencyManagement 节中声明 mybatis-plus-boot-starter 版本 3.5.9
4. THE Parent POM SHALL 在 dependencyManagement 节中声明 jedis 版本 5.2.0
5. THE Parent POM SHALL 在 dependencyManagement 节中声明 lombok 版本 1.18.34 和 junit-jupiter 版本 5.10.3
6. THE Parent POM SHALL 配置 maven-compiler-plugin，将 source 和 target 设置为 21

### 需求 3：接口层模块创建

**用户故事：** 作为开发人员，我希望创建接口层模块来处理外部请求，以便实现HTTP API和消息队列消费功能

#### 验收标准

1. THE System SHALL 创建 traffic/pom.xml 文件，packaging 类型为 "pom"，modules 列表包含 "http" 和 "consumer"
2. THE traffic/http/pom.xml SHALL 声明对 "application-api"（groupId 为 "com.catface.order"）和 "common"（groupId 为 "com.catface.order"）的依赖
3. THE traffic/consumer/pom.xml SHALL 声明对 "application-api"（groupId 为 "com.catface.order"）和 "common"（groupId 为 "com.catface.order"）的依赖
4. THE traffic/http/pom.xml SHALL 包含 spring-boot-starter-web 和 spring-boot-starter-validation 依赖
5. THE traffic/http/pom.xml SHALL NOT 包含任何基础设施实现依赖（mysql-impl、redis-impl、sqs-impl）

### 需求 4：应用层模块创建

**用户故事：** 作为开发人员，我希望创建应用层模块来编排业务用例，以便协调领域层完成业务逻辑

#### 验收标准

1. THE System SHALL 创建 application/pom.xml 文件，packaging 类型为 "pom"，modules 列表包含 "application-api" 和 "application-impl"
2. THE application/application-api/pom.xml SHALL 仅声明对 "common"（groupId 为 "com.catface.order"）的依赖
3. THE application/application-impl/pom.xml SHALL 声明对 "application-api"、"domain-api" 和 "common" 的依赖，所有 groupId 均为 "com.catface.order"
4. THE application/application-impl/pom.xml SHALL NOT 包含对 mysql-impl、redis-impl 或 sqs-impl 的依赖
5. THE application/application-api/pom.xml SHALL NOT 包含任何 Spring Boot starter 依赖

### 需求 5：领域层模块创建

**用户故事：** 作为开发人员，我希望创建领域层模块来封装核心业务规则，以便实现领域驱动设计

#### 验收标准

1. THE System SHALL 创建 domain/pom.xml 文件，packaging 类型为 "pom"，modules 列表包含 "domain-api" 和 "domain-impl"
2. THE domain/domain-api/pom.xml SHALL 仅声明对 "common"（groupId 为 "com.catface.order"）的依赖
3. THE domain/domain-impl/pom.xml SHALL 声明对 "domain-api"、"repository-api"、"cache-api"、"mq-api" 和 "common" 的依赖，所有 groupId 均为 "com.catface.order"
4. THE domain/domain-impl/pom.xml SHALL NOT 包含对 mysql-impl、redis-impl 或 sqs-impl 的依赖
5. THE domain/domain-api/pom.xml SHALL NOT 包含任何基础设施依赖

### 需求 6：基础设施层-仓储模块创建

**用户故事：** 作为开发人员，我希望创建仓储模块来实现数据持久化，以便支持MySQL数据库访问

#### 验收标准

1. THE System SHALL 创建 infrastructure/repository/pom.xml 文件，packaging 类型为 "pom"，modules 列表包含 "repository-api" 和 "mysql-impl"
2. THE infrastructure/repository/repository-api/pom.xml SHALL 仅声明对 "common"（groupId 为 "com.catface.order"）的依赖
3. THE infrastructure/repository/mysql-impl/pom.xml SHALL 声明对 "repository-api" 和 "common" 的依赖，groupId 均为 "com.catface.order"
4. THE infrastructure/repository/mysql-impl/pom.xml SHALL 包含 mybatis-plus-boot-starter 依赖
5. THE infrastructure/repository/mysql-impl/pom.xml SHALL 包含 mysql-connector-j 依赖，scope 为 "runtime"

### 需求 7：基础设施层-缓存模块创建

**用户故事：** 作为开发人员，我希望创建缓存模块来实现缓存访问，以便支持Redis缓存功能

#### 验收标准

1. THE System SHALL 创建 infrastructure/cache/pom.xml 文件，packaging 类型为 "pom"，modules 列表包含 "cache-api" 和 "redis-impl"
2. THE infrastructure/cache/cache-api/pom.xml SHALL 仅声明对 "common"（groupId 为 "com.catface.order"）的依赖
3. THE infrastructure/cache/redis-impl/pom.xml SHALL 声明对 "cache-api" 和 "common" 的依赖，groupId 均为 "com.catface.order"
4. THE infrastructure/cache/redis-impl/pom.xml SHALL 包含 jedis 依赖
5. THE infrastructure/cache/cache-api/pom.xml SHALL NOT 包含任何第三方缓存库依赖

### 需求 8：基础设施层-消息队列模块创建

**用户故事：** 作为开发人员，我希望创建消息队列模块来实现消息通信，以便支持AWS SQS消息功能

#### 验收标准

1. THE System SHALL 创建 infrastructure/mq/pom.xml 文件，packaging 类型为 "pom"，modules 列表包含 "mq-api" 和 "sqs-impl"
2. THE infrastructure/mq/mq-api/pom.xml SHALL 仅声明对 "common"（groupId 为 "com.catface.order"）的依赖
3. THE infrastructure/mq/sqs-impl/pom.xml SHALL 声明对 "mq-api" 和 "common" 的依赖，groupId 均为 "com.catface.order"
4. THE infrastructure/mq/sqs-impl/pom.xml SHALL 包含 aws-java-sdk-sqs 依赖，版本为 1.12.x
5. THE infrastructure/mq/mq-api/pom.xml SHALL NOT 包含任何 AWS SDK 依赖

### 需求 9：启动模块配置

**用户故事：** 作为开发人员，我希望配置启动模块来启动整个应用，以便集成所有功能模块

#### 验收标准

1. THE bootstrap/pom.xml SHALL 声明对 "http"、"consumer"、"application-impl"、"domain-impl"、"mysql-impl"、"redis-impl"、"sqs-impl" 和 "common" 的依赖，所有 groupId 均为 "com.catface.order"
2. THE bootstrap/pom.xml SHALL 包含 spring-boot-starter 依赖
3. THE bootstrap/pom.xml SHALL 配置 spring-boot-maven-plugin 插件，包含 repackage goal
4. THE bootstrap Module SHALL 包含一个名为 "OrderServiceApplication" 的 Java 类，位于包 "com.catface.com.orderservice" 中，带有 @SpringBootApplication 注解和 main 方法
5. THE bootstrap/pom.xml SHALL 的 packaging 类型为 "jar"

### 需求 10：模块依赖关系验证

**用户故事：** 作为开发人员，我希望验证模块间的依赖关系符合DDD分层原则，以便防止循环依赖和职责混乱

#### 验收标准

1. WHEN 执行 "mvn dependency:tree" 命令时，THE System SHALL 成功完成且不出现循环依赖错误
2. THE domain-api Module SHALL NOT 声明对 application-api 或任何 traffic 模块的依赖
3. THE domain-impl Module SHALL NOT 声明对 application-impl 或任何 traffic 模块的依赖
4. THE mysql-impl Module SHALL NOT 声明对 redis-impl 或 sqs-impl 的依赖
5. THE application-api Module SHALL NOT 声明对 domain-impl、mysql-impl、redis-impl 或 sqs-impl 的依赖

### 需求 11：基础包结构创建

**用户故事：** 作为开发人员，我希望在每个模块中创建符合规范的Java包结构，以便组织代码文件

#### 验收标准

1. THE System SHALL 在每个模块中创建目录 "src/main/java/com/catface/com/orderservice"
2. THE common Module SHALL 在基础包目录下包含子目录 "utils"、"exceptions"、"constants" 和 "dto"
3. THE traffic/http Module SHALL 在基础包目录下包含子目录 "controller" 和 "converter"
4. THE application/application-api Module SHALL 在基础包目录下包含子目录 "service"、"dto"、"command" 和 "query"
5. THE domain/domain-api Module SHALL 在基础包目录下包含子目录 "model"、"aggregate"、"entity"、"valueobject" 和 "repository"

### 需求 12：配置文件模板创建

**用户故事：** 作为开发人员，我希望创建多环境配置文件模板，以便支持不同环境的部署

#### 验收标准

1. THE bootstrap Module SHALL 包含文件 "src/main/resources/application.yml"，其中 spring.application.name 设置为 "order-service"
2. THE bootstrap Module SHALL 包含文件 "src/main/resources/application-dev.yml"，其中 spring.config.activate.on-profile 属性设置为 "dev"
3. THE bootstrap Module SHALL 包含文件 "src/main/resources/application-staging.yml"，其中 spring.config.activate.on-profile 属性设置为 "staging"
4. THE bootstrap Module SHALL 包含文件 "src/main/resources/application-prod.yml"，其中 spring.config.activate.on-profile 属性设置为 "prod"
5. THE application.yml 文件 SHALL 包含 spring.datasource、spring.data.redis 和 aws.sqs 配置属性的占位符部分

### 需求 13：父POM模块声明

**用户故事：** 作为开发人员，我希望在父POM中声明所有子模块，以便Maven能够正确构建整个项目

#### 验收标准

1. THE Parent POM SHALL 在 modules 节中声明 "common"
2. THE Parent POM SHALL 在 modules 节中声明 "bootstrap"
3. THE Parent POM SHALL 在 modules 节中声明 "traffic"、"application"、"domain" 和 "infrastructure"
4. THE Parent POM SHALL 在根级别包含恰好 5 个模块声明
5. WHEN 在项目根目录执行 "mvn clean install" 命令时，THE System SHALL 成功构建所有模块

### 需求 14：基础设施层父模块配置

**用户故事：** 作为开发人员，我希望创建基础设施层父模块来组织所有基础设施子模块，以便统一管理

#### 验收标准

1. THE System SHALL 创建 infrastructure/pom.xml 文件，packaging 类型为 "pom"
2. THE infrastructure/pom.xml SHALL 在 modules 节中声明 "repository"、"cache" 和 "mq"
3. THE infrastructure/pom.xml SHALL 包含对根 pom 的父引用，groupId 为 "com.catface.order"，artifactId 为 "order-service"
4. THE infrastructure/pom.xml SHALL 的 artifactId 为 "infrastructure"
5. THE infrastructure/pom.xml SHALL NOT 声明任何依赖

### 需求 15：通用异常体系创建

**用户故事：** 作为开发人员，我希望创建统一的异常体系，以便规范化错误处理和异常传播

#### 验收标准

1. THE common Module SHALL 在 "exceptions" 包下创建 BaseException 类作为所有自定义异常的基类
2. THE common Module SHALL 在 "exceptions" 包下创建 BusinessException 类用于业务逻辑异常
3. THE common Module SHALL 在 "exceptions" 包下创建 SystemException 类用于系统级异常
4. THE common Module SHALL 在 "exceptions" 包下创建 ValidationException 类用于参数验证异常
5. THE common Module SHALL 在 "exceptions" 包下创建 InfrastructureException 类用于基础设施异常

### 需求 16：全局异常处理器创建

**用户故事：** 作为开发人员，我希望在HTTP模块中实现全局异常处理器，以便统一处理和返回错误响应

#### 验收标准

1. THE traffic/http Module SHALL 创建 GlobalExceptionHandler 类，带有 @RestControllerAdvice 注解
2. THE GlobalExceptionHandler SHALL 包含处理 BusinessException 的方法，带有 @ExceptionHandler 注解
3. THE GlobalExceptionHandler SHALL 包含处理 ValidationException 的方法，带有 @ExceptionHandler 注解
4. THE GlobalExceptionHandler SHALL 包含处理通用 Exception 的方法，带有 @ExceptionHandler 注解
5. THE GlobalExceptionHandler SHALL 返回统一的错误响应格式，包含错误码、错误消息和时间戳



### 需求 17：Prometheus监控集成

**用户故事：** 作为开发人员，我希望集成Prometheus监控，以便收集和暴露应用运行指标

#### 验收标准

1. THE bootstrap/pom.xml SHALL 包含 micrometer-registry-prometheus 依赖
2. THE bootstrap/pom.xml SHALL 包含 spring-boot-starter-actuator 依赖
3. THE application.yml SHALL 配置 management.endpoints.web.exposure.include 包含 "prometheus"
4. THE application.yml SHALL 配置 management.metrics.export.prometheus.enabled 设置为 true
5. WHEN 应用启动后访问 "/actuator/prometheus" 端点时，THE System SHALL 返回 Prometheus 格式的指标数据，包含 JVM 指标、HTTP 请求指标和数据库连接池指标
