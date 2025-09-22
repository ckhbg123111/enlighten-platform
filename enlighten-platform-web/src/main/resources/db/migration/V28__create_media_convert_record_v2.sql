USE `enlighten_platform`;

CREATE TABLE IF NOT EXISTS `media_convert_record_v2` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `external_id` bigint(20) NOT NULL COMMENT '关联的外部ID（如 gzh_article.id）',
    `platform` varchar(64) NOT NULL COMMENT '平台：gzh/xiaohongshu/douyin',
    `status` varchar(32) NOT NULL COMMENT '状态：PROCESSING/SUCCESS/FAILED',
    `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_platform_time` (`user_id`, `platform`, `create_time`),
    KEY `idx_user_deleted_time` (`user_id`, `deleted`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体转换记录 v2';


