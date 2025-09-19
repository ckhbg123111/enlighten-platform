USE `enlighten_platform`;

-- 为 folder 表增加软删除字段，并调整唯一约束

-- 添加列 deleted（若不存在）
SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'folder' AND COLUMN_NAME = 'deleted'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `folder` ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除：0-未删除，1-已删除'' AFTER `sort`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加列 delete_time（若不存在）
SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'folder' AND COLUMN_NAME = 'delete_time'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `folder` ADD COLUMN `delete_time` DATETIME NULL DEFAULT NULL COMMENT ''删除时间'' AFTER `deleted`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除原有唯一约束 uk_user_name（若存在）
SET @uk_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'folder' AND INDEX_NAME = 'uk_user_name'
);
SET @ddl := IF(@uk_exists = 1,
    'ALTER TABLE `folder` DROP INDEX `uk_user_name`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 新增唯一索引：用户+名称+未删除 唯一
SET @idx_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'folder' AND INDEX_NAME = 'uk_user_name_not_deleted'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `folder` ADD UNIQUE KEY `uk_user_name_not_deleted` (`user_id`, `name`, `deleted`)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 为查询添加辅助索引（若不存在）
SET @idx_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'folder' AND INDEX_NAME = 'idx_user_deleted_sort'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `folder` ADD INDEX `idx_user_deleted_sort` (`user_id`, `deleted`, `sort`)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


