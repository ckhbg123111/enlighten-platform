-- 添加医院和科室字段到用户表（Beta）
-- V20__add_hospital_department_to_user.sql

ALTER TABLE `user` 
ADD COLUMN `hospital` VARCHAR(100) NULL COMMENT '医院名称（Beta）' AFTER `tenant_id`,
ADD COLUMN `department` VARCHAR(100) NULL COMMENT '科室名称（Beta）' AFTER `hospital`;
