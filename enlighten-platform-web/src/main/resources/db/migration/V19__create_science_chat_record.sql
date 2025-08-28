-- 创建会话聊天记录表，用于落库用户每次对话请求与AI回复
CREATE TABLE IF NOT EXISTS `science_chat_record` (
  `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       bigint(20) NOT NULL COMMENT '用户ID',
  `session_id`    varchar(36) NOT NULL COMMENT '会话ID（UUID）',
  `req_messages`  longtext     NULL COMMENT '请求携带的 messages 原始JSON',
  `need_recommend` tinyint(1)  NULL COMMENT '是否需要推荐问题',
  `prompt`        text         NULL COMMENT '上游提示词',
  `resp_content`  longtext     NULL COMMENT 'AI回复拼接后的完整文本内容（仅assistant内容）',
  `success`       tinyint(1)   NULL COMMENT '是否成功',
  `error_message` text         NULL COMMENT '失败时的错误信息',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者端对话记录表';


