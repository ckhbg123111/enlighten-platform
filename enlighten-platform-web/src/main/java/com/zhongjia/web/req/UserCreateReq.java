package com.zhongjia.web.req;

import lombok.Data;


/**
 * 创建用户请求类
 */
@Data
public class UserCreateReq {
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private Integer status = 1; // 默认启用

    /**
     * 角色：ADMIN/USER
     */
    private String role;

    /**
     * 租户ID（可选）
     */
    private Long tenantId;
}
