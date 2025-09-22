-- 为 media_convert_record_v2 增加唯一索引，防止并发下重复插入

SET @has_uk := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_convert_record_v2'
    AND INDEX_NAME = 'uk_user_ext_platform_deleted'
);

SET @sql := IF(@has_uk = 0,
  'ALTER TABLE `media_convert_record_v2`\n   ADD UNIQUE KEY `uk_user_ext_platform_deleted` (`user_id`,`external_id`,`platform`,`deleted`)',
  'SELECT 1'
);

PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


