package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.FillInRecord;
import com.zhongjia.biz.service.FillInRecordService;
import com.zhongjia.web.security.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/fill_in")
public class FillInController {

    @Autowired
    private FillInRecordService recordService;

    @Value("${app.upstream.fill-in-url:http://192.168.1.65:8000/fill_in}")
    private String upstreamUrl;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void fillIn(@Valid @RequestBody FillInReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        FillInRecord record = new FillInRecord()
                .setUserId(user.userId())
                .setTenantId(user.tenantId())
                .setReqContent(req.getContent())
                .setCreateTime(LocalDateTime.now());
        recordService.save(record);

        response.setStatus(200);
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.flushBuffer();

        StringBuilder respBuf = new StringBuilder(256);
        try {
            streamUpstream(req, response, respBuf);
            record.setSuccess(true).setRespContent(respBuf.toString());
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            writeSseLine(response, "data:{\"code\":500,\"success\":false,\"msg\":\"服务异常！\",\"data\":null}\n\n");
        } finally {
            recordService.updateById(record);
        }
    }

    private void streamUpstream(FillInReq req, HttpServletResponse resp, StringBuilder respBuf) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        String body = "{" + "\"content\":\"" + escapeJson(req.getContent()) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMinutes(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<java.io.InputStream> upstream = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (upstream.statusCode() != 200) {
            throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
        }
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(upstream.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("data:")) {
                    writeSseLine(resp, line + "\n\n");
                    // 累积 choices[0].delta.content 字段的字符串。这里只做原样累积，交由前端解析 <FIELD:...><SEP>
                    respBuf.append(extractContentDelta(line));
                }
            }
        }
    }

    private static void writeSseLine(HttpServletResponse response, String s) throws IOException {
        response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new RuntimeException("未认证");
        return info;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String extractContentDelta(String dataLine) {
        // 粗略提取 JSON 中的 \"content\":\"...\" 片段（上游是逐 token 输出，兼容性要求不高）
        int idx = dataLine.indexOf("\"content\":");
        if (idx < 0) return "";
        int start = dataLine.indexOf('"', idx + 10);
        if (start < 0) return "";
        int end = dataLine.indexOf('"', start + 1);
        if (end < 0) return "";
        String piece = dataLine.substring(start + 1, end);
        // 反转义基本字符
        return piece.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    @Data
    public static class FillInReq {
        @NotBlank
        private String content;
    }
}


