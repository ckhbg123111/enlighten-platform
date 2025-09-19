USE `enlighten_platform`;

-- ================================
-- folder：文件夹表（硬删除）
-- ================================
CREATE TABLE IF NOT EXISTS `folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(255) NOT NULL COMMENT '文件夹名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_id`, `name`),
  KEY `idx_user_sort` (`user_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件夹';

-- ======================================
-- gzh_article：公众号内容记录表（软删除）
-- ======================================
CREATE TABLE IF NOT EXISTS `gzh_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `folder_id` BIGINT NULL DEFAULT NULL COMMENT '关联的文件夹ID',
  `name` VARCHAR(255) NOT NULL COMMENT '名称',
  `name_pinyin` VARCHAR(255) NULL DEFAULT NULL COMMENT '名称的拼音首字母（用于排序）',
  `tag` VARCHAR(100) NULL DEFAULT NULL COMMENT '标签',
  `cover_image_url` VARCHAR(1024) NULL DEFAULT NULL COMMENT '封面图片URL',
  `original_text` LONGTEXT NULL COMMENT '原文',
  `typeset_content` LONGTEXT NULL COMMENT '排版内容',
  `status` VARCHAR(32) NOT NULL DEFAULT 'INITIAL' COMMENT '状态: INITIAL/EDITING/REVIEWING/APPROVED/REJECTED/PUBLISHED',
  `last_edit_time` DATETIME NULL DEFAULT NULL COMMENT '最后编辑时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `delete_time` DATETIME NULL DEFAULT NULL COMMENT '删除时间（软删除打入回收站时记录）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_deleted` (`user_id`, `deleted`),
  KEY `idx_folder` (`folder_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tag` (`tag`),
  KEY `idx_last_edit_time` (`last_edit_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_name` (`name`),
  KEY `idx_name_pinyin` (`name_pinyin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号-文章记录';

-- ==============================================
-- recycle_bin：回收站（硬删除，支持多文件类型）
-- ==============================================
CREATE TABLE IF NOT EXISTS `recycle_bin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `file_id` BIGINT NOT NULL COMMENT '关联的文件ID',
  `file_type` VARCHAR(32) NOT NULL COMMENT '文件类型: WECHAT_ARTICLE/IMAGE/VIDEO/AUDIO',
  `delete_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '删除时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_delete_time` (`user_id`, `delete_time`),
  KEY `idx_user_type` (`user_id`, `file_type`),
  KEY `idx_user_file` (`user_id`, `file_id`, `file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回收站';


