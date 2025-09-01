package com.zhongjia.web.req;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 创建用户请求类
 */
@Data
@Schema(name = "UserCreateReq", description = "创建用户请求")
public class UserCreateReq {
    
    @Schema(description = "用户名", example = "alice")
    private String username;
    
    @Schema(description = "密码(明文)")
    private String password;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "状态：1启用/0禁用", example = "1")
    private Integer status = 1; // 默认启用

    /**
     * 角色：ADMIN/USER
     */
    @Schema(description = "角色：ADMIN/USER")
    private String role;

    /**
     * 租户ID（可选）
     */
    @Schema(description = "租户ID")
    private Long tenantId;

    /**
     * 医院名称（Beta）
     */
    @Schema(description = "医院名称（Beta）", example = "北京协和医院")
    private String hospital;

    /**
     * 科室名称（Beta）
     */
    @Schema(description = "科室名称（Beta）", example = "心内科")
    private String department;
}
