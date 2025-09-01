-- 创建初始用户数据
-- V21__create_initial_users.sql

USE `enlighten_platform`;

-- 赣州人民医院用户（8个用户）
-- 妇科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_F_01' AS `username`, MD5('123456') AS `password`, 'admin_g_f_01@gzrmyy.com' AS `email`, '13800000001' AS `phone`, 1 AS `status`, 'USER' AS `role`, '赣州人民医院' AS `hospital`, '妇科' AS `department`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_F_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_F_02', MD5('123456'), 'admin_g_f_02@gzrmyy.com', '13800000002', 1, 'USER', '赣州人民医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_F_02');

-- 宣传科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_X_01', MD5('123456'), 'admin_g_x_01@gzrmyy.com', '13800000003', 1, 'USER', '赣州人民医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_X_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_X_02', MD5('123456'), 'admin_g_x_02@gzrmyy.com', '13800000004', 1, 'USER', '赣州人民医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_X_02');

-- 信息科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_I_01', MD5('123456'), 'admin_g_i_01@gzrmyy.com', '13800000005', 1, 'USER', '赣州人民医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_I_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_I_02', MD5('123456'), 'admin_g_i_02@gzrmyy.com', '13800000006', 1, 'USER', '赣州人民医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_I_02');

-- 肿瘤科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_Z_01', MD5('123456'), 'admin_g_z_01@gzrmyy.com', '13800000007', 1, 'USER', '赣州人民医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_Z_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_G_Z_02', MD5('123456'), 'admin_g_z_02@gzrmyy.com', '13800000008', 1, 'USER', '赣州人民医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_G_Z_02');

-- 其他医院用户（8个用户）
-- 妇科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_F_01', MD5('123456'), 'admin_o_f_01@other.com', '13800000009', 1, 'USER', '其他医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_F_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_F_02', MD5('123456'), 'admin_o_f_02@other.com', '13800000010', 1, 'USER', '其他医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_F_02');

-- 宣传科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_X_01', MD5('123456'), 'admin_o_x_01@other.com', '13800000011', 1, 'USER', '其他医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_X_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_X_02', MD5('123456'), 'admin_o_x_02@other.com', '13800000012', 1, 'USER', '其他医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_X_02');

-- 信息科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_I_01', MD5('123456'), 'admin_o_i_01@other.com', '13800000013', 1, 'USER', '其他医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_I_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_I_02', MD5('123456'), 'admin_o_i_02@other.com', '13800000014', 1, 'USER', '其他医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_I_02');

-- 肿瘤科用户（2个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_Z_01', MD5('123456'), 'admin_o_z_01@other.com', '13800000015', 1, 'USER', '其他医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_Z_01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin_O_Z_02', MD5('123456'), 'admin_o_z_02@other.com', '13800000016', 1, 'USER', '其他医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin_O_Z_02');
