package com.zhongjia.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "GzhArticleVO", description = "公众号文章展示数据")
public class GzhArticleVO {
    @Schema(description = "文章ID")
    private Long id;
    @Schema(description = "所属用户ID")
    private Long userId;
    @Schema(description = "文件夹ID")
    private Long folderId;
    @Schema(description = "文章名称")
    private String name;
    @Schema(description = "文章标签")
    private String tag;
    @Schema(description = "封面图URL")
    private String coverImageUrl;
    @Schema(description = "原始文本")
    private String originalText;
    @Schema(description = "排版内容(HTML)")
    private String typesetContent;
    @Schema(description = "状态 INITIAL/EDITING/REVIEWING/APPROVED/REJECTED/PUBLISHED")
    private String status;
    @Schema(description = "最后编辑时间")
    private LocalDateTime lastEditTime;
    @Schema(description = "是否删除：0否/1是")
    private Integer deleted;
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}


