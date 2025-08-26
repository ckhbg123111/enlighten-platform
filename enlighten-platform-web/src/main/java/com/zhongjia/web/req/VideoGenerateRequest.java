package com.zhongjia.web.req;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 视频生成请求DTO
 */
@Data
public class VideoGenerateRequest {
    
    /**
     * 输入文本，必填
     */
    @NotBlank(message = "输入文本不能为空")
    @Size(max = 5000, message = "输入文本长度不能超过5000字符")
    private String text;
    
    /**
     * 数字人模型名称，可选
     */
    private String modelName;
    
    /**
     * 语音类型，可选，默认 Female_Voice_1
     */
    private String voice = "Female_Voice_1";
}
