# 实施任务列表

## 任务概述

本任务列表基于需求文档（27 个需求）和设计文档，将项目实施分解为可独立执行、可验证的具体任务。

## 任务执行原则

1. **渐进式开发**：按照依赖关系顺序执行任务，每完成一个任务都要确保项目可以成功构建
2. **持续验证**：每个任务完成后立即验证，确保符合验收标准
3. **验证优先级**：运行时验证 > 构建验证 > 静态检查

## 任务列表

### 阶段一：项目基础结构搭建

- [x] 1. 创建父 POM 和项目根结构
  - 创建项目根目录和父 POM 文件
  - 配置项目基本信息（groupId: com.catface, artifactId: order-service, version: 1.0.0-SNAPSHOT）
  - 配置 Java 21 和 Maven 编译插件
  - 配置 Spring Boot 和 Spring Cloud BOM
  - 在 dependencyManagement 中声明 MyBatis-Plus 3.5.7、Druid 1.2.20、Micrometer Tracing 1.3.5、Logstash Logback Encoder 7.4、AWS SDK 2.20.0 版本
  - **验收标准**：
    - 父 POM 文件存在，groupId 为 com.catface，artifactId 为 order-service，version 为 1.0.0-SNAPSHOT
    - dependencyManagement 中包含所有指定版本的依赖声明
    - properties 中定义了 java.version=21
    - Maven 编译插件配置正确
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，无错误
  - _需求：1.1, 1.2, 1.5_

- [x] 2. 创建 common 通用模块
  - 在项目根目录创建 common 模块
  - 配置 common 模块的 pom.xml（继承父 POM，打包类型为 jar）
  - 添加 Lombok 依赖
  - 创建包结构：exception、dto、constant、util
  - 在父 POM 的 modules 中声明 common 模块
  - **验收标准**：
    - common 模块的 pom.xml 存在，packaging 为 jar，继承父 POM
    - 父 POM 的 modules 中包含 common 模块声明
    - 包结构存在：com.catface.orderservice.common.exception、dto、constant、util
    - Lombok 依赖已添加
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，common 模块编译成功
  - _需求：1.3, 20.2_

- [x] 3. 创建 common 模块的异常体系和响应类
  - 在 exception 包中创建 BaseException 抽象类（包含 errorCode 和 message 字段）
  - 创建 BusinessException 类（继承 BaseException）
  - 创建 SystemException 类（继承 BaseException）
  - 在 dto 包中创建 Result 类（包含 code、message、data、timestamp 字段）
  - **验收标准**：
    - BaseException 类存在，包含 errorCode 和 message 字段，使用 Lombok 注解
    - BusinessException 和 SystemException 类存在，继承 BaseException
    - Result 类存在，包含 code、message、data（泛型）、timestamp 字段
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功
  - _需求：6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 4. 创建 domain 领域层模块结构
  - 创建 domain 目录和 domain-api、domain-impl 两个子模块
  - 配置 domain-api 模块（依赖 common，打包类型为 jar）
  - 配置 domain-impl 模块（依赖 domain-api 和 common）
  - 创建 domain-api 的包结构：entity、vo、repository
  - 在父 POM 的 modules 中声明 domain-api 和 domain-impl 模块
  - **验收标准**：
    - domain-api 模块的 pom.xml 存在，packaging 为 jar，依赖 common
    - domain-impl 模块的 pom.xml 存在，依赖 domain-api 和 common
    - 父 POM 的 modules 中包含 domain-api 和 domain-impl 声明
    - domain-api 包结构存在：entity、vo、repository
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，domain-api 和 domain-impl 模块编译成功
  - _需求：4.1, 4.2, 4.3, 4.4, 4.5, 20.3_

- [x] 5. 创建 infrastructure 基础设施层模块结构
  - 创建 infrastructure 目录
  - 创建 repository 目录，包含 repository-api 和 mysql-impl 两个子模块
  - 创建 cache 目录，包含 cache-api 和 redis-impl 两个子模块
  - 创建 mq 目录，包含 mq-api 和 sqs-impl 两个子模块
  - 配置所有 API 模块（依赖 common，打包类型为 jar）
  - 在父 POM 的 modules 中声明所有基础设施层模块
  - **验收标准**：
    - repository-api、cache-api、mq-api 模块的 pom.xml 存在，packaging 为 jar，依赖 common
    - mysql-impl、redis-impl、sqs-impl 模块的 pom.xml 存在
    - 父 POM 的 modules 中包含所有 6 个基础设施层模块声明
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，所有基础设施层模块编译成功
  - _需求：5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 6. 配置 mysql-impl 模块的 MyBatis-Plus 依赖
  - 在 mysql-impl 模块的 pom.xml 中添加 repository-api 和 common 依赖
  - 添加 mybatis-plus-spring-boot3-starter 依赖（不指定版本）
  - 添加 druid-spring-boot-starter 依赖（不指定版本）
  - 添加 mysql-connector-j 依赖（scope 为 runtime）
  - 创建 mysql-impl 的包结构：mapper、config
  - **验收标准**：
    - mysql-impl 的 pom.xml 中包含 repository-api、common、mybatis-plus-spring-boot3-starter、druid-spring-boot-starter、mysql-connector-j 依赖
    - 所有依赖都不指定版本号（除 mysql-connector-j 由 Spring Boot BOM 管理）
    - mysql-connector-j 的 scope 为 runtime
    - 包结构存在：mapper、config
    - 无依赖冲突
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 和 `mvn dependency:tree` 命令成功，mysql-impl 模块编译成功且无依赖冲突
  - _需求：5.6, 14.1, 14.2, 14.3, 14.4, 14.5, 20.5_

- [x] 7. 配置 redis-impl 和 sqs-impl 模块依赖
  - 在 redis-impl 模块中添加 cache-api、common 和 spring-boot-starter-data-redis 依赖
  - 在 sqs-impl 模块中添加 mq-api、common 和 aws-java-sdk-sqs 依赖
  - **验收标准**：
    - redis-impl 的 pom.xml 中包含 cache-api、common、spring-boot-starter-data-redis 依赖
    - sqs-impl 的 pom.xml 中包含 mq-api、common、aws-java-sdk-sqs 依赖
    - 所有依赖都不指定版本号（由父 POM 或 BOM 管理）
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功
  - _需求：5.7, 5.8_

- [x] 8. 创建 application 应用层模块结构
  - 创建 application 目录和 application-api、application-impl 两个子模块
  - 配置 application-api 模块（依赖 common，打包类型为 jar）
  - 配置 application-impl 模块（依赖 application-api、domain-api 和 common）
  - 创建 application-api 的包结构：service、dto、command
  - 在父 POM 的 modules 中声明 application-api 和 application-impl 模块
  - **验收标准**：
    - application-api 模块的 pom.xml 存在，packaging 为 jar，依赖 common
    - application-impl 模块的 pom.xml 存在，依赖 application-api、domain-api 和 common
    - 父 POM 的 modules 中包含 application-api 和 application-impl 声明
    - application-api 包结构存在：service、dto、command
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，application-api 和 application-impl 模块编译成功
  - _需求：3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 9. 更新 domain-impl 模块依赖
  - 在 domain-impl 模块中添加 repository-api、cache-api、mq-api 依赖
  - **验收标准**：
    - domain-impl 的 pom.xml 中包含 domain-api、repository-api、cache-api、mq-api、common 依赖
    - 所有依赖都不指定版本号
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功
  - _需求：4.3_

- [x] 10. 创建 interface 接口层模块结构
  - 创建 interface 目录和 http、consumer 两个子模块
  - 配置 http 模块（依赖 application-api、common 和 spring-boot-starter-web）
  - 配置 consumer 模块（依赖 application-api 和 common）
  - 创建 http 模块的包结构：controller、handler
  - 在父 POM 的 modules 中声明 http 和 consumer 模块
  - **验收标准**：
    - http 模块的 pom.xml 存在，依赖 application-api、common 和 spring-boot-starter-web
    - consumer 模块的 pom.xml 存在，依赖 application-api 和 common
    - 父 POM 的 modules 中包含 http 和 consumer 声明
    - http 模块包结构存在：controller、handler
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，http 和 consumer 模块编译成功
  - _需求：2.1, 2.2, 2.3, 2.4, 2.5, 20.4_

- [x] 11. 创建 bootstrap 启动模块
  - 创建 bootstrap 模块
  - 配置 bootstrap 模块的 pom.xml（依赖 http、consumer、application-impl、domain-impl、mysql-impl、redis-impl、sqs-impl、common）
  - 添加 spring-boot-starter-web 依赖
  - 配置 spring-boot-maven-plugin（打包类型为 jar）
  - 创建 Spring Boot 主启动类 OrderServiceApplication
  - 在父 POM 的 modules 中声明 bootstrap 模块
  - **验收标准**：
    - bootstrap/target 目录生成 order-service-1.0.0-SNAPSHOT.jar 文件
    - bootstrap 的 pom.xml 中包含所有必需的依赖（http、consumer、application-impl、domain-impl、mysql-impl、redis-impl、sqs-impl、common、spring-boot-starter-web）
    - pom.xml 中配置了 spring-boot-maven-plugin
    - OrderServiceApplication 类存在，包含 @SpringBootApplication 注解和 main 方法
    - 父 POM 的 modules 中包含 bootstrap 声明
  - **验证方式**：构建验证 - 执行 `mvn clean package` 命令成功
  - _需求：7.1, 7.2, 7.3, 7.4, 7.5_


- [x] 12. 创建缓存和消息队列接口定义
  - 在 cache-api 模块中创建 CacheService 接口（定义 get、set、delete、exists 方法）
  - 在 mq-api 模块中创建 MessageProducer 接口（定义 send 方法）
  - 在 mq-api 模块中创建 MessageConsumer 接口（定义 consume 方法）
  - 在 redis-impl 模块中创建 CacheService 的空实现类（标注 @Component 注解）
  - 在 sqs-impl 模块中创建 MessageProducer 和 MessageConsumer 的空实现类（标注 @Component 注解）
  - **验收标准**：
    - CacheService 接口存在，定义了 get、set、delete、exists 方法
    - MessageProducer 接口存在，定义了 send 方法
    - MessageConsumer 接口存在，定义了 consume 方法
    - redis-impl 中存在 CacheService 的实现类，标注 @Component 注解
    - sqs-impl 中存在 MessageProducer 和 MessageConsumer 的实现类，标注 @Component 注解
  - **验证方式**：构建验证 - 执行 `mvn clean compile` 命令成功，所有接口和实现类编译成功
  - _需求：19.1, 19.2, 19.3, 19.4, 19.5_


### 阶段二：可观测性基础设施集成

- [x] 13. 集成分布式链路追踪
  - 在 bootstrap 模块中添加 micrometer-tracing-bridge-brave 依赖
  - 在 bootstrap 模块中添加 micrometer-tracing-reporter-wavefront 依赖（可选）
  - **验收标准**：
    - 创建测试 Controller，发送 HTTP 请求
    - 日志中包含 traceId 和 spanId 字段
    - HTTP 响应头中包含 X-B3-TraceId 字段
    - traceId 在同一请求的所有日志中保持一致
  - **验证方式**：运行时验证 - 启动应用，访问测试端点，检查日志和响应头
  - _需求：8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 14. 配置结构化日志
  - 在 bootstrap/src/main/resources 目录创建 logback-spring.xml 配置文件
  - 配置 JSON 格式日志输出（使用 logstash-logback-encoder）
  - 配置日志字段：timestamp、level、thread、logger、traceId、spanId、message、exception
  - 配置 local profile 使用控制台输出（默认格式）
  - 配置 dev/test/staging/prod profile 使用文件输出（JSON 格式）
  - **验收标准**：
    - logback-spring.xml 文件存在于 bootstrap/src/main/resources 目录
    - dev profile 日志文件为 JSON 格式，包含所有必需字段
    - local profile 控制台输出默认格式日志
  - **验证方式**：运行时验证 - 分别以 dev 和 local profile 启动应用，检查日志格式
  - _需求：9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 15. 配置多环境日志级别
  - 在 logback-spring.xml 中使用 springProfile 标签区分不同环境
  - 配置 local/dev/test/staging profile：com.catface 包为 DEBUG，其他包为 INFO
  - 配置 prod profile：所有包为 INFO，使用异步日志输出（AsyncAppender）
  - **验收标准**：
    - local profile 启动时，com.catface 包日志级别为 DEBUG
    - prod profile 启动时，所有包日志级别为 INFO
    - prod profile 使用 AsyncAppender 异步输出日志
  - **验证方式**：运行时验证 - 分别以 local 和 prod profile 启动应用，创建测试类输出 DEBUG 日志，验证日志级别
  - _需求：11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 16. 集成 Prometheus 监控
  - 在 bootstrap 模块中添加 spring-boot-starter-actuator 依赖
  - 在 bootstrap 模块中添加 micrometer-registry-prometheus 依赖
  - 创建 application.yml 配置文件
  - 配置 management.endpoints.web.exposure.include 包含 prometheus、health、info、metrics
  - 配置 management.metrics.export.prometheus.enabled 为 true
  - **验收标准**：
    - /actuator/prometheus 端点返回 200 状态码
    - 响应内容为 Prometheus 格式的指标数据（text/plain 格式）
    - 指标数据包含 JVM 指标（如 jvm_memory_used_bytes）和 HTTP 请求指标（如 http_server_requests_seconds）
  - **验证方式**：运行时验证 - 启动应用，访问 /actuator/prometheus 端点，检查响应内容
  - _需求：13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 17. 配置健康检查端点
  - 在 application.yml 中配置 management.endpoint.health.show-details 为 always
  - **验收标准**：
    - /actuator/health 端点返回 200 状态码
    - 响应为 JSON 格式，包含 status 字段，值为 UP
    - 响应包含详细的健康检查信息（如 diskSpace、ping 等组件状态）
  - **验证方式**：运行时验证 - 启动应用，访问 /actuator/health 端点，检查响应内容
  - _需求：17.1, 17.2, 17.3, 17.4, 17.5_

- [x] 18. 配置应用信息端点
  - 在 application.yml 中配置 info.app.name 为 "Order Service"
  - 配置 info.app.version 为 "1.0.0-SNAPSHOT"
  - 配置 info.app.description 为 "Order management service based on DDD architecture"
  - **验收标准**：
    - /actuator/info 端点返回 200 状态码
    - 响应为 JSON 格式，包含 app.name、app.version、app.description 字段
    - app.name 值为 "Order Service"，app.version 值为 "1.0.0-SNAPSHOT"
  - **验证方式**：运行时验证 - 启动应用，访问 /actuator/info 端点，检查响应内容
  - _需求：18.1, 18.2, 18.3, 18.4, 18.5_


### 阶段三：多环境配置和异常处理

- [x] 19. 创建多环境配置文件
  - 在 bootstrap/src/main/resources 目录创建 application-local.yml
  - 创建 application-dev.yml
  - 创建 application-test.yml
  - 创建 application-staging.yml
  - 创建 application-prod.yml
  - 在 application.yml 中配置 spring.profiles.active 默认为 local
  - **验收标准**：
    - 所有 5 个环境配置文件存在于 bootstrap/src/main/resources 目录
    - application.yml 中 spring.profiles.active 配置为 local
    - dev profile 启动时日志显示加载了 application-dev.yml
    - prod profile 启动时日志显示加载了 application-prod.yml
  - **验证方式**：运行时验证 - 分别以 dev 和 prod profile 启动应用，检查启动日志中的配置文件加载信息
  - _需求：10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 20. 实现全局异常处理器
  - 在 http 模块的 handler 包中创建 GlobalExceptionHandler 类（使用 @RestControllerAdvice 注解）
  - 实现 BusinessException 处理方法（返回 Result 对象，HTTP 状态码 200）
  - 实现 SystemException 处理方法（返回 Result 对象，HTTP 状态码 500）
  - 实现 Exception 处理方法（返回通用错误消息，HTTP 状态码 500，不暴露内部错误详情）
  - 实现 MethodArgumentNotValidException 处理方法（返回 Result 对象，HTTP 状态码 400）
  - **验收标准**：
    - GlobalExceptionHandler 类存在，标注 @RestControllerAdvice 注解
    - 抛出 BusinessException 返回 HTTP 状态码 200 和 Result 对象
    - 抛出 SystemException 返回 HTTP 状态码 500 和 Result 对象
    - 抛出未知异常返回 HTTP 状态码 500 和通用错误消息
    - 抛出 MethodArgumentNotValidException 返回 HTTP 状态码 400 和验证错误信息
    - 所有异常响应都不包含堆栈信息
  - **验证方式**：运行时验证 - 启动应用，创建测试 Controller 抛出不同类型的异常，检查响应状态码和内容
  - _需求：12.1, 12.2, 12.3, 12.4, 12.5_


### 阶段四：安全和性能配置

- [x] 21. 配置生产环境端点安全
  - 在 application-prod.yml 中配置 management.endpoints.web.exposure.include 只包含 health、info、prometheus
  - 在 application-prod.yml 中配置 management.endpoint.env.enabled 为 false
  - 在 application-prod.yml 中配置 management.endpoint.beans.enabled 为 false
  - 在 application-prod.yml 中配置 management.endpoint.configprops.enabled 为 false
  - **验收标准**：
    - /actuator/health、/actuator/info、/actuator/prometheus 端点返回 200 状态码
    - /actuator/env、/actuator/beans 端点返回 404 状态码
  - **验证方式**：运行时验证 - 以 prod profile 启动应用，访问各个端点，检查响应状态码
  - _需求：23.1, 23.5_

- [x] 22. 添加 HTTP 安全响应头
  - 在 http 模块中创建 SecurityHeadersFilter 过滤器
  - 在过滤器中添加 X-Content-Type-Options: nosniff 响应头
  - 添加 X-Frame-Options: DENY 响应头
  - 添加 X-XSS-Protection: 1; mode=block 响应头
  - **验收标准**：
    - SecurityHeadersFilter 类存在，实现 Filter 接口或继承 OncePerRequestFilter
    - 响应头包含 X-Content-Type-Options: nosniff
    - 响应头包含 X-Frame-Options: DENY
    - 响应头包含 X-XSS-Protection: 1; mode=block
  - **验证方式**：运行时验证 - 启动应用，创建测试 Controller，发送 HTTP 请求，检查响应头
  - _需求：23.4_

- [x] 23. 配置优雅关闭
  - 在 application.yml 中配置 server.shutdown 为 graceful
  - 配置 spring.lifecycle.timeout-per-shutdown-phase 为 30s
  - **验收标准**：
    - application.yml 中 server.shutdown 配置为 graceful
    - spring.lifecycle.timeout-per-shutdown-phase 配置为 30s
    - 应用等待请求完成后才关闭，日志显示 "Waiting for active requests to complete"
  - **验证方式**：运行时验证 - 启动应用，创建延迟 5 秒响应的测试端点，发送请求后立即发送 SIGTERM 信号，检查应用行为
  - _需求：24.4_

- [x] 24. 配置外部化和安全性
  - 在 application.yml 中使用环境变量占位符配置数据库连接（${SPRING_DATASOURCE_URL}、${SPRING_DATASOURCE_USERNAME}、${SPRING_DATASOURCE_PASSWORD}）
  - 配置默认值（使用 :- 语法）
  - **验收标准**：
    - application.yml 中数据库配置使用环境变量占位符
    - 启动日志中数据库 URL 显示正确
    - 密码在日志中显示为 ****** 或不显示
    - 日志输出当前激活的 profile 和配置文件加载顺序
  - **验证方式**：运行时验证 - 设置环境变量后启动应用，检查启动日志
  - _需求：26.1, 26.2, 26.3, 26.4, 26.5_


### 阶段五：验证和优化

- [x] 25. 验证依赖版本统一管理
  - 执行 `mvn dependency:tree` 命令
  - 检查所有子模块的依赖版本是否由父 POM 管理
  - 确认没有子模块直接指定版本号
  - **验收标准**：
    - 所有第三方库依赖版本一致
    - 所有子模块的 pom.xml 依赖声明中不包含 version 标签（Spring Boot BOM 管理的除外）
    - 父 POM 的 dependencyManagement 中包含所有第三方库的版本声明
    - 无版本冲突警告
  - **验证方式**：构建验证 - 执行 `mvn dependency:tree` 和 `mvn clean package` 命令成功，检查输出和 pom.xml 文件
  - _需求：15.1, 15.2, 15.3, 15.4, 15.5_

- [x] 26. 验证模块依赖关系
  - 检查 interface 层模块只依赖 application-api 和 common
  - 检查 application-impl 只依赖 application-api、domain-api 和 common
  - 检查 domain-impl 只依赖 domain-api、repository-api、cache-api、mq-api 和 common
  - 检查 infrastructure 层实现模块只依赖对应的 API 模块和 common
  - **验收标准**：
    - http 和 consumer 模块只依赖 application-api 和 common
    - application-impl 只依赖 application-api、domain-api 和 common
    - domain-impl 只依赖 domain-api、repository-api、cache-api、mq-api 和 common
    - mysql-impl、redis-impl、sqs-impl 只依赖对应的 API 模块和 common
    - 依赖关系符合 DDD 分层架构原则
  - **验证方式**：构建验证 + 静态检查 - 执行 `mvn clean compile` 命令成功且无循环依赖错误，检查各模块的 pom.xml 文件
  - _需求：16.1, 16.2, 16.3, 16.4, 16.5_

- [x] 27. 验证构建和打包
  - 执行 `mvn clean package` 命令
  - 检查 bootstrap/target 目录是否生成 order-service-1.0.0-SNAPSHOT.jar 文件
  - 检查 JAR 文件的 MANIFEST.MF 是否包含 Main-Class、Start-Class、Implementation-Version
  - 执行 `java -jar bootstrap/target/order-service-1.0.0-SNAPSHOT.jar` 启动应用
  - 检查应用是否在 30 秒内启动成功
  - **验收标准**：
    - bootstrap/target 目录存在 order-service-1.0.0-SNAPSHOT.jar 文件
    - JAR 文件大小合理（包含所有依赖，通常 > 50MB）
    - META-INF/MANIFEST.MF 包含 Main-Class、Start-Class、Implementation-Version
    - 应用在 30 秒内启动成功，日志输出 "Started OrderServiceApplication"
    - 日志显示启动耗时信息
  - **验证方式**：运行时验证 - 执行 `mvn clean package` 和 `java -jar` 命令，检查构建产物和应用启动情况
  - _需求：27.1, 27.2, 27.3, 27.4, 27.5, 21.1, 21.3_

- [x] 28. 端到端功能验证
  - 启动应用（使用 local profile）
  - 访问 /actuator/health 端点，验证健康检查
  - 访问 /actuator/info 端点，验证应用信息
  - 访问 /actuator/prometheus 端点，验证监控指标
  - 创建测试 Controller，发送 HTTP 请求，验证链路追踪（检查日志中的 traceId 和响应头中的 X-B3-TraceId）
  - 触发异常，验证全局异常处理器
  - 检查日志格式（local profile 为默认格式，dev profile 为 JSON 格式）
  - **验收标准**：
    - /actuator/health 返回 200，status 为 UP
    - /actuator/info 返回 200，包含应用名称和版本
    - /actuator/prometheus 返回 200，包含 Prometheus 格式指标
    - 测试 Controller 返回 200
    - 日志包含 traceId 和 spanId，响应头包含 X-B3-TraceId
    - 触发 BusinessException 返回 200 和 Result 对象
    - local profile 控制台日志为默认格式，dev profile 文件日志为 JSON 格式
    - 包结构符合规范（所有包名以 com.catface.orderservice 开头）
  - **验证方式**：运行时验证 - 以 local 和 dev profile 启动应用，访问各个端点，创建测试 Controller，检查响应和日志
  - _需求：综合验证，20.1_

