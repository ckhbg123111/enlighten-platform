USE `enlighten_platform`;

CREATE TABLE IF NOT EXISTS `draft` (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
	`user_id` bigint(20) NOT NULL COMMENT '用户ID',
	`tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
	`essay_code` varchar(128) NOT NULL COMMENT '生成文章编码',
	`title` varchar(255) NOT NULL COMMENT '草稿标题',
	`content` longtext NOT NULL COMMENT '草稿内容',
	`deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`delete_time` datetime DEFAULT NULL COMMENT '删除时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_user_essay_code` (`user_id`,`essay_code`),
	KEY `idx_user_deleted_time` (`user_id`, `deleted`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='草稿表';



