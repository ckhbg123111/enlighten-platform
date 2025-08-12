USE `enlighten_platform`;

-- 兼容 MySQL 5.7/8.0：通过 information_schema 判断列/索引是否存在，再按需执行 DDL

-- 添加列：role（若不存在）
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'role'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(32) NOT NULL DEFAULT ''USER'' COMMENT ''角色：ADMIN/USER''',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加列：last_login_time（若不存在）
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'last_login_time'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `last_login_time` DATETIME NULL DEFAULT NULL COMMENT ''最后登录时间''',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 为角色列添加索引：idx_role（若不存在）
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND INDEX_NAME = 'idx_role'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `user` ADD INDEX `idx_role` (`role`)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

