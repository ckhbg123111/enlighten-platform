package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Accessors(chain = true)
@TableName("user")
public class User {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 角色：ADMIN/USER
     */
    private String role;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 租户ID（ToB 多租户）
     */
    private Long tenantId;

    /**
     * 医院名称（Beta测试版）
     */
    private String hospital;

    /**
     * 科室名称（Beta测试版）
     */
    private String department;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted;
}
