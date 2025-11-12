-- =====================================================
-- MyBatis-Plus 集成 - 节点表结构定义
-- =====================================================
-- 表名：t_node
-- 说明：用于管理系统节点（数据库、业务应用、API 接口、报表系统等）及其依赖关系和属性信息
-- 创建时间：2024-11
-- =====================================================

-- 如果表已存在则删除（开发环境使用，生产环境请谨慎）
DROP TABLE IF EXISTS t_node;

-- 创建节点表
CREATE TABLE IF NOT EXISTS t_node (
    -- 主键 ID（使用 BIGINT 支持雪花算法生成的长整型 ID）
    id BIGINT NOT NULL COMMENT '主键 ID（雪花算法生成）',
    
    -- 业务字段
    name VARCHAR(100) NOT NULL COMMENT '节点名称（唯一）',
    type VARCHAR(50) NOT NULL COMMENT '节点类型（DATABASE、APPLICATION、API、REPORT、OTHER）',
    description VARCHAR(500) NULL COMMENT '节点描述',
    properties TEXT NULL COMMENT '节点属性（JSON 格式字符串）',
    
    -- 审计字段
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) NOT NULL COMMENT '创建人',
    update_by VARCHAR(100) NOT NULL COMMENT '更新人',
    
    -- 逻辑删除和乐观锁字段
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    
    -- 主键约束
    PRIMARY KEY (id),
    
    -- 唯一索引：节点名称唯一
    UNIQUE KEY uk_name (name),
    
    -- 普通索引：提升按类型查询性能
    KEY idx_type (type),
    
    -- 普通索引：提升逻辑删除查询性能
    KEY idx_deleted (deleted)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='节点表';

-- =====================================================
-- 索引说明
-- =====================================================
-- 1. PRIMARY KEY (id)：主键索引，保证 ID 唯一性
-- 2. UNIQUE KEY uk_name (name)：唯一索引，保证节点名称唯一性
-- 3. KEY idx_type (type)：普通索引，提升按类型查询的性能
-- 4. KEY idx_deleted (deleted)：普通索引，提升逻辑删除查询的性能
-- 
-- 总计：1 个主键索引 + 3 个业务索引 = 4 个索引
-- =====================================================

-- =====================================================
-- 字段说明
-- =====================================================
-- 1. id：主键，使用 BIGINT 类型支持雪花算法生成的 64 位长整型 ID
-- 2. name：节点名称，最大 100 字符，唯一约束
-- 3. type：节点类型，最大 50 字符，枚举值（DATABASE、APPLICATION、API、REPORT、OTHER）
-- 4. description：节点描述，最大 500 字符，可选
-- 5. properties：节点属性，TEXT 类型，存储 JSON 格式字符串，可选
-- 6. create_time：创建时间，DATETIME 类型，由 MyBatis-Plus 自动填充
-- 7. update_time：更新时间，DATETIME 类型，由 MyBatis-Plus 自动填充
-- 8. create_by：创建人，最大 100 字符，由应用层设置
-- 9. update_by：更新人，最大 100 字符，由应用层设置
-- 10. deleted：逻辑删除标记，TINYINT 类型，默认 0（未删除），MyBatis-Plus 自动处理
-- 11. version：版本号，INT 类型，默认 0，MyBatis-Plus 乐观锁自动处理
-- =====================================================
