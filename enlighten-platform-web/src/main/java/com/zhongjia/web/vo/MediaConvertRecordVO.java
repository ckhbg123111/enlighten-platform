package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(name = "MediaConvertRecordVO", description = "媒体内容转换记录")
public class MediaConvertRecordVO {
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "媒体唯一编码")
    private String code;

    @Schema(description = "文章编码")
    private String essayCode;

    @Schema(description = "目标平台：xiaohongshu/douyin/wechat")
    private String platform;

    @Schema(description = "上游返回code")
    private Integer respCode;

    @Schema(description = "上游返回msg")
    private String respMsg;

    @Schema(description = "上游返回success")
    private Boolean respSuccess;

    @Schema(description = "上游返回data(JSON字符串)")
    private String respData;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}


