package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "DraftVO", description = "草稿数据")
public class DraftVO {
    @Schema(description = "草稿ID")
    private Long id;
    @Schema(description = "文章编码")
    private String essayCode;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "正文")
    private String content;
    @Schema(description = "标签")
    private String tags;
    @Schema(description = "是否删除：0否/1是")
    private Integer deleted;

    @ArraySchema(schema = @Schema(description = "媒体素材编码"))
    private List<String> mediaCodeList;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;
}


