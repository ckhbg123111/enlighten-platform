package com.zhongjia.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FillInRecordVO {
    private Long id;
    private String reqContent;
    private String respContent;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createTime;
}


