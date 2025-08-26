package com.zhongjia.web.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频生成响应DTO
 */
@Data
@Accessors(chain = true)
public class VideoGenerateResponse {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 响应消息
     */
    private String message;
}
