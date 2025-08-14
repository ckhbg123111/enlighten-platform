-- 确保关键用户相关表字符集为 utf8mb4

ALTER TABLE `user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `user_article_config` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


