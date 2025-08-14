USE `enlighten_platform`;

ALTER TABLE `draft`
    ADD COLUMN `tags` varchar(255) DEFAULT NULL COMMENT '标签' AFTER `media_code_list_string`;


