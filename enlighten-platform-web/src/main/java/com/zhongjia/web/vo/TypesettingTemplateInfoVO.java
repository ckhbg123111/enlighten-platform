package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模板VO类
 */
@Data
@Schema(name = "TypesettingTemplateVO", description = "模板列表信息")
public class TypesettingTemplateInfoVO {
    
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "模板名称")
    private String name;
    
    @Schema(description = "标签（字段预留，暂时不用）")
    private String tag;
    
    @Schema(description = "顺序")
    private Integer sort;
    
    @Schema(description = "封面图")
    private String coverImage;
}
