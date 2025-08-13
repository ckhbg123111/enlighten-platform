USE `enlighten_platform`;

-- 为 media_convert_record.code 添加唯一索引（若不存在）
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'media_convert_record'
      AND INDEX_NAME = 'uk_media_convert_record_code'
);

SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `media_convert_record` ADD UNIQUE KEY `uk_media_convert_record_code` (`code`)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


