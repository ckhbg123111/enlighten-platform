package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户VO类
 */
@Data
@Schema(name = "UserVO", description = "用户信息")
public class UserVO {
    
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "状态：1启用/0禁用")
    private Integer status;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "上次登录时间")
    private LocalDateTime lastLoginTime;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "医院名称（Beta）")
    private String hospital;
    @Schema(description = "科室名称（Beta）")
    private String department;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
