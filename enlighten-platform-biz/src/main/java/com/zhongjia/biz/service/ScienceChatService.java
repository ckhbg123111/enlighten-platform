package com.zhongjia.biz.service;

import java.util.function.Consumer;

public interface ScienceChatService {
    /**
     * 代理上游 /science-chat SSE 接口，将上游逐行数据写给回调，同时完成落库与缓存。
     * 返回拼接后的完整 assistant 文本。
     */
    String streamChat(Long userId,
                      String sessionId,
                      String messagesJson,
                      Boolean needRecommend,
                      String prompt,
                      int historyLimit,
                      Consumer<String> sseWriter);
}


