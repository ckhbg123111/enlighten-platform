USE `enlighten_platform`;

-- 为内容库查询优化添加必要索引

-- 1) 提升根据 user_id + code + deleted 查询/连接的效率
ALTER TABLE `media_convert_record`
  ADD INDEX `idx_user_code_deleted` (`user_id`, `code`, `deleted`);

-- 2) 覆盖 user_id + deleted (+ 可选 tag) 的过滤条件
ALTER TABLE `draft_media_map`
  ADD INDEX `idx_user_deleted_tag` (`user_id`, `deleted`, `tag`);


