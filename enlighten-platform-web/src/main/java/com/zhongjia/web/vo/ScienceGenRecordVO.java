package com.zhongjia.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScienceGenRecordVO {
    private Long id;
    private String code;
    private String respContent;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createTime;
}


