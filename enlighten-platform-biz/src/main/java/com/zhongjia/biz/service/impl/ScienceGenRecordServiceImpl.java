package com.zhongjia.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhongjia.biz.entity.ScienceGenRecord;
import com.zhongjia.biz.repository.ScienceGenRecordRepository;
import com.zhongjia.biz.service.ScienceGenRecordService;
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
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class ScienceGenRecordServiceImpl implements ScienceGenRecordService {

    private final ScienceGenRecordRepository scienceGenRecordRepository;

    private final HttpClient httpClient;
    private final WebClient webClient;

    public ScienceGenRecordServiceImpl(ScienceGenRecordRepository scienceGenRecordRepository, HttpClient httpClient, WebClient webClient) {
        this.scienceGenRecordRepository = scienceGenRecordRepository;
        this.httpClient = httpClient;
        this.webClient = webClient;
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
            final String traceId = org.slf4j.MDC.get("traceId");
            final StringBuilder carry = new StringBuilder();

            webClient.post()
                .uri(upstreamUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Trace-Id", traceId == null ? "" : traceId)
                .bodyValue(upstreamBody)
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
            final String traceId2 = org.slf4j.MDC.get("traceId");
            final StringBuilder carry2 = new StringBuilder();

            webClient.post()
                .uri(regenerateUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Trace-Id", traceId2 == null ? "" : traceId2)
                .bodyValue(upstreamBody)
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
                    carry2.append(chunk);
                    java.util.List<String> lines = new java.util.ArrayList<>();
                    int idx;
                    while ((idx = carry2.indexOf("\n")) >= 0) {
                        String line = carry2.substring(0, idx);
                        if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
                        lines.add(line);
                        carry2.delete(0, idx + 1);
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


