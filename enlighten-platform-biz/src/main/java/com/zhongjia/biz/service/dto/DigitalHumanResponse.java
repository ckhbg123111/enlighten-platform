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
        private String task_id;
        
        /**
         * 音频数据列表
         */
        private List<AudioData> audio_data;
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
