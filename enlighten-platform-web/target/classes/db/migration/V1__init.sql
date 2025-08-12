-- 初始化库（若使用 DatabaseInitializer 建库，此处不会重复报错）
CREATE DATABASE IF NOT EXISTS `enlighten_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `enlighten_platform`;

-- 表：user
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化数据（幂等）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`)
SELECT * FROM (
    SELECT 'admin' AS `username`, '123456' AS `password`, 'admin@example.com' AS `email`, '13800138000' AS `phone`, 1 AS `status`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`)
SELECT * FROM (
    SELECT 'test1', '123456', 'test1@example.com', '13800138001', 1
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'test1');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`)
SELECT * FROM (
    SELECT 'test2', '123456', 'test2@example.com', '13800138002', 1
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'test2');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`)
SELECT * FROM (
    SELECT 'disabled_user', '123456', 'disabled@example.com', '13800138003', 0
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'disabled_user');

