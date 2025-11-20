# Spec 开发最佳实践

本文档提供 Spec 开发流程的概览和最佳实践指引。

## 通用最佳实践

所有阶段都应遵守的通用原则和最佳实践，包括渐进式开发、持续验证、质量保证、风险管理等。

详见：[00-general-best-practices.md](.kiro/steering/00-general-best-practices.md)

## 核心原则

**切勿一次性完成所有阶段** - LLM 是概率模型，需要通过多次自检和双向验证来确保质量。

## 开发流程

Spec 开发遵循以下四个阶段，每个阶段都有详细的最佳实践指导：

### 1. 需求分析阶段（Requirements）

准备原始需求文档，进行需求完整性验证，确保需求与原始意图一致。

详见：[01-requirements-best-practices.md](.kiro/steering/01-requirements-best-practices.md)

### 2. 方案设计阶段（Design）

基于需求进行方案设计，验证设计与需求的一致性，避免过度设计。

详见：[02-design-best-practices.md](.kiro/steering/02-design-best-practices.md)

### 3. 任务拆分阶段（Tasks Planning）

将设计转化为可执行的任务列表，确保任务的可执行性和可验证性。

详见：[03-tasks-planning-best-practices.md](.kiro/steering/03-tasks-planning-best-practices.md)

### 4. 任务执行阶段（Tasks Execution）

执行具体任务，保持项目持续可编译，采用渐进式模块声明策略。

详见：[04-tasks-execution-best-practices.md](.kiro/steering/04-tasks-execution-best-practices.md)

## 关键收益

- ✅ 降低需求理解偏差
- ✅ 及早发现设计缺陷
- ✅ 确保任务可执行性
- ✅ 提高最终交付质量
- ✅ 减少返工成本
- ✅ 项目始终处于可编译状态
- ✅ 支持增量开发和迭代交付

## 快速参考

### 需求阶段验证提示词
> "请检查需求是否与原始意图一致，任何需要澄清的需求点，请务必与我确认，确保需求足够完备，足够准确。"

### 设计阶段验证提示词
> "请再次检查设计文档与需求文档是一致的，并且设计自身的内容内部没有冲突，且能完全并准确的覆盖需求。"

> "请检查设计中，是否有需求未提及的内容，属于过度设计的内容？如果有，请与我确认，是否补充和完善需求。"

### 任务拆分验证提示词
> "请检查任务列表是否与需求和设计一致，是否有超出设计范围的任务。另外，请明确每个任务的验证标准，并对任务进行验证。"

### 任务执行验证提示词
> "请确保每个任务完成后，项目可以成功编译。对于多模块项目，采用渐进式模块声明策略，只声明已创建的模块。"

### 任务执行验证命令
```bash
mvn clean compile
```

# 为什么要按领域切分 Spec

在 Spec Driven Development 中，按 DDD 领域切分 Spec 文档（需求、设计、任务）带来诸多优势：

## 核心优势

### 1. 上下文隔离 🎯
- **开发时只使用对应领域的文档**：在 Domain 层开发时，只需关注 Domain 的需求、设计和 steering
- **降低认知负担**：不需要理解整个系统，只需理解当前领域
- **减少干扰**：避免被其他领域的细节分散注意力

### 2. Steering 差异化 📋
- **命名规则不同**：Domain 层使用业务术语，Infrastructure 层使用技术术语
- **编码规则不同**：Domain 层强调纯粹性，Infrastructure 层关注性能
- **最佳实践不同**：每个领域有自己的最佳实践指南

### 3. 技术栈差异化 🛠️
- **Domain 层**：纯 Java，不依赖框架
- **Infrastructure 层**：MyBatis-Plus、Redis、AWS SDK
- **Traffic 层**：Spring MVC、Validation
- **Application 层**：事务管理、编排逻辑

### 4. 验证标准差异化 ✅
- **Domain 层**：完全可 mock，纯单元测试，快速反馈
- **Infrastructure 层**：必须与依赖的中间件联调通过（数据库、缓存、MQ）
- **Application 层**：服务测试，mock 基础设施
- **Traffic 层**：端到端测试，完整流程验证

## Spec Driven Development 的额外优势

### 5. 需求粒度控制 📝
- 每个领域的需求可以独立演进
- 避免一个巨大的需求文档难以维护
- 可以针对不同领域设置不同的优先级
- 需求变更的影响范围更明确

### 6. 设计复杂度管理 🏗️
- 每个领域的设计文档聚焦于该领域的关注点
- 避免设计文档过于庞大和复杂
- 降低设计评审的认知负担
- 更容易发现设计问题

### 7. 任务并行开发 ⚡
- 不同领域的任务可以并行开发
- 减少团队成员之间的冲突
- 提高开发效率
- 支持多人协作

### 8. 依赖关系清晰 🔗
- 每个领域的依赖关系独立管理
- 更容易识别循环依赖
- 便于进行依赖分析和优化
- 符合 DDD 的依赖倒置原则

### 9. 测试策略差异化 🧪
- **Domain 层**：纯单元测试，无需启动容器，秒级反馈
- **Infrastructure 层**：集成测试，需要 TestContainers，分钟级反馈
- **Application 层**：服务测试，mock 基础设施，秒级反馈
- **Traffic 层**：端到端测试，完整流程验证，分钟级反馈

### 10. 文档可读性提升 📖
- 每个领域的文档更聚焦、更易读
- 新成员可以快速理解特定领域
- 减少文档维护的心智负担
- 便于知识传递

### 11. 变更影响范围可控 🎛️
- 修改某个领域的需求/设计，影响范围明确
- 更容易进行影响分析
- 降低变更风险
- 支持增量交付

### 12. 技能匹配优化 👥
- 可以根据团队成员的技能分配领域
- 前端开发者关注 Traffic/HTTP
- 后端开发者关注 Domain/Infrastructure
- DBA 关注 Repository
- 架构师关注整体依赖关系

### 13. 验收流程优化 ✔️
- 每个领域可以独立验收
- 验收标准更明确、更聚焦
- 可以分阶段交付
- 降低验收复杂度

### 14. 重用性提升 ♻️
- Domain 层的设计可以在多个项目中重用
- Infrastructure 层的实现可以替换（如从 MySQL 切换到 PostgreSQL）
- 更容易提取通用模块
- 支持模块化演进

### 15. 监控和度量差异化 📊
- **Infrastructure 层**：关注连接池、响应时间、错误率
- **Domain 层**：关注业务规则执行次数、业务指标
- **Traffic 层**：关注请求量、QPS、延迟分布
- **Application 层**：关注事务成功率、用例执行时间

### 16. 安全策略差异化 🔒
- **Traffic 层**：输入验证、认证授权、防 SQL 注入
- **Domain 层**：业务规则校验、权限检查
- **Infrastructure 层**：数据加密、访问控制、审计日志

### 17. 性能优化策略差异化 🚀
- **Domain 层**：算法优化、业务逻辑优化
- **Infrastructure 层**：缓存策略、数据库优化、连接池调优
- **Traffic 层**：限流、熔断、降级
- **Application 层**：事务优化、批处理

### 18. 错误处理策略差异化 ⚠️
- **Traffic 层**：转换为用户友好的错误信息，统一响应格式
- **Domain 层**：抛出业务异常（BusinessException）
- **Infrastructure 层**：转换技术异常为系统异常（SystemException）
- **Application 层**：编排异常处理流程

### 19. 配置管理差异化 ⚙️
- 每个领域可以有独立的配置文件
- 配置变更的影响范围更小
- 更容易进行配置审计
- 支持领域级别的配置热更新

### 20. 持续集成优化 🔄
- 可以针对不同领域设置不同的 CI 流程
- **Domain 层**：快速单元测试，每次提交都运行
- **Infrastructure 层**：需要启动依赖服务，定期运行
- 可以并行执行不同领域的测试
- 提高 CI/CD 效率

## 总结

按领域切分 Spec 不仅符合 DDD 的分层架构原则，更在 Spec Driven Development 的各个阶段（需求、设计、任务拆分、任务执行）都带来显著优势。这种切分方式：

- ✅ 降低复杂度
- ✅ 提高可维护性
- ✅ 支持并行开发
- ✅ 优化验收流程
- ✅ 提升团队效率
- ✅ 增强系统质量

## 项目文档

### 01 - 客户调研

#### AIDLC 客户调研问卷

了解客户需求和痛点的调研问卷，用于收集项目初期的需求信息。

详见：[AIDLC 客户调研问卷](doc/01-questionnaire/AIDLC客户调研问卷.md)

---

### 02 - 需求与意图

#### 项目蓝图

AIOps 系统的原始需求文档，包含项目概述、核心功能需求、非功能性需求、技术约束等完整内容。

详见：[AIOps 系统原始需求文档（Blueprint）](doc/02-intent/blueprint.md)

#### 功能特性列表

从用户价值角度拆分的功能特性列表，每个特性代表一个可独立交付的最小价值单元。

详见：[功能特性列表（Feature List）](doc/02-intent/feature-list.md)

#### 术语表

项目中使用的核心概念、技术术语和业务术语的定义和说明。

详见：[术语表（Glossary）](doc/02-intent/glossy.md)

#### 初始化后端项目

##### 项目架构设计

详细的项目架构设计说明，包括模块结构、技术栈选型、配置管理等。

详见：[项目架构设计说明书](doc/02-intent/01-init-backend/1-project-architecture-design.md)

##### MyBatis-Plus 集成需求

MyBatis-Plus 集成与 NodeEntity 实现的完整需求说明，包括业务背景、数据模型、功能需求等。

详见：[MyBatis-Plus 集成需求](doc/02-intent/01-init-backend/2-mybatis-plus-integration.md)

#### 功能特性详细文档

每个功能特性的详细说明，包括用户故事、功能价值、核心功能、验收标准等。

- [F01: 创建和管理 IT 资源](doc/02-intent/feature/F01-创建和管理IT资源.md)
- [F02: 建立资源间的拓扑关系](doc/02-intent/feature/F02-建立资源间的拓扑关系.md)
- [F03: 可视化查看拓扑图](doc/02-intent/feature/F03-可视化查看拓扑图.md)
- [F04: 在拓扑图上进行交互操作](doc/02-intent/feature/F04-在拓扑图上进行交互操作.md)
- [F05: 配置和管理 Agent](doc/02-intent/feature/F05-配置和管理Agent.md)
- [F06: 将 Agent 关联到资源节点](doc/02-intent/feature/F06-将Agent关联到资源节点.md)
- [F07: 手动执行 Agent 任务](doc/02-intent/feature/F07-手动执行Agent任务.md)
- [F08: 查看 Agent 执行结果和报告](doc/02-intent/feature/F08-查看Agent执行结果和报告.md)
- [F09: 配置 LLM 服务](doc/02-intent/feature/F09-配置LLM服务.md)
- [F10: 用户登录和身份认证](doc/02-intent/feature/F10-用户登录和身份认证.md)
- [F11: 管理资源的访问权限](doc/02-intent/feature/F11-管理资源的访问权限.md)
- [F12: 通过 Chatbot 查询资源信息](doc/02-intent/feature/F12-通过Chatbot查询资源信息.md)
- [F13: 通过 Chatbot 执行临时任务](doc/02-intent/feature/F13-通过Chatbot执行临时任务.md)
- [F14: 定时自动执行 Agent 任务](doc/02-intent/feature/F14-定时自动执行Agent任务.md)
- [F15: 基于事件触发 Agent 任务](doc/02-intent/feature/F15-基于事件触发Agent任务.md)
- [F16: 自定义报告模板](doc/02-intent/feature/F16-自定义报告模板.md)
- [F17: 管理提示词模板](doc/02-intent/feature/F17-管理提示词模板.md)
- [F18: 集成监控系统数据](doc/02-intent/feature/F18-集成监控系统数据.md)
- [F19: 集成 CMDB 系统数据](doc/02-intent/feature/F19-集成CMDB系统数据.md)
- [F20: 配置告警规则](doc/02-intent/feature/F20-配置告警规则.md)
- [F21: 接收和处理外部告警](doc/02-intent/feature/F21-接收和处理外部告警.md)
- [F22: 配置多种通知渠道](doc/02-intent/feature/F22-配置多种通知渠道.md)
- [F23: 导出拓扑图和报告](doc/02-intent/feature/F23-导出拓扑图和报告.md)
- [F24: 分析资源故障的影响范围](doc/02-intent/feature/F24-分析资源故障的影响范围.md)
- [F25: 追踪故障的根本原因](doc/02-intent/feature/F25-追踪故障的根本原因.md)
- [F26: 预测资源使用趋势](doc/02-intent/feature/F26-预测资源使用趋势.md)
- [F27: 编排多个 Agent 协作](doc/02-intent/feature/F27-编排多个Agent协作.md)
- [F28: 多租户数据隔离](doc/02-intent/feature/F28-多租户数据隔离.md)
- [F29: 移动端访问和操作](doc/02-intent/feature/F29-移动端访问和操作.md)

功能特性目录说明：[Feature 文档目录](doc/02-intent/feature/README.md)

---

### 03 - 分析报告

#### 功能相似度分析报告

四个分支（001-004）的功能相似度分析，包括 Maven 模块结构对比、技术栈对比、实现功能对比等。

详见：[功能相似度分析报告](doc/03-analysis/functional_similarity_report.md)

---

### 04 - 评分标准

#### 设计评分标准

设计文档的评分标准和质量检查清单。

详见：[设计评分标准](doc/04-scoring/design-scoring-criteria.md)

#### 需求评分标准

需求文档的评分标准和质量检查清单。

详见：[需求评分标准](doc/04-scoring/requirements-scoring-criteria.md)

#### 任务规划评分标准

任务拆分和规划的评分标准和质量检查清单。

详见：[任务规划评分标准](doc/04-scoring/tasks-planning-scoring-criteria.md)

---

### 05 - 最佳实践与经验总结

#### 开发洞察

在 GenDev（生成式开发）时代，关于编码效率与可读性平衡的深度思考。

详见：[GenDev 时代的开发洞察](doc/05-best-practice/insights.md)

#### 常见问题与解决方案

在 Spec 开发过程中遇到的实际问题及应对策略，包括需求分析、设计、任务分解等各阶段的经验总结。

详见：[常见问题与解决方案](doc/05-best-practice/problems.md)

---

### 99 - 开发过程截图

展示在 Spec 开发过程中遇到的典型场景和问题的截图，包括 Kiro 的自动化行为和常见问题示例。

详见：[开发过程截图](doc/99-imgs/screenshots.md)

## 畅想 
- 是否可以建立一个评分体系，分别对需求，设计，任务分解评分
- 如果设计这样一个体系，应该从哪些维度设计，例如：规范，业务描述是否清晰？
- 是扣分制，还是加分制？
