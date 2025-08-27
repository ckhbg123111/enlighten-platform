package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数字人任务状态响应DTO
 */
@Data
@Accessors(chain = true)
public class DigitalHumanStatusResponse {
    
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
    private String msg;
    
    /**
     * 响应数据
     */
    private DhStatusData data;
    
    @Data
    public static class DhStatusData {
        /**
         * 任务状态
         */
        private String status;
        
        /**
         * 进度 (0-100)
         */
        private Integer progress;
        
        /**
         * 结果URL (完成后)
         */
        private String result_url;
        
        /**
         * 错误信息 (失败时)
         */
        private String error_message;
    }
}
