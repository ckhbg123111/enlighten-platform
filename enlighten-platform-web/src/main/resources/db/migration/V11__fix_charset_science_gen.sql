-- 统一 science_gen_record 表字符集到 utf8mb4，避免中文/emoji 写入失败

ALTER TABLE `science_gen_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


