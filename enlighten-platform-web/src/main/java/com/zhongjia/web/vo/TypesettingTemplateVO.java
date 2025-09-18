package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模板VO类
 */
@Data
@Schema(name = "TypesettingTemplateVO", description = "模板信息")
public class TypesettingTemplateVO {
    
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "模板名称")
    private String name;
    
    @Schema(description = "标签（字段预留，暂时不用）")
    private String tag;
    
    @Schema(description = "顺序")
    private Integer sort;
    
    @Schema(description = "模板头")
    private String header;
    
    @Schema(description = "模板脚")
    private String footer;
    
    @Schema(description = "正文模板")
    private String text;
    
    @Schema(description = "图片样式")
    private String image;
    
    @Schema(description = "单行标题样式")
    private String singleTitle;
    
    @Schema(description = "多行标题样式")
    private String doubleTitle;
    
    @Schema(description = "文本框样式")
    private String textCard;
    
    @Schema(description = "图文框样式")
    private String blockCard;
    
    @Schema(description = "副标题样式")
    private String numberedTitle;
}
