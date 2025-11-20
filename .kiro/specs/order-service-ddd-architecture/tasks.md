# 任务列表

## 任务概述

本任务列表将订单服务 DDD 架构工程的设计方案拆分为可独立执行、可验证的具体任务。任务按照依赖关系和优先级排序，遵循渐进式开发原则。

---

## 阶段 1：基础工程搭建

- [x] 1. 创建 Maven 父工程
  - 统一管理所有子模块的依赖版本和构建配置
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，父工程构建成功
    - 【静态检查】检查 pom.xml 文件存在，groupId 为 "com.catface.orderservice"，artifactId 为 "order-service-parent"
    - 【静态检查】检查 pom.xml 中包含 Spring Boot BOM（3.4.1）和 Spring Cloud BOM（2025.0.0）的 dependencyManagement 声明
    - 【静态检查】检查 pom.xml 中包含第三方库版本变量（MyBatis-Plus 3.5.7、Druid 1.2.20、AWS SDK for SQS 2.20.0、Micrometer Tracing 1.3.5、Logstash Logback Encoder 7.4）
    - 【静态检查】检查 Maven 编译插件配置使用 JDK 21
    - 【静态检查】检查打包类型为 pom
    - 【静态检查】检查 modules 中声明 6 个顶层模块：common、interface、application、domain、infrastructure、bootstrap
  - _需求: 1, 13_

- [x] 2. 创建通用模块（Common）
  - 提供项目中共享的异常体系、统一响应类、常量和工具类
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，common 模块构建成功
    - 【静态检查】检查 common 模块的 artifactId 为 "order-service-common"，name 为 "Order Service Common"
    - 【静态检查】检查异常体系包含 BaseException（包含 errorCode 和 errorMessage 字段）、BusinessException、SystemException
    - 【静态检查】检查统一响应类 Result 包含 code、message、data、timestamp 字段
    - 【静态检查】检查包结构包含 exception、dto、constant、util 目录
    - 【静态检查】检查打包类型为 jar
  - _需求: 2, 12_

---

## 阶段 2：领域层和应用层模块

- [x] 3. 创建领域层聚合模块和子模块
  - 创建 domain 聚合模块，并创建 domain-api 和 domain-impl 子模块
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，domain 聚合模块及其子模块构建成功
    - 【静态检查】检查 domain 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 domain 聚合模块的 modules 中声明 domain-api 和 domain-impl 子模块
    - 【静态检查】检查 domain-api 模块的 artifactId 为 "order-service-domain-api"，name 为 "Order Service Domain API"
    - 【静态检查】检查 domain-api 模块仅依赖 common 模块
    - 【静态检查】检查 domain-api 模块包结构包含 model、repository、service 目录
    - 【静态检查】检查 domain-api 模块打包类型为 jar
  - _需求: 3, 12_

- [x] 4. 创建领域层实现模块
  - 实现领域服务逻辑
  - **前置条件**：任务 3、7、9、11 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，domain-impl 模块构建成功
    - 【静态检查】检查 domain-impl 模块的 artifactId 为 "order-service-domain-impl"，name 为 "Order Service Domain Implementation"
    - 【静态检查】检查模块依赖 domain-api、repository-api、cache-api、mq-api 和 common 模块
    - 【静态检查】检查包结构包含 service.impl 目录
    - 【静态检查】检查打包类型为 jar
  - _需求: 3, 12_

- [x] 5. 创建应用层聚合模块和 API 模块
  - 创建 application 聚合模块，并创建 application-api 子模块
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，application 聚合模块及 application-api 模块构建成功
    - 【静态检查】检查 application 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 application 聚合模块的 modules 中声明 application-api 和 application-impl 子模块
    - 【静态检查】检查 application-api 模块的 artifactId 为 "order-service-application-api"，name 为 "Order Service Application API"
    - 【静态检查】检查 application-api 模块仅依赖 common 模块
    - 【静态检查】检查 application-api 模块包结构包含 service、dto、command、query 目录
    - 【静态检查】检查 application-api 模块打包类型为 jar
  - _需求: 4, 12_

- [x] 6. 创建应用层实现模块
  - 实现业务用例编排逻辑
  - **前置条件**：任务 3、5 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，application-impl 模块构建成功
    - 【静态检查】检查 application-impl 模块的 artifactId 为 "order-service-application-impl"，name 为 "Order Service Application Implementation"
    - 【静态检查】检查模块依赖 application-api、domain-api 和 common 模块
    - 【静态检查】检查包结构包含 service.impl 目录
    - 【静态检查】检查打包类型为 jar
  - _需求: 4, 12_

---

## 阶段 3：基础设施层模块

- [x] 7. 创建基础设施层和仓储层聚合模块
  - 创建 infrastructure 聚合模块和 repository 聚合模块，并创建 repository-api 子模块
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，infrastructure 和 repository 聚合模块及 repository-api 模块构建成功
    - 【静态检查】检查 infrastructure 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 infrastructure 聚合模块的 modules 中声明 repository、cache、mq 子模块
    - 【静态检查】检查 repository 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 repository 聚合模块的 modules 中声明 repository-api 和 mysql-impl 子模块
    - 【静态检查】检查 repository-api 模块的 artifactId 为 "order-service-repository-api"，name 为 "Order Service Repository API"
    - 【静态检查】检查 repository-api 模块仅依赖 common 模块
    - 【静态检查】检查 repository-api 模块包结构包含 repository 目录
    - 【静态检查】检查 repository-api 模块打包类型为 jar
  - _需求: 5, 12_

- [x] 8. 创建 MySQL 实现模块
  - 基于 MyBatis-Plus 实现 MySQL 数据访问
  - **前置条件**：任务 7 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，mysql-impl 模块构建成功
    - 【静态检查】检查 mysql-impl 模块的 artifactId 为 "order-service-mysql-impl"，name 为 "Order Service MySQL Implementation"
    - 【静态检查】检查模块依赖 repository-api 和 common 模块
    - 【静态检查】检查模块包含 MyBatis-Plus（mybatis-plus-spring-boot3-starter）、Druid 和 MySQL 驱动依赖
    - 【静态检查】检查包结构包含 repository.impl、mapper、entity 目录
    - 【静态检查】检查打包类型为 jar
  - _需求: 5, 12_

- [x] 9. 创建缓存层聚合模块和 API 模块
  - 创建 cache 聚合模块，并创建 cache-api 子模块
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，cache 聚合模块及 cache-api 模块构建成功
    - 【静态检查】检查 cache 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 cache 聚合模块的 modules 中声明 cache-api 和 redis-impl 子模块
    - 【静态检查】检查 cache-api 模块的 artifactId 为 "order-service-cache-api"，name 为 "Order Service Cache API"
    - 【静态检查】检查 cache-api 模块仅依赖 common 模块
    - 【静态检查】检查 cache-api 模块包结构包含 cache 目录
    - 【静态检查】检查 cache-api 模块打包类型为 jar
  - _需求: 5, 12_

- [x] 10. 创建 Redis 实现模块
  - 基于 Spring Data Redis 实现 Redis 缓存
  - **前置条件**：任务 9 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，redis-impl 模块构建成功
    - 【静态检查】检查 redis-impl 模块的 artifactId 为 "order-service-redis-impl"，name 为 "Order Service Redis Implementation"
    - 【静态检查】检查模块依赖 cache-api 和 common 模块
    - 【静态检查】检查模块包含 Spring Data Redis 依赖
    - 【静态检查】检查包结构包含 cache.impl 目录
    - 【静态检查】检查打包类型为 jar
  - _需求: 5, 12_

- [x] 11. 创建消息队列层聚合模块和 API 模块
  - 创建 mq 聚合模块，并创建 mq-api 子模块
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，mq 聚合模块及 mq-api 模块构建成功
    - 【静态检查】检查 mq 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 mq 聚合模块的 modules 中声明 mq-api 和 sqs-impl 子模块
    - 【静态检查】检查 mq-api 模块的 artifactId 为 "order-service-mq-api"，name 为 "Order Service MQ API"
    - 【静态检查】检查 mq-api 模块仅依赖 common 模块
    - 【静态检查】检查 mq-api 模块包结构包含 mq 目录
    - 【静态检查】检查 mq-api 模块打包类型为 jar
  - _需求: 5, 12_

- [x] 12. 创建 SQS 实现模块
  - 基于 AWS SDK for SQS 实现消息队列
  - **前置条件**：任务 11 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，sqs-impl 模块构建成功
    - 【静态检查】检查 sqs-impl 模块的 artifactId 为 "order-service-sqs-impl"，name 为 "Order Service SQS Implementation"
    - 【静态检查】检查模块依赖 mq-api 和 common 模块
    - 【静态检查】检查模块包含 AWS SDK for SQS 依赖
    - 【静态检查】检查包结构包含 mq.impl 目录
    - 【静态检查】检查打包类型为 jar
  - _需求: 5, 12_

---

## 阶段 4：接口层模块

- [x] 13. 创建接口层聚合模块和 HTTP 接口模块
  - 创建 interface 聚合模块，并创建 interface-http 子模块
  - **前置条件**：任务 5 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，interface 聚合模块及 interface-http 模块构建成功
    - 【静态检查】检查 interface 聚合模块的 pom.xml 存在，打包类型为 pom
    - 【静态检查】检查 interface 聚合模块的 modules 中声明 interface-http 和 interface-consumer 子模块
    - 【静态检查】检查 interface-http 模块的 artifactId 为 "order-service-interface-http"，name 为 "Order Service HTTP Interface"
    - 【静态检查】检查 interface-http 模块依赖 application-api 和 common 模块
    - 【静态检查】检查 interface-http 模块包含 Spring Web 依赖
    - 【静态检查】检查 interface-http 模块包结构包含 controller、handler、interceptor 目录
    - 【静态检查】检查全局异常处理器使用 @RestControllerAdvice 注解
    - 【静态检查】检查 interface-http 模块打包类型为 jar
  - _需求: 6, 9, 12_

- [x] 14. 创建消息消费者接口模块
  - 接收和处理消息队列事件，并实现全局异常处理器
  - **前置条件**：任务 5 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，interface-consumer 模块构建成功
    - 【静态检查】检查 interface-consumer 模块的 artifactId 为 "order-service-interface-consumer"，name 为 "Order Service Consumer Interface"
    - 【静态检查】检查模块依赖 application-api 和 common 模块
    - 【静态检查】检查包结构包含 consumer、handler 目录
    - 【静态检查】检查全局异常处理器使用 @ControllerAdvice 注解
    - 【静态检查】检查打包类型为 jar
  - _需求: 6, 9, 12_

---

## 阶段 5：启动模块和配置

- [x] 15. 创建启动模块（Bootstrap）
  - 作为应用的启动入口，整合所有模块
  - **前置条件**：任务 4、6、8、10、12、13、14 已完成
  - **验收标准**：
    - 【构建验证】执行 `mvn clean install` 命令，bootstrap 模块构建成功
    - 【静态检查】检查 bootstrap 模块的 artifactId 为 "order-service-bootstrap"，name 为 "Order Service Bootstrap"
    - 【静态检查】检查模块依赖 interface-http、interface-consumer、application-impl、domain-impl、mysql-impl、redis-impl、sqs-impl 和 common 模块
    - 【静态检查】检查包含 Spring Boot 主启动类，使用 @SpringBootApplication 注解
    - 【静态检查】检查配置 Spring Boot Maven 插件
    - 【静态检查】检查打包类型为 jar
  - _需求: 7, 12_

- [x] 16. 配置多环境支持
  - 在 bootstrap 模块中配置多环境支持，创建通用配置和各环境特定配置文件
  - **前置条件**：任务 15 已完成
  - **验收标准**：
    - 【运行时验证】启动应用（使用默认 profile），检查日志输出显示激活的 profile 为 local
    - 【运行时验证】使用 `--spring.profiles.active=dev` 启动应用，检查日志输出显示激活的 profile 为 dev
    - 【静态检查】检查 resources 目录下存在 application.yml、application-local.yml、application-dev.yml、application-test.yml、application-staging.yml、application-prod.yml
    - 【静态检查】检查 application.yml 中配置应用名称为 "order-service"，默认激活 profile 为 local
    - 【静态检查】检查各环境配置文件包含数据库、Redis、AWS SQS 配置（支持环境变量）
  - _需求: 10_

- [x] 17. 配置日志与链路追踪体系
  - 在 bootstrap 模块中集成 Micrometer Tracing 和 Logstash Logback Encoder，配置多环境日志输出
  - **前置条件**：任务 15 已完成
  - **验收标准**：
    - 【运行时验证】使用 local profile 启动应用，检查控制台输出彩色格式日志，项目包使用 DEBUG 级别，框架包使用 WARN 级别
    - 【运行时验证】使用 dev profile 启动应用，检查 logs/application.log 文件输出 JSON 格式日志，项目包使用 DEBUG 级别，框架包使用 WARN 级别
    - 【运行时验证】使用 staging profile 启动应用，检查日志项目包使用 INFO 级别，框架包使用 WARN 级别
    - 【运行时验证】使用 prod profile 启动应用，检查日志项目包使用 INFO 级别，框架包使用 WARN 级别
    - 【运行时验证】检查日志包含 timestamp、level、thread、logger、traceId、spanId、message 字段
    - 【静态检查】检查 bootstrap 模块包含 Micrometer Tracing 和 Logstash Logback Encoder 依赖
    - 【静态检查】检查 resources 目录下存在 logback-spring.xml 配置文件
    - 【静态检查】检查 logback-spring.xml 配置 local 环境输出到控制台，dev/test/staging/prod 环境输出到文件
    - 【静态检查】检查日志文件按日期滚动，单个文件超过 100MB 时分割
    - 【静态检查】检查非生产环境保留 30 天日志，生产环境保留 90 天日志
  - _需求: 8_

- [x] 18. 集成 Prometheus 监控
  - 在 bootstrap 模块中集成 Spring Boot Actuator 和 Micrometer Registry Prometheus，暴露监控端点
  - **前置条件**：任务 15 已完成
  - **验收标准**：
    - 【运行时验证】启动应用后访问 /actuator/prometheus 端点，检查返回 Prometheus 格式的指标数据
    - 【运行时验证】检查指标数据包含 JVM 指标（内存、GC、线程）
    - 【运行时验证】检查指标数据包含 HTTP 请求指标（QPS、延迟、错误率）
    - 【静态检查】检查 bootstrap 模块包含 Spring Boot Actuator 和 Micrometer Registry Prometheus 依赖
    - 【静态检查】检查 application.yml 配置 Actuator 暴露 prometheus、health、info 端点
    - 【静态检查】检查 Actuator 端点路径为 /actuator
  - _需求: 11_

---

## 任务执行说明

### 执行顺序

任务按照阶段顺序执行，每个阶段内的任务可以并行执行（如果没有前置条件）。

### 验证方法说明

- **【构建验证】**：执行 `mvn clean install` 命令，确保构建成功
- **【运行时验证】**：启动应用，访问端点或检查日志输出
- **【静态检查】**：检查文件存在性和内容

### 关键原则

1. **渐进式开发**：每完成一个任务，项目都应该可以成功构建
2. **独立验证**：每个任务完成后都应该独立验证其正确性
3. **依赖管理**：严格遵循任务的前置条件和依赖关系

---

## 任务统计

- **总任务数**：18
- **阶段 1（基础工程搭建）**：2 个任务
- **阶段 2（领域层和应用层模块）**：4 个任务
- **阶段 3（基础设施层模块）**：6 个任务
- **阶段 4（接口层模块）**：2 个任务
- **阶段 5（启动模块和配置）**：4 个任务

---

## 预计工作量

- **阶段 1**：4-6 小时
- **阶段 2**：6-8 小时
- **阶段 3**：10-12 小时
- **阶段 4**：4-6 小时
- **阶段 5**：6-8 小时

**总计**：30-40 小时
