package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 模板实体类
 */
@Data
@Accessors(chain = true)
@TableName("typesetting_template")
public class TypesettingTemplate {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 所属医院
     */
    private String hospital;
    
    /**
     * 所属科室
     */
    private String department;
    
    /**
     * 标签（字段预留，暂时不用）
     */
    private String tag;
    
    /**
     * 顺序
     */
    private Integer sort;
    
    /**
     * 模板头
     */
    private String header;
    
    /**
     * 模板脚
     */
    private String footer;
    
    /**
     * 正文模板
     */
    @TableField("`text`")
    private String text;
    
    /**
     * 图片样式
     */
    @TableField("`image`")
    private String image;
    
    /**
     * 封面图
     */
    @TableField("cover_image")
    private String coverImage;
    
    /**
     * 单行标题样式
     */
    private String singleTitle;
    
    /**
     * 多行标题样式
     */
    private String doubleTitle;
    
    /**
     * 文本框样式
     */
    private String textCard;
    
    /**
     * 图文框样式
     */
    private String blockCard;
    
    /**
     * 副标题样式
     */
    private String numberedTitle;
    
    /**
     * 样例
     */
    private String sample;
    
    /**
     * 标题样例
     */
    @TableField("title_sample")
    private String titleSample;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted;
}
