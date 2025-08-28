package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import com.zhongjia.biz.service.MediaConvertRecordService;
import com.zhongjia.biz.service.dto.UpstreamResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MediaConvertRecordServiceImpl implements MediaConvertRecordService {
    private final MediaConvertRecordRepository mediaConvertRecordRepository;
    private final HttpClient httpClient;

    public MediaConvertRecordServiceImpl(MediaConvertRecordRepository mediaConvertRecordRepository, HttpClient httpClient) {
        this.mediaConvertRecordRepository = mediaConvertRecordRepository;
        this.httpClient = httpClient;
    }
    private static final ObjectMapper JSON = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${app.upstream.convert2media-url:http://192.168.1.65:8000/convert2media}")
    private String upstreamUrl;

    @org.springframework.beans.factory.annotation.Value("${app.upstream.convert2gzh-re-url:http://192.168.1.65:8000/convert2gzh_re}")
    private String upstreamGzhReUrl;

    @org.springframework.beans.factory.annotation.Value("${app.upstream.convert2gzh-url:http://192.168.1.65:8000/convert2gzh}")
    private String upstreamGzhUrl;

    @Override
    public UpstreamResult convertCommon(Long userId, Long tenantId, String mediaCode, String essayCode, String content, String platform) {
        MediaConvertRecord record = new MediaConvertRecord()
            .setUserId(userId)
            .setTenantId(tenantId)
            .setCode(mediaCode)
            .setEssayCode(essayCode)
            .setContent(content)
            .setPlatform(platform)
            .setCreateTime(LocalDateTime.now());
        mediaConvertRecordRepository.save(record);

        UpstreamResult result = new UpstreamResult();
        try {
            String upstreamResp = callUpstreamConvert(content, platform);
            UpstreamResp parsed = parseUpstreamJson(upstreamResp);
            String dataRaw = toDataRaw(parsed);

            record.setSuccess(Boolean.TRUE)
                .setRespCode(parsed == null ? null : parsed.code)
                .setRespMsg(parsed == null ? null : parsed.msg)
                .setRespSuccess(parsed == null ? null : parsed.success)
                .setRespData(dataRaw);

            int code = parsed == null || parsed.code == null ? 200 : parsed.code;
            boolean success = parsed != null && parsed.success != null && parsed.success;
            String msg = parsed == null || parsed.msg == null ? "ok" : parsed.msg;

            result.setCode(code).setSuccess(success).setMsg(msg).setDataRaw(dataRaw);
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            result.setCode(500).setSuccess(false).setMsg("服务异常！").setDataRaw(null);
        } finally {
            mediaConvertRecordRepository.updateById(record);
        }
        return result;
    }

    @Override
    public UpstreamResult convertGzhRe(Long userId, Long tenantId, String mediaCode, String essayCode, String content) {
        MediaConvertRecord record = new MediaConvertRecord()
            .setUserId(userId)
            .setTenantId(tenantId)
            .setCode(mediaCode)
            .setEssayCode(essayCode)
            .setContent(content)
            .setPlatform("ghz")
            .setCreateTime(LocalDateTime.now());
        mediaConvertRecordRepository.save(record);

        UpstreamResult result = new UpstreamResult();
        try {
            String upstreamResp = callUpstreamGzhRe(content);
            UpstreamResp parsed = parseUpstreamJson(upstreamResp);
            String dataRaw = toDataRaw(parsed);

            record.setSuccess(Boolean.TRUE)
                .setRespCode(parsed == null ? null : parsed.code)
                .setRespMsg(parsed == null ? null : parsed.msg)
                .setRespSuccess(parsed == null ? null : parsed.success)
                .setRespData(dataRaw);

            int code = parsed == null || parsed.code == null ? 200 : parsed.code;
            boolean success = parsed != null && parsed.success != null && parsed.success;
            String msg = parsed == null || parsed.msg == null ? "ok" : parsed.msg;
            result.setCode(code).setSuccess(success).setMsg(msg).setDataRaw(dataRaw);
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            result.setCode(500).setSuccess(false).setMsg("服务异常！").setDataRaw(null);
        } finally {
            mediaConvertRecordRepository.updateById(record);
        }
        return result;
    }

    @Override
    public UpstreamResult convertGzh(Long userId, Long tenantId, String mediaCode, String essayCode, String contentJson) {
        MediaConvertRecord record = new MediaConvertRecord()
            .setUserId(userId)
            .setTenantId(tenantId)
            .setCode(mediaCode)
            .setEssayCode(essayCode)
            .setContent(contentJson)
            .setPlatform("ghz")
            .setCreateTime(LocalDateTime.now());
        mediaConvertRecordRepository.save(record);

        UpstreamResult result = new UpstreamResult();
        try {
            String upstreamResp = callUpstreamGzh(contentJson);
            UpstreamResp parsed = parseUpstreamJson(upstreamResp);
            String dataRaw = toDataRaw(parsed);

            record.setSuccess(Boolean.TRUE)
                .setRespCode(parsed == null ? null : parsed.code)
                .setRespMsg(parsed == null ? null : parsed.msg)
                .setRespSuccess(parsed == null ? null : parsed.success)
                .setRespData(dataRaw);

            int code = parsed == null || parsed.code == null ? 200 : parsed.code;
            boolean success = parsed != null && parsed.success != null && parsed.success;
            String msg = parsed == null || parsed.msg == null ? "ok" : parsed.msg;
            result.setCode(code).setSuccess(success).setMsg(msg).setDataRaw(dataRaw);
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            result.setCode(500).setSuccess(false).setMsg("服务异常！").setDataRaw(null);
        } finally {
            mediaConvertRecordRepository.updateById(record);
        }
        return result;
    }

    private String callUpstreamConvert(String content, String platform) throws Exception {
        String body = "{" +
            "\"content\":\"" + escapeJson(content) + "\""," +
            "\"platform\":\"" + escapeJson(platform) + "\"" +
            "}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(upstreamUrl))
            .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (upstream.statusCode() != 200) {
            throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
        }
        return upstream.body();
    }

    private String callUpstreamGzhRe(String content) throws Exception {
        String body = "{" +
            "\"content\":\"" + escapeJson(content) + "\"" +
            "}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(upstreamGzhReUrl))
            .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (upstream.statusCode() != 200) {
            throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
        }
        return upstream.body();
    }

    private String callUpstreamGzh(String contentJson) throws Exception {
        String body = contentJson;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(upstreamGzhUrl))
            .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (upstream.statusCode() != 200) {
            throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
        }
        return upstream.body();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private static String toDataRaw(UpstreamResp parsed) {
        try {
            return parsed == null || parsed.data == null ? "null" : JSON.writeValueAsString(parsed.data);
        } catch (Exception ignore) {
            return "null";
        }
    }

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
}


