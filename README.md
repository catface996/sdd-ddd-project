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

### 任务执行验证命令
```bash
mvn clean compile
```
