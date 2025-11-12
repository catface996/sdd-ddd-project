-- =============================================
-- 系统节点表初始化脚本
-- =============================================

-- 创建节点表
CREATE TABLE IF NOT EXISTS t_node
(
    id          BIGINT       NOT NULL COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL COMMENT '节点名称',
    type        VARCHAR(50)  NOT NULL COMMENT '节点类型',
    description VARCHAR(500) COMMENT '节点描述',
    properties  TEXT COMMENT '节点属性JSON',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    create_by   VARCHAR(50)  NOT NULL COMMENT '创建人',
    update_by   VARCHAR(50)  NOT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0: 未删除, 1: 已删除）',
    version     INT          NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_type (type),
    KEY idx_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统节点表';
