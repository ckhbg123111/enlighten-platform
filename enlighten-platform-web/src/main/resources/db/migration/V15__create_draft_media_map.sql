USE `enlighten_platform`;

CREATE TABLE IF NOT EXISTS `draft_media_map` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
    `draft_id` bigint(20) NOT NULL COMMENT '草稿ID',
    `media_code` varchar(64) NOT NULL COMMENT '媒体素材编码',
    `tag` varchar(255) DEFAULT NULL COMMENT '标签',
    `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_tag` (`user_id`, `tag`),
    KEY `idx_user_draft` (`user_id`, `draft_id`),
    KEY `idx_user_deleted_time` (`user_id`, `deleted`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='草稿与媒体映射表';



