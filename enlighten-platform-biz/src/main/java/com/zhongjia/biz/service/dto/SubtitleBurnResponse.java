package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 字幕烧录响应DTO
 */
@Data
@Accessors(chain = true)
public class SubtitleBurnResponse {
    // fixme Result 响应码

    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * PENDING/DOWNLOADING/PROCESSING/UPLOADING/COMPLETED/FAILED
     */
    private String state;

    private String outputUrl;

    private String errorMessage;

    private Integer progress;
}
