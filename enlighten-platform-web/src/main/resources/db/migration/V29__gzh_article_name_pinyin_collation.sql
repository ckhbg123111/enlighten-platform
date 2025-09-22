-- 改造 gzh_article：name 使用拼音排序（utf8mb4_zh_0900_ai_ci），移除 name_pinyin 及其索引
-- 适用于 MySQL 8.0+（ICU/CLDR 中文排序规则）

-- 将 name 列修改为使用中文拼音排序规则
ALTER TABLE `gzh_article`
  MODIFY COLUMN `name` VARCHAR(255) NOT NULL COLLATE utf8mb4_zh_0900_ai_ci;

-- 删除基于拼音的冗余列与索引
DROP INDEX `idx_name_pinyin` ON `gzh_article`;
ALTER TABLE `gzh_article` DROP COLUMN `name_pinyin`;


