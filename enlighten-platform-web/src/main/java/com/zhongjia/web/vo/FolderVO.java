package com.zhongjia.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "FolderVO", description = "文件夹展示数据")
public class FolderVO {
    @Schema(description = "文件夹ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "是否删除：0否/1是")
    private Integer deleted;
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}


