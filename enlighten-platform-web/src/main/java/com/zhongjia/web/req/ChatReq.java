package com.zhongjia.web.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ChatReq {
    /** UUID 作为本次会话唯一标识 */
    @NotBlank
    private String sessionId;

    /** 结构化的消息列表，兼容字符串与携带图片的多模态结构 */
    @NotNull
    private List<Message> messages;

    private Boolean needRecommend = Boolean.TRUE;

    private String prompt;

    /** 携带历史条数（可选），不传则用全局默认 */
    private Integer historyLimit;

    @Data
    public static class Message {
        @NotBlank
        private String role; // user | assistant

        /** content 可为字符串或多模态列表（按上游协议） */
        private Object content;
    }
}


