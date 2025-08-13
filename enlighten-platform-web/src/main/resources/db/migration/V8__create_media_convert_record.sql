USE `enlighten_platform`;

CREATE TABLE IF NOT EXISTS `media_convert_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
    `essay_code` varchar(128) NOT NULL COMMENT '生成文章编码',
    `content` longtext NOT NULL COMMENT '原文内容',
	`platform` varchar(64) NOT NULL COMMENT '媒体平台',
	`resp_code` int(11) DEFAULT NULL COMMENT '上游返回code',
	`resp_msg` varchar(1024) DEFAULT NULL COMMENT '上游返回msg',
	`resp_success` tinyint(1) DEFAULT NULL COMMENT '上游返回success',
	`resp_data` longtext DEFAULT NULL COMMENT '上游返回data(JSON)',
    `success` tinyint(1) DEFAULT NULL COMMENT '是否成功',
    `error_message` varchar(1024) DEFAULT NULL COMMENT '错误信息',
    `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_essay_time` (`user_id`, `essay_code`, `create_time`),
    KEY `idx_user_deleted_time` (`user_id`, `deleted`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体图文转换记录';


