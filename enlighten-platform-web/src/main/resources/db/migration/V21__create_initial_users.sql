-- 创建初始用户数据
-- V21__create_initial_users.sql

USE `enlighten_platform`;

-- 赣州人民医院用户（12个用户）
-- 妇科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin01' AS `username`, MD5('123456') AS `password`, 'admin_g_f_01@gzrmyy.com' AS `email`, '13800000001' AS `phone`, 1 AS `status`, 'USER' AS `role`, '赣州人民医院' AS `hospital`, '妇科' AS `department`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin01');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin02', MD5('123456'), 'admin_g_f_02@gzrmyy.com', '13800000002', 1, 'USER', '赣州人民医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin02');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin03', MD5('123456'), 'admin_g_f_03@gzrmyy.com', '13800000003', 1, 'USER', '赣州人民医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin03');

-- 宣传科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin04', MD5('123456'), 'admin_g_x_01@gzrmyy.com', '13800000004', 1, 'USER', '赣州人民医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin04');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin05', MD5('123456'), 'admin_g_x_02@gzrmyy.com', '13800000005', 1, 'USER', '赣州人民医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin05');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin06', MD5('123456'), 'admin_g_x_03@gzrmyy.com', '13800000006', 1, 'USER', '赣州人民医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin06');

-- 信息科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin07', MD5('123456'), 'admin_g_i_01@gzrmyy.com', '13800000007', 1, 'USER', '赣州人民医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin07');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin08', MD5('123456'), 'admin_g_i_02@gzrmyy.com', '13800000008', 1, 'USER', '赣州人民医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin08');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin09', MD5('123456'), 'admin_g_i_03@gzrmyy.com', '13800000009', 1, 'USER', '赣州人民医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin09');

-- 肿瘤科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin10', MD5('123456'), 'admin_g_z_01@gzrmyy.com', '13800000010', 1, 'USER', '赣州人民医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin10');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin11', MD5('123456'), 'admin_g_z_02@gzrmyy.com', '13800000011', 1, 'USER', '赣州人民医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin11');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin12', MD5('123456'), 'admin_g_z_03@gzrmyy.com', '13800000012', 1, 'USER', '赣州人民医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin12');

-- 其他医院用户（12个用户）
-- 妇科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin13', MD5('123456'), 'admin_o_f_01@other.com', '13800000013', 1, 'USER', '其他医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin13');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin14', MD5('123456'), 'admin_o_f_02@other.com', '13800000014', 1, 'USER', '其他医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin14');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin15', MD5('123456'), 'admin_o_f_03@other.com', '13800000015', 1, 'USER', '其他医院', '妇科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin15');

-- 宣传科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin16', MD5('123456'), 'admin_o_x_01@other.com', '13800000016', 1, 'USER', '其他医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin16');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin17', MD5('123456'), 'admin_o_x_02@other.com', '13800000017', 1, 'USER', '其他医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin17');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin18', MD5('123456'), 'admin_o_x_03@other.com', '13800000018', 1, 'USER', '其他医院', '宣传科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin18');

-- 信息科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin19', MD5('123456'), 'admin_o_i_01@other.com', '13800000019', 1, 'USER', '其他医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin19');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin20', MD5('123456'), 'admin_o_i_02@other.com', '13800000020', 1, 'USER', '其他医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin20');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin21', MD5('123456'), 'admin_o_i_03@other.com', '13800000021', 1, 'USER', '其他医院', '信息科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin21');

-- 肿瘤科用户（3个）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin22', MD5('123456'), 'admin_o_z_01@other.com', '13800000022', 1, 'USER', '其他医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin22');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin23', MD5('123456'), 'admin_o_z_02@other.com', '13800000023', 1, 'USER', '其他医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin23');

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `role`, `hospital`, `department`)
SELECT * FROM (
    SELECT 'admin24', MD5('123456'), 'admin_o_z_03@other.com', '13800000024', 1, 'USER', '其他医院', '肿瘤科'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin24');
