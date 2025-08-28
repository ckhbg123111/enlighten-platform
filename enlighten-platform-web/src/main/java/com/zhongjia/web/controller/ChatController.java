package com.zhongjia.web.controller;

import com.zhongjia.biz.service.ScienceChatService;
import com.zhongjia.web.req.ChatReq;
import com.zhongjia.web.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@Tag(name = "患者端对话")
@RequestMapping("/api/science-chat")
public class ChatController {

    @Autowired
    private ScienceChatService chatService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天(SSE)", description = "返回 text/event-stream")
    public void chat(@Valid @RequestBody ChatReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        response.setStatus(200);
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.flushBuffer();

        int limit = req.getHistoryLimit() == null ? 0 : Math.max(0, req.getHistoryLimit());
        String messagesJson;
        try {
            messagesJson = objectMapper.writeValueAsString(req.getMessages());
        } catch (Exception e) {
            throw new RuntimeException("请求参数序列化失败", e);
        }

        chatService.streamChat(
                user.userId(),
                req.getSessionId(),
                messagesJson,
                req.getNeedRecommend(),
                req.getPrompt(),
                limit,
                line -> {
                    try {
                        response.getOutputStream().write(line.getBytes(StandardCharsets.UTF_8));
                        response.flushBuffer();
                    } catch (Exception ignored) {}
                }
        );
    }

    private static UserContext.UserInfo requireUser() {
        UserContext.UserInfo user = UserContext.get();
        if (user == null) throw new RuntimeException("未认证");
        return user;
    }
}


