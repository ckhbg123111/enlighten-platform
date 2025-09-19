-- 为 typesetting_template 表新增字段：封面图、样例

ALTER TABLE `typesetting_template`
  ADD COLUMN `cover_image` TEXT COMMENT '封面图' AFTER `image`;

ALTER TABLE `typesetting_template`
  ADD COLUMN `sample` TEXT COMMENT '样例' AFTER `cover_image`;


