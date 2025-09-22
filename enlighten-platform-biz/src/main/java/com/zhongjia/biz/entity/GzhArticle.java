package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("gzh_article")
public class GzhArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long folderId;

    private String name;

    private String namePinyin;

    private String tag;

    private String coverImageUrl;

    private String originalText;

    private String typesetContent;

    private String status; // 使用字符串以匹配数据库字段，业务层用枚举

    private LocalDateTime lastEditTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime deleteTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}


