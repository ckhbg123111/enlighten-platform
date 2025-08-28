package com.zhongjia.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhongjia.biz.entity.ScienceGenRecord;
import com.zhongjia.biz.repository.ScienceGenRecordRepository;
import com.zhongjia.biz.service.ScienceGenRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class ScienceGenRecordServiceImpl implements ScienceGenRecordService {

    private final ScienceGenRecordRepository scienceGenRecordRepository;

    private final HttpClient httpClient;

    public ScienceGenRecordServiceImpl(ScienceGenRecordRepository scienceGenRecordRepository, HttpClient httpClient) {
        this.scienceGenRecordRepository = scienceGenRecordRepository;
        this.httpClient = httpClient;
    }
    @org.springframework.beans.factory.annotation.Value("${app.upstream.science-generator-url:http://192.168.1.65:8000/science-generator}")
    private String upstreamUrl;

    @Value("${app.upstream.science-regenerator-url:http://192.168.1.65:8000/science-regenerator}")
    private String regenerateUrl;

    @Override
    public String streamGenerate(Long userId, Long tenantId, String code, String upstreamBody, Consumer<String> writeLine) {
        ScienceGenRecord record = new ScienceGenRecord()
            .setUserId(userId)
            .setTenantId(tenantId)
            .setCode(code)
            .setReqBody(upstreamBody)
            .setCreateTime(LocalDateTime.now());
        scienceGenRecordRepository.save(record);

        StringBuilder respBuf = new StringBuilder(256);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMinutes(10))
                .header("Content-Type", "application/json")
                .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                .POST(HttpRequest.BodyPublishers.ofString(upstreamBody, StandardCharsets.UTF_8))
                .build();
            HttpResponse<java.io.InputStream> upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (upstream.statusCode() != 200) {
                throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(upstream.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        writeLine.accept(line + "\n\n");
                        respBuf.append(extractContentDelta(line));
                    }
                }
            }
            record.setSuccess(true).setRespContent(respBuf.toString());
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            writeLine.accept("data:{\"code\":500,\"success\":false,\"msg\":\"服务异常！\",\"data\":null}\n\n");
        } finally {
            scienceGenRecordRepository.updateById(record);
        }
        return respBuf.toString();
    }

    @Override
    public String streamReGenerate(String code,  Consumer<String> writeLine) {
        ScienceGenRecord byCode = scienceGenRecordRepository.getByCode(code);
        if(Objects.isNull(byCode)) {
            writeLine.accept("data:{\"code\":400,\"success\":false,\"msg\":\"无效的code！\",\"data\":null}\n\n");
            return null;
        }

        RegenerateReq regenerateReq = new RegenerateReq( byCode.getRespContent());
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        sb.append("\"content\":").append(regenerateReq.content);
        sb.append('}');

        String upstreamBody = sb.toString();
        StringBuilder respBuf = new StringBuilder(256);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(regenerateUrl))
                    .timeout(Duration.ofMinutes(10))
                    .header("Content-Type", "application/json")
                    .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                    .POST(HttpRequest.BodyPublishers.ofString(upstreamBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<java.io.InputStream> upstream = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (upstream.statusCode() != 200) {
                throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(upstream.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        writeLine.accept(line + "\n\n");
                        respBuf.append(extractContentDelta(line));
                    }
                }
            }
            byCode.setSuccess(true).setRespContent(respBuf.toString()).setErrorMessage(null);
        } catch (Exception ex) {
            byCode.setSuccess(false).setErrorMessage(ex.getMessage());
            writeLine.accept("data:{\"code\":500,\"success\":false,\"msg\":\"重生成服务异常！\",\"data\":null}\n\n");
        } finally {
            scienceGenRecordRepository.updateById(byCode);
        }
        return respBuf.toString();
    }
    record RegenerateReq(String content){};

    private static String extractContentDelta(String dataLine) {
        int idx = dataLine.indexOf("\"content\":");
        if (idx < 0) return "";
        int start = dataLine.indexOf('"', idx + 10);
        if (start < 0) return "";
        int end = dataLine.indexOf('"', start + 1);
        if (end < 0) return "";
        String piece = dataLine.substring(start + 1, end);
        return piece.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}


