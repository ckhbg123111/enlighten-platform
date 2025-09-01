package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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
         * 音频数据列表
         */
        private List<AudioData> audio_data;
        
        /**
         * 错误信息 (失败时)
         */
        private String error_message;
    }

    @Data
    public static class AudioData {
        /**
         * 文本内容
         */
        private String text;

        /**
         * 时间信息，兼容数组形式 [start, end]
         */
        private List<String> time;
    }
}
