package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 字幕烧录响应DTO
 */
@Data
@Accessors(chain = true)
public class SubtitleBurnResponse {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 任务ID
     */
    private String taskId;
}
