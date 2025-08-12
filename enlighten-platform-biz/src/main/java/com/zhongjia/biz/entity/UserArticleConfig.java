package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户文章配置项
 */
@Data
@Accessors(chain = true)
@TableName("user_article_config")
public class UserArticleConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 分类: style, length, form, scene
     */
    private String category;

    /**
     * 展示名称
     */
    private String optionName;

    /**
     * 选项唯一编码（UUID）
     */
    private String optionCode;

    /**
     * 排序序号
     */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}


