package com.zhongjia.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "VideoRecordTempVO", description = "视频临时记录返回对象")
public class VideoRecordTempVO {
    private Long id;
    private Long userId;
    private String taskId;
    private String name;
    private String status;
    private String url;
    private List<Object> stepList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


