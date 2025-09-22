package com.zhongjia.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "RecycleBinItemVO", description = "回收站条目展示数据")
public class RecycleBinItemVO {
    @Schema(description = "回收站条目ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "关联文件ID")
    private Long fileId;
    @Schema(description = "文件类型")
    private String fileType;
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}


