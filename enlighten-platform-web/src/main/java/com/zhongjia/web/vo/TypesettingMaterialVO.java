package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 素材VO类
 */
@Data
@Schema(name = "TypesettingMaterialVO", description = "素材信息")
public class TypesettingMaterialVO {
    
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "素材名称")
    private String name;
    
    @Schema(description = "顺序")
    private Integer sort;
    
    @Schema(description = "素材类型")
    private String type;
    
    @Schema(description = "素材内容")
    private String content;
}
