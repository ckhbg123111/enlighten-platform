package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.entity.FillInRecord;
import com.zhongjia.biz.repository.FillInRecordRepository;
import com.zhongjia.biz.service.FillInRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FillInRecordServiceImpl implements FillInRecordService {

    @Autowired
    private FillInRecordRepository fillInRecordRepository;
    
    @Autowired
    private HttpClient httpClient;
    
    @Autowired
    private WebClient webClient;

    @Value("${app.upstream.url}")
    private String upstreamUrl;

    private static final String fillInPath = "/fill_in";

    @Override
    public String streamFillIn(Long userId, Long tenantId, String content, java.util.function.Consumer<String> writeLine) {
        FillInRecord record = new FillInRecord()
            .setUserId(userId)
            .setTenantId(tenantId)
            .setReqContent(content)
            .setCreateTime(LocalDateTime.now());
        fillInRecordRepository.save(record);

        StringBuilder respBuf = new StringBuilder(256);
        try {
            String body = "{" + "\"content\":\"" + escapeJson(content) + "\"}";
            final String traceId = org.slf4j.MDC.get("traceId");
            final StringBuilder carry = new StringBuilder();

            webClient.post()
                .uri(upstreamUrl + fillInPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Trace-Id", traceId == null ? "" : traceId)
                .bodyValue(body)
                .exchangeToFlux(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToFlux(DataBuffer.class);
                    }
                    return Flux.error(new IllegalStateException("上游返回非200:" + resp.statusCode().value()));
                })
                .map(db -> {
                    byte[] bytes = new byte[db.readableByteCount()];
                    db.read(bytes);
                    DataBufferUtils.release(db);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .concatMap(chunk -> {
                    carry.append(chunk);
                    java.util.List<String> lines = new java.util.ArrayList<>();
                    int idx;
                    while ((idx = carry.indexOf("\n")) >= 0) {
                        String line = carry.substring(0, idx);
                        if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
                        lines.add(line);
                        carry.delete(0, idx + 1);
                    }
                    return Flux.fromIterable(lines);
                })
                .doOnNext(line -> {
                    if (line.startsWith("data:")) {
                        writeLine.accept(line + "\n\n");
                        respBuf.append(extractContentDelta(line));
                    }
                })
                .blockLast();
            record.setSuccess(true).setRespContent(respBuf.toString());
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            writeLine.accept("data:{\"code\":500,\"success\":false,\"msg\":\"服务异常！\",\"data\":null}\n\n");
        } finally {
            fillInRecordRepository.updateById(record);
        }
        return respBuf.toString();
    }

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

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}


