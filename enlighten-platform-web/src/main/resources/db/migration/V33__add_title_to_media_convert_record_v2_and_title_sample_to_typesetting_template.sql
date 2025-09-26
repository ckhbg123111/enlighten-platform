USE `enlighten_platform`;

-- 为 media_convert_record_v2 表新增字段：title
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'media_convert_record_v2' AND COLUMN_NAME = 'title'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `media_convert_record_v2` ADD COLUMN `title` VARCHAR(255) NULL COMMENT ''标题'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 为 typesetting_template 表新增字段：title_sample
SET @col2_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'typesetting_template' AND COLUMN_NAME = 'title_sample'
);
SET @ddl2 := IF(@col2_exists = 0,
  'ALTER TABLE `typesetting_template` ADD COLUMN `title_sample` VARCHAR(255) NULL COMMENT ''标题样例'' AFTER `sample`',
  'SELECT 1'
);
PREPARE stmt2 FROM @ddl2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;



