---
inclusion: always
---

# 任务执行阶段最佳实践

## 核心原则：保持项目持续可编译

每个任务完成后，必须确保整个项目可以成功编译通过。这是持续集成的基本要求，确保项目始终处于健康状态。

## 模块声明的渐进式管理策略

### 问题场景

在多模块 Maven 项目中，如果在父 POM 中预先声明了所有子模块，但这些模块尚未创建，会导致编译失败：

```
[ERROR] Child module /path/to/module does not exist
```

### 解决方案：渐进式模块声明

#### 1. 只声明已创建的模块
- 在父 POM 的 `<modules>` 节中，只包含已经实际创建的模块
- 不要预先声明计划中但尚未创建的模块

#### 2. 创建模块时同步更新父 POM
- 每创建一个新模块后，立即在父 POM 中添加该模块的声明
- 确保模块声明与实际目录结构保持同步

#### 3. 多层级模块的处理
- 对于有子模块的父模块（如 infrastructure），同样遵循此原则
- 父模块的 `<modules>` 节也应该只声明已创建的子模块

## 实施步骤

### 1. 确认当前状态
检查父 POM 中只包含已创建的模块

### 2. 创建新模块
创建模块目录和 pom.xml 文件

### 3. 更新父 POM
在父 POM 的 modules 节中添加新模块声明

### 4. 验证编译
运行 `mvn clean compile` 确保构建成功

## 验证标准

每个任务完成后，必须执行以下验证：

```bash
# 1. 清理并编译整个项目
mvn clean compile

# 2. 确认构建成功
# 输出应包含：BUILD SUCCESS
# 不应有任何 ERROR 信息

# 3. 检查所有模块都被正确构建
# Reactor Build Order 应列出所有已声明的模块
```

## 示例：infrastructure 模块创建

### 任务：创建 infrastructure 基础设施层父模块

1. 创建 infrastructure/pom.xml
2. 暂时不声明子模块（repository、cache、mq 尚未创建）
3. 在根 pom.xml 中添加 infrastructure 模块声明
4. 运行 `mvn clean compile` 验证成功

### infrastructure/pom.xml 初始内容

```xml
<project>
    <parent>
        <groupId>com.catface.com</groupId>
        <artifactId>order-service</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>infrastructure</artifactId>
    <packaging>pom</packaging>
    
    <!-- 暂时不声明子模块 -->
    <!-- 等 repository、cache、mq 创建后再添加 -->
</project>
```

### 后续创建 repository 模块时

```xml
<project>
    <artifactId>infrastructure</artifactId>
    <packaging>pom</packaging>
    
    <modules>
        <module>repository</module>  <!-- 新增 -->
    </modules>
</project>
```

## 关键收益

- ✅ 项目始终处于可编译状态
- ✅ 便于持续集成和自动化测试
- ✅ 及时发现配置错误和依赖问题
- ✅ 支持增量开发和迭代交付
- ✅ 降低后期集成风险

## 注意事项

1. 不要一次性声明所有计划中的模块
2. 每次修改 POM 后都要验证编译
3. 保持模块声明顺序与依赖关系一致
4. 对于父模块，先创建父 POM，再逐步添加子模块
5. 遇到编译错误时，优先检查模块声明和依赖配置

## 验收原则

任务完成后的验收应遵循以下优先级顺序：

### 1. 运行时验证（最优先）
能通过实际运行应用来验证的功能，必须通过运行应用进行验证。

**适用场景**：
- 多环境配置验证
- Prometheus 指标端点验证
- 日志输出格式验证
- 异常处理验证
- API 接口功能验证
- 数据库连接验证

**验证方法**：
```bash
# 启动应用
mvn spring-boot:run

# 或者运行打包后的 JAR
java -jar bootstrap/target/order-service-bootstrap-1.0.0-SNAPSHOT.jar

# 访问相关端点验证功能
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8080/actuator/health
```

### 2. 编译验证（次优先）
无法通过运行验证的结构性要求，通过编译项目进行验证。

**适用场景**：
- 模块结构验证
- 依赖关系验证
- POM 配置验证
- 代码语法正确性验证

**验证方法**：
```bash
# 清理并编译
mvn clean compile

# 打包验证
mvn clean package

# 检查构建日志
# 确认 Reactor Build Order 正确
# 确认没有编译错误或警告
```

### 3. 静态检查（最后）
仅在无法通过上述两种方式验证时，才使用静态文件检查。

**适用场景**：
- 文件存在性检查
- 配置文件内容检查
- 目录结构检查

## 任务完成检查清单

- [ ] 代码已提交到正确的位置
- [ ] 相关 POM 文件已更新
- [ ] 项目可以成功编译（mvn clean compile）
- [ ] 没有编译错误或警告
- [ ] 模块声明与实际目录结构一致
- [ ] 依赖关系配置正确
- [ ] **如果功能可运行验证，已通过运行应用进行验证**
- [ ] **如果是结构性变更，已通过编译验证**
