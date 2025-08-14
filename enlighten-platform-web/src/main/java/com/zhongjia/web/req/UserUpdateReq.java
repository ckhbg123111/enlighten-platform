package com.zhongjia.web.req;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 更新用户请求类
 */
@Data
@Schema(name = "UserUpdateReq", description = "更新用户请求")
public class UserUpdateReq {
    
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "状态：1启用/0禁用")
    private Integer status;

    @Schema(description = "角色：ADMIN/USER")
    private String role;

    @Schema(description = "租户ID")
    private Long tenantId;
}
