package com.zhongjia.biz.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数字人生成请求DTO
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DigitalHumanRequest {
    
    /**
     * 数字人模型名称
     */
    private String model_name;
    
    /**
     * 输入文本
     */
    private String text;
    
    /**
     * 语音类型
     */
    private String voice;
    
    /**
     * 是否进行文本切分 (可选)
     */
    private Integer text_split_len;
    
    /**
     * 切分参数 (可选)
     */
    private Integer speech_pause_split_ms;

    /**
     * 用户ID（上游0916新增，必填）
     */
    private String user_id;
}
