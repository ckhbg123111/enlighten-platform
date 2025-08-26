package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 数字人生成响应DTO
 */
@Data
@Accessors(chain = true)
public class DigitalHumanResponse {
    
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
    private DhData data;
    
    @Data
    public static class DhData {
        /**
         * 任务ID
         */
        private String taskId;
        
        /**
         * 音频数据列表
         */
        private List<AudioData> audioData;
    }
    
    @Data
    public static class AudioData {
        /**
         * 文本内容
         */
        private String text;
        
        /**
         * 时间信息 (格式: "00:00:00,170 --> 00:00:01,670")
         */
        private String time;
    }
}
