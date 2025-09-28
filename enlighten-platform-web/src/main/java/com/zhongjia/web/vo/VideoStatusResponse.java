package com.zhongjia.web.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频状态查询响应DTO
 */
@Data
@Accessors(chain = true)
public class VideoStatusResponse {
    private String taskId;
    
    /**
     * 任务状态: CREATED/DH_PROCESSING/DH_DONE/BURN_PROCESSING/COMPLETED/FAILED
     */
    private String status;
    
    /**
     * 任务进度 (0-100)
     */
    private Integer progress;
    
    /**
     * 结果URL (完成后为带字幕视频URL)
     */
    private String resultUrl;
    
    /**
     * 消息或错误信息
     */
    private String message;
    
    /**
     * 任务创建时间
     */
    private String createdAt;
    
    /**
     * 任务更新时间
     */
    private String updatedAt;
}
