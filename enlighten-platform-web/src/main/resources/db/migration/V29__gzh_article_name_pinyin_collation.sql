-- 改造 gzh_article：name 使用中文拼音排序（若环境支持 zh_0900），移除 name_pinyin 及其索引
-- 该脚本可在不支持 zh_0900 的环境下安全跳过改列，避免启动失败

-- 1) 动态探测中文 Collation（优先 as_ci，再 as_cs）
SET @coll := (
  SELECT COLLATION_NAME
  FROM INFORMATION_SCHEMA.COLLATIONS
  WHERE COLLATION_NAME IN ('utf8mb4_zh_0900_as_ci','utf8mb4_zh_0900_as_cs')
  LIMIT 1
);

-- 2) 若存在中文 Collation，则修改 gzh_article.name；否则跳过
SET @sql := IF(@coll IS NULL,
  'SELECT 1',
  CONCAT('ALTER TABLE `gzh_article` MODIFY COLUMN `name` VARCHAR(255) NOT NULL COLLATE ', @coll)
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) 如存在 idx_name_pinyin 索引则删除
SET @has_idx := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='gzh_article' AND INDEX_NAME='idx_name_pinyin'
);
SET @sql := IF(@has_idx>0, 'DROP INDEX `idx_name_pinyin` ON `gzh_article`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) 如存在 name_pinyin 列则删除
SET @has_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='gzh_article' AND COLUMN_NAME='name_pinyin'
);
SET @sql := IF(@has_col>0, 'ALTER TABLE `gzh_article` DROP COLUMN `name_pinyin`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


