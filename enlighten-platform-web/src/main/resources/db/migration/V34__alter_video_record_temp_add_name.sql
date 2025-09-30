USE `enlighten_platform`;

-- 为视频记录临时表添加名称字段（程序生成："视频" + 递增数字，按用户维度）
ALTER TABLE `video_record_temp`
    ADD COLUMN `name` VARCHAR(100) NULL DEFAULT NULL COMMENT '视频名称（程序生成：视频+数字）' AFTER `task_id`;


