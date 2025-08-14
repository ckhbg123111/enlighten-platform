package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(name = "ArticleOptionVO", description = "文章选项数据")
public class ArticleOptionVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "名称")
    private String optionName;
    @Schema(description = "编码")
    private String optionCode;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}


