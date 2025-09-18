-- 模板表
CREATE TABLE `typesetting_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) NOT NULL COMMENT '模板名称',
  `hospital` varchar(255) DEFAULT NULL COMMENT '所属医院',
  `department` varchar(255) DEFAULT NULL COMMENT '所属科室',
  `tag` varchar(255) DEFAULT NULL COMMENT '标签（字段预留，暂时不用）',
  `sort` int DEFAULT NULL COMMENT '顺序',
  `header` text COMMENT '模板头',
  `footer` text COMMENT '模板脚',
  `text` text COMMENT '正文模板',
  `image` text COMMENT '图片样式',
  `single_title` text COMMENT '单行标题样式',
  `double_title` text COMMENT '多行标题样式',
  `text_card` text COMMENT '文本框样式',
  `block_card` text COMMENT '图文框样式',
  `numbered_title` text COMMENT '副标题样式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除字段：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_hospital_department` (`hospital`, `department`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板表';

-- 素材表
CREATE TABLE `typesetting_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) NOT NULL COMMENT '素材名称',
  `hospital` varchar(255) DEFAULT NULL COMMENT '所属医院',
  `department` varchar(255) DEFAULT NULL COMMENT '所属科室',
  `sort` int DEFAULT NULL COMMENT '顺序',
  `type` varchar(255) DEFAULT NULL COMMENT '素材类型',
  `content` text COMMENT '素材内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除字段：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type_hospital_department` (`type`, `hospital`, `department`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材表';
