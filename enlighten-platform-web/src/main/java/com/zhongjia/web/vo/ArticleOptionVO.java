package com.zhongjia.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleOptionVO {
    private Long id;
    private String category;
    private String optionName;
    private String optionCode;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


