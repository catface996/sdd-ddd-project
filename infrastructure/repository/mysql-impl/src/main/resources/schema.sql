-- ============================================================================
-- 系统节点表初始化脚本
-- 
-- 表名: t_node
-- 说明: 用于记录和管理系统中的各类节点（数据库、应用、API 等）及其依赖关系
-- 
-- 创建时间: 2024-11-11
-- ============================================================================

-- 创建 t_node 表（如果不存在）
CREATE TABLE IF NOT EXISTS t_node (
    -- 主键 ID（雪花算法生成）
    id BIGINT NOT NULL COMMENT '主键ID',
    
    -- 节点基本信息
    name VARCHAR(100) NOT NULL COMMENT '节点名称（唯一）',
    type VARCHAR(50) NOT NULL COMMENT '节点类型',
    description VARCHAR(500) NULL COMMENT '节点描述',
    properties TEXT NULL COMMENT 'JSON格式的扩展属性',
    
    -- 审计字段
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) NULL COMMENT '创建人',
    update_by VARCHAR(100) NULL COMMENT '更新人',
    
    -- 逻辑删除和乐观锁
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0:未删除, 1:已删除）',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    -- 主键约束
    PRIMARY KEY (id),
    
    -- 唯一索引：节点名称（既保证唯一性，又提升查询性能）
    UNIQUE KEY uk_name (name),
    
    -- 普通索引：节点类型（提升按类型查询性能）
    KEY idx_type (type),
    
    -- 普通索引：逻辑删除标记（提升逻辑删除查询性能）
    KEY idx_deleted (deleted)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统节点表';

-- ============================================================================
-- 索引说明
-- ============================================================================
-- 1. PRIMARY KEY (id): 主键索引，保证 ID 唯一性
-- 2. UNIQUE KEY uk_name (name): 唯一索引，保证节点名称唯一性，同时提升按名称查询性能
-- 3. KEY idx_type (type): 普通索引，提升按类型查询性能
-- 4. KEY idx_deleted (deleted): 普通索引，提升逻辑删除查询性能
-- ============================================================================
