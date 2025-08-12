package com.zhongjia.web.req;

import lombok.Data;


/**
 * 更新用户请求类
 */
@Data
public class UserUpdateReq {
    
    private Long id;

    private String email;
    
    private String phone;
    
    private Integer status;

    private String role;

    private Long tenantId;
}
