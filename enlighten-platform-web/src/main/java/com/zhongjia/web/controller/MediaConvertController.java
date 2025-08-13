package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.enums.MediaPlatform;
import com.zhongjia.biz.service.MediaConvertRecordService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/convert2media")
public class MediaConvertController {

    @Autowired
    private MediaConvertRecordService recordService;

    @Value("${app.upstream.convert2media-url:http://192.168.1.65:8000/convert2media}")
    private String upstreamUrl;

    // 1) 转换：解析上游，并记录
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void convert(@Valid @RequestBody ConvertReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        if (!MediaPlatform.isValid(req.getPlatform())) {
            writeJson(response, 400, false, "platform不合法！", null);
            return;
        }

        MediaConvertRecord record = new MediaConvertRecord()
                .setUserId(user.userId())
                .setTenantId(user.tenantId())
                .setEssayCode(req.getEssayCode())
                .setContent(req.getContent())
                .setPlatform(req.getPlatform())
                .setCreateTime(LocalDateTime.now());
        recordService.save(record);

        try {
            String upstreamResp = callUpstream(req);
            // 使用固定结构体承接上游返回
            UpstreamResp parsed = parseUpstreamJson(upstreamResp);
            String dataRaw;
            try {
                dataRaw = parsed == null || parsed.data == null ? "null" : JSON.writeValueAsString(parsed.data);
            } catch (Exception ignore) {
                dataRaw = "null";
            }

            record.setSuccess(Boolean.TRUE)
                    .setRespCode(parsed == null ? null : parsed.code)
                    .setRespMsg(parsed == null ? null : parsed.msg)
                    .setRespSuccess(parsed == null ? null : parsed.success)
                    .setRespData(dataRaw);

            // 只返回data，其他字段由本服务包装
            int code = parsed == null || parsed.code == null ? 200 : parsed.code;
            boolean success = parsed != null && parsed.success != null && parsed.success;
            String msg = parsed == null || parsed.msg == null ? "ok" : parsed.msg;
            writeJson(response, code, success, msg, dataRaw);
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            writeJson(response, 500, false, "服务异常！", null);
        } finally {
            recordService.updateById(record);
        }
    }

    // 2) 查询：通过 essayCode，倒序（按时间）
    @GetMapping("/records")
    public Result<List<MediaConvertRecord>> listByEssayCode(@RequestParam("essayCode") String essayCode) {
        UserContext.UserInfo user = requireUser();
        List<MediaConvertRecord> list = recordService.list(new LambdaQueryWrapper<MediaConvertRecord>()
                .eq(MediaConvertRecord::getUserId, user.userId())
                .eq(MediaConvertRecord::getEssayCode, essayCode)
                .eq(MediaConvertRecord::getDeleted, 0)
                .orderByDesc(MediaConvertRecord::getCreateTime));
        return Result.success(list);
    }

    // 3) 删除：软删除
    @DeleteMapping("/{id}")
    public Result<Boolean> softDelete(@PathVariable("id") Long id) {
        UserContext.UserInfo user = requireUser();
        MediaConvertRecord exist = recordService.getById(id);
        if (exist == null || exist.getDeleted() != null && exist.getDeleted() == 1) {
            return Result.error(404, "记录不存在");
        }
        if (!exist.getUserId().equals(user.userId())) {
            return Result.error(403, "无权限");
        }
        exist.setDeleted(1).setDeleteTime(LocalDateTime.now());
        boolean ok = recordService.updateById(exist);
        return ok ? Result.success(true) : Result.error(500, "删除失败");
    }

    private String callUpstream(ConvertReq req) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        String body = "{" +
                "\"content\":\"" + escapeJson(req.getContent()) + "\"," +
                "\"platform\":\"" + escapeJson(req.getPlatform()) + "\"" +
                "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> upstream = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (upstream.statusCode() != 200) {
            throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
        }
        return upstream.body();
    }

    private static void writeJson(HttpServletResponse response, int code, boolean success, String msg, Object data) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        String payload = String.format("{\"code\":%d,\"success\":%s,\"msg\":\"%s\",\"data\":%s}",
                code,
                success ? "true" : "false",
                escapeJson(msg == null ? "" : msg),
                data == null ? "null" : data.toString());
        response.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private static UpstreamResp parseUpstreamJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return JSON.readValue(json, UpstreamResp.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static class UpstreamResp {
        public Integer code;
        public Boolean success;
        public String msg;
        public Object data;
    }

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new RuntimeException("未认证");
        return info;
    }

    @Data
    public static class ConvertReq {
        @NotBlank
        private String content;
        @NotBlank
        private String platform; // xiaohongshu / douyin
        @NotBlank
        private String essayCode; // 额外字段
    }
}


