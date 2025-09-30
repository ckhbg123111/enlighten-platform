package com.zhongjia.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "MediaConvertRecordV2InfoVO", description = "媒体转换记录V2列表数据")
public class MediaConvertRecordV2InfoVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "外部ID，例如文章ID")
    private Long externalId;
    @Schema(description = "平台：gzh/xiaohongshu/douyin")
    private String platform;
    @Schema(description = "状态：PROCESSING/SUCCESS/FAILED")
    private String status;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "是否删除：0否/1是")
    private Integer deleted;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;
}


