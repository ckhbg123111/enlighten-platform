package com.zhongjia.web.controller;

import com.zhongjia.biz.service.FillInRecordService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@Tag(name = "填空生成")
@RequestMapping("/api/fill_in")
public class FillInController {

    @Autowired
    private FillInRecordService recordService;

    // 上游交互已下沉至 Service

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "流式帮填生成(SSE)", description = "返回 text/event-stream", security = {@SecurityRequirement(name = "bearer-jwt")})
    public void fillIn(@Valid @RequestBody FillInReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        response.setStatus(200);
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.flushBuffer();

        recordService.streamFillIn(user.userId(), user.tenantId(), req.getContent(), line -> {
            try {
                writeSseLine(response, line);
            } catch (Exception ignored) {}
        });
    }

    private static void writeSseLine(HttpServletResponse response, String s) throws IOException {
        response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }


    @Data
    @Schema(name = "FillInReq", description = "填空生成请求")
    public static class FillInReq {
        @Schema(description = "输入内容")
        @NotBlank
        private String content;
    }
}


