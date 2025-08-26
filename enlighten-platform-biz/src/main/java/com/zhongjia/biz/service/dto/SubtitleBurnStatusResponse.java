package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 字幕烧录状态查询响应DTO
 */
@Data
@Accessors(chain = true)
public class SubtitleBurnStatusResponse {
    
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
     * 响应数据
     */
    private BurnStatusData data;
    
    @Data
    public static class BurnStatusData {
        /**
         * 任务状态: PENDING/PROCESSING/COMPLETED/FAILED
         */
        private String state;
        
        /**
         * 进度 (0-100)
         */
        private Integer progress;
        
        /**
         * 输出URL (完成后)
         */
        private String outputUrl;
        
        /**
         * 错误信息 (失败时)
         */
        private String errorMessage;
    }
}
