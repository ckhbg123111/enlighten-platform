-- 统一库与表字符集到 utf8mb4，避免中文/emoji 写入失败

-- 修改数据库字符集（需要有相应权限）
ALTER DATABASE `enlighten_platform` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 转换表字符集
ALTER TABLE `fill_in_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


