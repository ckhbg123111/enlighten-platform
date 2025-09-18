-- 创建排版相关表：模板表和素材表

-- 模板表
CREATE TABLE IF NOT EXISTS `typesetting_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(255) NOT NULL COMMENT '模板名称',
  `hospital` VARCHAR(255) DEFAULT NULL COMMENT '所属医院',
  `department` VARCHAR(255) DEFAULT NULL COMMENT '所属科室',
  `tag` VARCHAR(255) DEFAULT NULL COMMENT '标签（字段预留，暂时不用）',
  `sort` INT DEFAULT NULL COMMENT '顺序',
  `header` TEXT COMMENT '模板头',
  `footer` TEXT COMMENT '模板脚',
  `text` TEXT COMMENT '正文模板',
  `image` TEXT COMMENT '图片样式',
  `single_title` TEXT COMMENT '单行标题样式',
  `double_title` TEXT COMMENT '多行标题样式',
  `text_card` TEXT COMMENT '文本框样式',
  `block_card` TEXT COMMENT '图文框样式',
  `numbered_title` TEXT COMMENT '副标题样式',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除字段：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_hospital_department` (`hospital`, `department`),
  KEY `idx_sort` (`sort`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板表';

-- 素材表
CREATE TABLE IF NOT EXISTS `typesetting_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(255) NOT NULL COMMENT '素材名称',
  `hospital` VARCHAR(255) DEFAULT NULL COMMENT '所属医院',
  `department` VARCHAR(255) DEFAULT NULL COMMENT '所属科室',
  `sort` INT DEFAULT NULL COMMENT '顺序',
  `type` VARCHAR(255) DEFAULT NULL COMMENT '素材类型',
  `content` TEXT COMMENT '素材内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除字段：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type_hospital_department` (`type`, `hospital`, `department`),
  KEY `idx_sort` (`sort`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材表';
