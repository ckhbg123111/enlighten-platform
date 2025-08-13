package com.zhongjia.web.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DraftVO {
    private Long id;
    private String essayCode;
    private String title;
    private String content;
    private Integer deleted;

    private List<String> mediaCodeList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
}


