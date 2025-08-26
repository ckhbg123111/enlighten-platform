package com.zhongjia.biz.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数字人生成请求DTO
 */
@Data
@Accessors(chain = true)
public class DigitalHumanRequest {
    
    /**
     * 数字人模型名称
     */
    private String modelName;
    
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
    private Boolean enableSplit;
    
    /**
     * 切分参数 (可选)
     */
    private String splitParams;
}
