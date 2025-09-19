-- 为 typesetting_template 表新增字段：封面图、样例
-- 使用 IF NOT EXISTS 保证幂等性（MySQL 8.0+）

ALTER TABLE `typesetting_template`
  ADD COLUMN IF NOT EXISTS `cover_image` TEXT COMMENT '封面图' AFTER `image`;

ALTER TABLE `typesetting_template`
  ADD COLUMN IF NOT EXISTS `sample` TEXT COMMENT '样例' AFTER `cover_image`;


