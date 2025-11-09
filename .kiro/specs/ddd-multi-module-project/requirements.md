# 需求文档

## 引言

本文档定义了 OrderCore 系统的功能和非功能性需求。OrderCore 是一个基于 Spring Cloud 的多模块 Maven 工程，采用领域驱动设计（DDD）思想，旨在构建清晰的层次划分、技术与业务解耦、可扩展可演进的系统架构。

## 术语表

- **OrderCore**：本项目的系统名称，一个基于 DDD 架构的多模块 Maven 工程
- **DDD**：领域驱动设计（Domain-Driven Design），一种软件设计方法论
- **Maven 模块**：Maven 项目中的独立构建单元，具有自己的 POM 文件
- **父 POM**：Maven 多模块项目的根 POM 文件，用于统一管理依赖版本和构建配置
- **BOM**：Bill of Materials，Maven 依赖管理机制，用于统一管理一组相关依赖的版本
- **接口层**：DDD 架构中负责处理外部请求的层，包括 HTTP API 和消息队列消费者
- **应用层**：DDD 架构中负责业务用例编排的层，协调领域层完成业务逻辑
- **领域层**：DDD 架构中封装核心业务规则和领域模型的层
- **基础设施层**：DDD 架构中提供技术实现的层，如数据库访问、缓存、消息队列等
- **通用模块**：提供项目通用工具类、异常定义、常量等的共享模块
- **启动模块**：系统启动入口模块，负责加载配置和启动 Spring 上下文
- **Trace ID**：分布式链路追踪标识符，用于跟踪请求在系统中的完整调用链路
- **Span ID**：调用链片段标识符，表示链路追踪中的一个操作单元
- **Profile**：Spring 环境配置标识，用于区分不同的运行环境（如 dev、test、prod）
- **Actuator**：Spring Boot 提供的监控和管理端点
- **Prometheus**：开源的监控和告警系统，用于收集和存储时间序列数据

## 需求

### 需求 1：项目基础结构

**用户故事：** 作为开发人员，我希望创建一个符合 DDD 架构的 Maven 多模块项目结构，以便清晰地组织代码和管理依赖。

#### 验收标准

1. THE OrderCore SHALL 创建一个父 POM 文件，其 groupId 为 "com.catface"，artifactId 为 "order-core-parent"
2. THE OrderCore SHALL 确保所有子模块的 groupId 为 "com.catface"
3. THE OrderCore SHALL 在父 POM 中通过 dependencyManagement 统一管理 Spring Boot 3.3.x 和 Spring Cloud 2024.0.x（Leyton）的版本
4. THE OrderCore SHALL 在父 POM 中配置 Java 21 作为编译和运行环境
5. THE OrderCore SHALL 创建以下顶层模块目录：common、bootstrap、interface、application、domain、infrastructure
6. THE OrderCore SHALL 确保所有子模块的 name 标签使用首字母大写的英文单词，单词之间用空格分隔
7. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译父 POM

### 需求 2：通用模块

**用户故事：** 作为开发人员，我希望有一个通用模块来提供项目级别的工具类、异常定义和常量，以便在各个模块中复用。

#### 验收标准

1. THE OrderCore SHALL 创建 common 模块，其 packaging 类型为 jar
2. THE OrderCore SHALL 在 common 模块中定义异常体系，包括基础异常类、业务异常类和系统异常类
3. THE OrderCore SHALL 在 common 模块中定义统一响应类 Result，包含 code、message、data 字段
4. THE OrderCore SHALL 在 common 模块中创建以下包结构：com.catface.common.exception、com.catface.common.dto、com.catface.common.constant、com.catface.common.util
5. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 common 模块

### 需求 3：接口层模块

**用户故事：** 作为开发人员，我希望创建接口层模块来处理外部请求，以便将外部交互与业务逻辑分离。

#### 验收标准

1. THE OrderCore SHALL 创建 interface 父模块，其 packaging 类型为 pom
2. THE OrderCore SHALL 在 interface 模块下创建 http 子模块，用于处理 HTTP 请求
3. THE OrderCore SHALL 在 interface 模块下创建 consumer 子模块，用于处理消息队列消费
4. THE OrderCore SHALL 在 http 模块中添加 Spring Web 和 Spring Validation 依赖
5. THE OrderCore SHALL 在 http 模块中实现全局异常处理器，使用 @RestControllerAdvice 注解
6. THE OrderCore SHALL 在 consumer 模块中实现全局异常处理器，使用 @ControllerAdvice 注解
7. THE OrderCore SHALL 确保 http 和 consumer 模块依赖 application-api 和 common 模块
8. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 interface 模块及其子模块
9. WHEN 应用启动后发送导致 BusinessException 的 HTTP 请求时 THEN THE OrderCore SHALL 返回 HTTP 400 状态码和包含 code、message、timestamp 的 JSON 响应

### 需求 4：应用层模块

**用户故事：** 作为开发人员，我希望创建应用层模块来编排业务用例，以便协调领域层完成业务逻辑。

#### 验收标准

1. THE OrderCore SHALL 创建 application 父模块，其 packaging 类型为 pom
2. THE OrderCore SHALL 在 application 模块下创建 application-api 子模块，用于定义应用服务接口
3. THE OrderCore SHALL 在 application 模块下创建 application-impl 子模块，用于实现应用服务
4. THE OrderCore SHALL 确保 application-api 模块仅依赖 common 模块
5. THE OrderCore SHALL 确保 application-impl 模块依赖 application-api、domain-api 和 common 模块
6. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 application 模块及其子模块

### 需求 5：领域层模块

**用户故事：** 作为开发人员，我希望创建领域层模块来封装核心业务规则，以便实现业务逻辑与技术实现的解耦。

#### 验收标准

1. THE OrderCore SHALL 创建 domain 父模块，其 packaging 类型为 pom
2. THE OrderCore SHALL 在 domain 模块下创建 domain-api 子模块，用于定义领域模型和仓储接口
3. THE OrderCore SHALL 在 domain 模块下创建 domain-impl 子模块，用于实现领域服务逻辑
4. THE OrderCore SHALL 确保 domain-api 模块仅依赖 common 模块
5. THE OrderCore SHALL 确保 domain-impl 模块依赖 domain-api、repository-api、cache-api、mq-api 和 common 模块
6. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 domain 模块及其子模块

### 需求 6：基础设施层模块

**用户故事：** 作为开发人员，我希望创建基础设施层模块来提供技术实现，以便支持数据持久化、缓存和消息队列等功能。

#### 验收标准

1. THE OrderCore SHALL 创建 infrastructure 父模块，其 packaging 类型为 pom
2. THE OrderCore SHALL 在 infrastructure 模块下创建 repository 父模块，包含 repository-api 和 mysql-impl 子模块
3. THE OrderCore SHALL 在 infrastructure 模块下创建 cache 父模块，包含 cache-api 和 redis-impl 子模块
4. THE OrderCore SHALL 在 infrastructure 模块下创建 mq 父模块，包含 mq-api 和 sqs-impl 子模块
5. THE OrderCore SHALL 在 mysql-impl 模块中添加 MyBatis-Plus 3.5.x 依赖
6. THE OrderCore SHALL 在 redis-impl 模块中添加 Spring Boot Starter Data Redis 依赖（使用 Lettuce 客户端）
7. THE OrderCore SHALL 在 sqs-impl 模块中添加 AWS SDK for SQS 依赖
8. THE OrderCore SHALL 确保所有 *-api 模块仅依赖 common 模块
9. THE OrderCore SHALL 确保所有 *-impl 模块依赖对应的 *-api 模块和 common 模块
10. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 infrastructure 模块及其所有子模块

### 需求 7：启动模块

**用户故事：** 作为开发人员，我希望创建启动模块作为应用的入口点，以便加载配置和启动 Spring 上下文。

#### 验收标准

1. THE OrderCore SHALL 创建 bootstrap 模块，其 packaging 类型为 jar
2. THE OrderCore SHALL 在 bootstrap 模块中添加 Spring Boot Starter Web 依赖
3. THE OrderCore SHALL 在 bootstrap 模块中依赖 http、consumer、application-impl、domain-impl、mysql-impl、redis-impl、sqs-impl 和 common 模块
4. THE OrderCore SHALL 在 bootstrap 模块的 com.catface.bootstrap 包下创建 Spring Boot 主启动类，使用 @SpringBootApplication 注解
5. THE OrderCore SHALL 配置 bootstrap 模块使用 Spring Boot Maven Plugin 打包为可执行 JAR
6. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译 bootstrap 模块
7. WHEN 执行 mvn clean package 命令时 THEN THE OrderCore SHALL 在 bootstrap/target 目录下生成可执行 JAR 文件
8. WHEN 执行 java -jar bootstrap/target/*.jar 命令时 THEN THE OrderCore SHALL 成功启动应用并监听 8080 端口

### 需求 8：日志与链路追踪

**用户故事：** 作为运维人员，我希望系统能够输出结构化日志并支持分布式链路追踪，以便快速定位和排查问题。

#### 验收标准

1. THE OrderCore SHALL 在父 POM 中添加 Micrometer Tracing 依赖用于链路追踪
2. THE OrderCore SHALL 在父 POM 中添加 logstash-logback-encoder 依赖用于 JSON 日志输出
3. THE OrderCore SHALL 在 bootstrap 模块的 resources 目录下创建 logback-spring.xml 配置文件
4. WHEN 应用使用 --spring.profiles.active=local 参数启动时 THEN THE OrderCore SHALL 输出彩色格式日志到控制台
5. WHEN 应用使用 --spring.profiles.active=local 参数启动并访问任意端点时 THEN THE OrderCore SHALL 在控制台输出包含 com.catface 包的 DEBUG 级别日志
6. WHEN 应用使用 --spring.profiles.active=dev 参数启动时 THEN THE OrderCore SHALL 在 logs 目录下创建 application.log 文件
7. WHEN 应用使用 --spring.profiles.active=dev 参数启动并访问任意端点时 THEN THE OrderCore SHALL 在 application.log 文件中输出 JSON 格式日志，包含 timestamp、level、thread、logger、traceId、spanId、message 字段
8. WHEN 应用使用 --spring.profiles.active=prod 参数启动并访问任意端点时 THEN THE OrderCore SHALL 在 application.log 文件中输出 JSON 格式日志，com.catface 包使用 INFO 级别
9. WHEN 应用运行过程中产生 ERROR 级别日志时 THEN THE OrderCore SHALL 在 logs 目录下创建 error.log 文件并记录错误日志
10. WHEN 应用使用 --spring.profiles.active=prod 参数启动时 THEN THE OrderCore SHALL 在 logback-spring.xml 中配置 AsyncAppender

### 需求 9：监控与指标

**用户故事：** 作为运维人员，我希望系统能够暴露 Prometheus 格式的监控指标，以便监控应用的运行状态和性能。

#### 验收标准

1. THE OrderCore SHALL 在 bootstrap 模块中添加 Spring Boot Actuator 依赖
2. THE OrderCore SHALL 在 bootstrap 模块中添加 Micrometer Registry Prometheus 依赖
3. THE OrderCore SHALL 在 application.yml 中配置暴露 /actuator/prometheus 端点
4. WHEN 应用启动后访问 http://localhost:8080/actuator/prometheus 端点时 THEN THE OrderCore SHALL 返回 Prometheus 格式的指标数据（Content-Type: text/plain）
5. WHEN 访问 /actuator/prometheus 端点时 THEN THE OrderCore SHALL 返回包含 JVM 指标的数据，包括 jvm_memory、jvm_gc、jvm_threads 等指标名称
6. WHEN 访问 /actuator/prometheus 端点时 THEN THE OrderCore SHALL 返回包含 HTTP 请求指标的数据，包括 http_server_requests 等指标名称

### 需求 10：多环境配置

**用户故事：** 作为开发人员，我希望系统支持多环境配置，以便在不同环境中使用不同的配置参数。

#### 验收标准

1. THE OrderCore SHALL 在 bootstrap 模块的 resources 目录下创建 application.yml 作为通用配置
2. THE OrderCore SHALL 在 bootstrap 模块的 resources 目录下创建 application-local.yml 作为本地开发环境配置
3. THE OrderCore SHALL 在 bootstrap 模块的 resources 目录下创建 application-dev.yml、application-test.yml、application-staging.yml、application-prod.yml 作为环境特定配置
4. THE OrderCore SHALL 在 bootstrap 模块的 resources 目录下创建 bootstrap.yml 作为引导配置
5. THE OrderCore SHALL 在 application.yml 中配置应用名称为 "order-core"
6. THE OrderCore SHALL 在 application.yml 中配置服务器端口为 8080
7. THE OrderCore SHALL 在 application.yml 中配置默认激活 dev 环境
8. WHEN 执行 java -jar bootstrap/target/*.jar --spring.profiles.active=prod 命令时 THEN THE OrderCore SHALL 使用 prod 环境配置启动
9. WHEN 设置环境变量 SPRING_PROFILES_ACTIVE=test 并执行 java -jar bootstrap/target/*.jar 命令时 THEN THE OrderCore SHALL 使用 test 环境配置启动

### 需求 11：异常处理

**用户故事：** 作为开发人员，我希望系统有统一的异常处理机制，以便规范化错误传播和响应格式。

#### 验收标准

1. THE OrderCore SHALL 在 common 模块中定义 BaseException 抽象类，包含 errorCode 和 message 字段
2. THE OrderCore SHALL 在 common 模块中定义 BusinessException 类，继承自 BaseException
3. THE OrderCore SHALL 在 common 模块中定义 SystemException 类，继承自 BaseException
4. THE OrderCore SHALL 在 http 模块中存在使用 @RestControllerAdvice 注解的全局异常处理器类
5. THE OrderCore SHALL 在 consumer 模块中存在使用 @ControllerAdvice 注解的全局异常处理器类
6. WHEN 应用启动后发送导致 BusinessException 的 HTTP 请求时 THEN THE OrderCore SHALL 返回 HTTP 400 状态码和包含 code、message、timestamp 的 JSON 响应
7. WHEN 应用启动后发送导致 SystemException 的 HTTP 请求时 THEN THE OrderCore SHALL 返回 HTTP 500 状态码和包含 code、message、timestamp 的 JSON 响应
8. WHEN 应用启动后发送导致未处理异常的 HTTP 请求时 THEN THE OrderCore SHALL 返回 HTTP 500 状态码和包含 code、message、timestamp 的 JSON 响应
9. THE OrderCore SHALL 确保异常响应的 message 字段不包含 Java 堆栈信息

### 需求 12：依赖管理

**用户故事：** 作为开发人员，我希望项目有统一的依赖版本管理，以便避免版本冲突和确保依赖一致性。

#### 验收标准

1. THE OrderCore SHALL 在父 POM 的 dependencyManagement 中导入 spring-boot-dependencies BOM
2. THE OrderCore SHALL 在父 POM 的 dependencyManagement 中导入 spring-cloud-dependencies BOM
3. THE OrderCore SHALL 在父 POM 的 properties 中定义 MyBatis-Plus、AWS SDK for SQS 等第三方库的版本
4. THE OrderCore SHALL 确保 common 模块在声明 Lombok 依赖时不指定 version 标签
5. THE OrderCore SHALL 确保 mysql-impl 模块在声明 MyBatis-Plus 依赖时不指定 version 标签
6. WHEN 执行 mvn dependency:tree -Dverbose 命令时 THEN THE OrderCore SHALL 在输出中显示依赖版本来源为父 POM 的 dependencyManagement

### 需求 13：构建和打包

**用户故事：** 作为开发人员，我希望项目能够正确构建和打包，以便部署到不同环境。

#### 验收标准

1. WHEN 执行 mvn clean compile 命令时 THEN THE OrderCore SHALL 成功编译所有模块，输出 "BUILD SUCCESS"
2. WHEN 执行 mvn clean package 命令时 THEN THE OrderCore SHALL 成功打包所有模块，输出 "BUILD SUCCESS"
3. WHEN 执行 mvn clean package 命令后 THEN THE OrderCore SHALL 在 bootstrap/target 目录下生成名称匹配 order-core-bootstrap-*.jar 的可执行 JAR 文件
4. WHEN 执行 java -jar bootstrap/target/order-core-bootstrap-*.jar 命令时 THEN THE OrderCore SHALL 成功启动应用，日志输出包含 "Started" 和 "Tomcat started on port"
5. WHEN 应用启动后访问 http://localhost:8080/actuator/health 端点时 THEN THE OrderCore SHALL 返回 HTTP 200 状态码和 {"status":"UP"} 响应

### 需求 14：模块依赖关系

**用户故事：** 作为架构师，我希望模块之间的依赖关系清晰且单向，以便维护系统的层次结构和可维护性。

#### 验收标准

1. THE OrderCore SHALL 确保 common 模块不依赖任何其他业务模块
2. THE OrderCore SHALL 确保所有 *-api 模块仅依赖 common 模块
3. THE OrderCore SHALL 确保 interface 层模块仅依赖 application-api 和 common 模块
4. THE OrderCore SHALL 确保 application-impl 模块仅依赖 application-api、domain-api 和 common 模块
5. THE OrderCore SHALL 确保 domain-impl 模块仅依赖 domain-api、repository-api、cache-api、mq-api 和 common 模块
6. THE OrderCore SHALL 确保基础设施层的 *-impl 模块仅依赖对应的 *-api 模块和 common 模块
7. THE OrderCore SHALL 确保不存在循环依赖
8. WHEN 执行 mvn dependency:tree 命令时 THEN THE OrderCore SHALL 显示符合 DDD 分层架构的依赖关系

### 需求 15：配置文件规范

**用户故事：** 作为开发人员，我希望项目的配置文件格式统一规范，以便提高可读性和可维护性。

#### 验收标准

1. THE OrderCore SHALL 确保所有 Spring 配置文件使用 YAML 格式（.yml 扩展名）
2. THE OrderCore SHALL 确保所有 POM 文件使用一致的缩进（2 个空格或 4 个空格）
3. WHEN 执行 mvn validate 命令时 THEN THE OrderCore SHALL 验证通过，无格式错误
