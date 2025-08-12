USE `enlighten_platform`;

-- 兼容 MySQL 5.7/8.0：按需添加列与索引

-- 添加列：tenant_id（若不存在）
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'tenant_id'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `tenant_id` BIGINT NULL DEFAULT NULL COMMENT ''租户ID''' ,
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加索引：idx_tenant_id（若不存在）
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND INDEX_NAME = 'idx_tenant_id'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `user` ADD INDEX `idx_tenant_id` (`tenant_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

