package com.zhongjia.web.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatReq {
    /** UUID 作为本次会话唯一标识 */
    @NotBlank
    private String sessionId;

    /** messages 原样 JSON 字符串，前端直接传数组字符串以支持图片结构 */
    @NotBlank
    private String messages;

    private Boolean needRecommend = Boolean.TRUE;

    private String prompt;

    /** 携带历史条数（可选），不传则用全局默认 */
    private Integer historyLimit;
}


