package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.entity.FillInRecord;
import com.zhongjia.biz.repository.FillInRecordRepository;
import com.zhongjia.biz.service.FillInRecordService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FillInRecordServiceImpl implements FillInRecordService {

    @Autowired
    private FillInRecordRepository fillInRecordRepository;
    
    @Autowired
    private HttpClient httpClient;
    
    @Autowired
    private WebClient webClient;
    @org.springframework.beans.factory.annotation.Value("${app.upstream.fill-in-url:http://192.168.1.65:8000/fill_in}")
    private String upstreamUrl;
    
    @org.springframework.beans.factory.annotation.Value("${app.upstream.fill-in-timeout:30}")
    private int initialTimeoutSeconds;
    
    @org.springframework.beans.factory.annotation.Value("${app.upstream.fill-in-total-timeout:300}")
    private int totalTimeoutSeconds;

    @Override
    public String streamFillIn(Long userId, Long tenantId, String content, java.util.function.Consumer<String> writeLine) {
        FillInRecord record = new FillInRecord()
            .setUserId(userId)
            .setTenantId(tenantId)
            .setReqContent(content)
            .setCreateTime(LocalDateTime.now());
        fillInRecordRepository.save(record);

        StringBuilder respBuf = new StringBuilder(256);
        AtomicBoolean firstTokenReceived = new AtomicBoolean(false);
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        
        try {
            String body = "{" + "\"content\":\"" + escapeJson(content) + "\"}";
            final String traceId = org.slf4j.MDC.get("traceId");
            final StringBuilder carry = new StringBuilder();

            webClient.post()
                .uri(upstreamUrl)
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
                .timeout(Duration.ofSeconds(totalTimeoutSeconds)) // 总体超时
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
                    // 检查首次响应超时
                    if (!firstTokenReceived.get()) {
                        long elapsed = System.currentTimeMillis() - startTime.get();
                        if (elapsed > initialTimeoutSeconds * 1000) {
                            throw new RuntimeException("等待首次响应超时，已等待" + elapsed/1000 + "秒");
                        }
                        firstTokenReceived.set(true);
                    }
                    
                    if (line.startsWith("data:")) {
                        writeLine.accept(line + "\n\n");
                        respBuf.append(extractContentDelta(line));
                    }
                })
                .onErrorResume(ex -> {
                    if (ex instanceof java.util.concurrent.TimeoutException || 
                        ex.getMessage().contains("超时")) {
                        String errorMsg = firstTokenReceived.get() ? 
                            "处理超时，请稍后重试" : "服务响应超时，请检查网络连接或稍后重试";
                        writeLine.accept("data:{\"code\":408,\"success\":false,\"msg\":\"" + errorMsg + "\",\"data\":null}\n\n");
                        return Flux.empty();
                    }
                    return Flux.error(ex);
                })
                .blockLast(Duration.ofSeconds(totalTimeoutSeconds + 5)); // 额外的阻塞超时
            record.setSuccess(true).setRespContent(respBuf.toString());
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            if (ex instanceof java.util.concurrent.TimeoutException || 
                errorMessage.contains("超时")) {
                errorMessage = firstTokenReceived.get() ? 
                    "处理超时" : "等待服务响应超时";
                writeLine.accept("data:{\"code\":408,\"success\":false,\"msg\":\"" + errorMessage + "\",\"data\":null}\n\n");
            } else {
                writeLine.accept("data:{\"code\":500,\"success\":false,\"msg\":\"服务异常！\",\"data\":null}\n\n");
            }
            record.setSuccess(false).setErrorMessage(errorMessage);
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


