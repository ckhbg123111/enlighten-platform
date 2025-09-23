-- V31: 1) 增加原文与生成内容字段；2) 将 (user_id, external_id, platform, deleted) 唯一索引改为普通索引

USE `enlighten_platform`;

-- 1. 增加列 original_text、generated_text 为 LONGTEXT（幂等）
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'media_convert_record_v2' AND COLUMN_NAME = 'original_text'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `media_convert_record_v2` ADD COLUMN `original_text` LONGTEXT NULL COMMENT ''原文内容'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col2_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'media_convert_record_v2' AND COLUMN_NAME = 'generated_text'
);
SET @ddl2 := IF(@col2_exists = 0,
  'ALTER TABLE `media_convert_record_v2` ADD COLUMN `generated_text` LONGTEXT NULL COMMENT ''生成内容'' AFTER `original_text`',
  'SELECT 1'
);
PREPARE stmt2 FROM @ddl2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- 2. 将唯一索引 uk_user_ext_platform_deleted 改为普通索引（若存在则先删除再添加普通索引）
SET @has_uk := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_convert_record_v2'
    AND INDEX_NAME = 'uk_user_ext_platform_deleted'
);

SET @drop_sql := IF(@has_uk > 0,
  'ALTER TABLE `media_convert_record_v2` DROP INDEX `uk_user_ext_platform_deleted`',
  'SELECT 1'
);
PREPARE stmt3 FROM @drop_sql; EXECUTE stmt3; DEALLOCATE PREPARE stmt3;

-- 添加同名普通索引，避免下游依赖变更（幂等）
SET @has_idx := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_convert_record_v2'
    AND INDEX_NAME = 'uk_user_ext_platform_deleted'
);
SET @add_idx := IF(@has_idx = 0,
  'ALTER TABLE `media_convert_record_v2` ADD INDEX `uk_user_ext_platform_deleted` (`user_id`,`external_id`,`platform`,`deleted`)',
  'SELECT 1'
);
PREPARE stmt4 FROM @add_idx; EXECUTE stmt4; DEALLOCATE PREPARE stmt4;


