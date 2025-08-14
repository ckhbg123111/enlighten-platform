package com.zhongjia.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(name = "FillInRecordVO", description = "填空生成记录")
public class FillInRecordVO {
    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "请求内容")
    private String reqContent;
    @Schema(description = "返回内容")
    private String respContent;
    @Schema(description = "是否成功")
    private Boolean success;
    @Schema(description = "错误信息")
    private String errorMessage;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}


