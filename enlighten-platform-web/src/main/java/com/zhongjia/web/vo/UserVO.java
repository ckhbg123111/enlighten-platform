package com.zhongjia.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户VO类
 */
@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
