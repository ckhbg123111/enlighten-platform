package com.zhongjia.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DraftVO {
    private Long id;
    private String essayCode;
    private String title;
    private String content;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
}


