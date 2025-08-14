package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.entity.FillInRecord;
import com.zhongjia.biz.repository.FillInRecordRepository;
import com.zhongjia.biz.service.FillInRecordService;
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

@Service
public class FillInRecordServiceImpl implements FillInRecordService {

    private final FillInRecordRepository fillInRecordRepository;

    public FillInRecordServiceImpl(FillInRecordRepository fillInRecordRepository) {
        this.fillInRecordRepository = fillInRecordRepository;
    }
    @org.springframework.beans.factory.annotation.Value("${app.upstream.fill-in-url:http://192.168.1.65:8000/fill_in}")
    private String upstreamUrl;

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
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            String body = "{" + "\"content\":\"" + escapeJson(content) + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMinutes(10))
                .header("Content-Type", "application/json")
                .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

            HttpResponse<java.io.InputStream> upstream = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (upstream.statusCode() != 200) {
                throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(upstream.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String out = line + "\n\n";
                        writeLine.accept(out);
                        respBuf.append(extractContentDelta(line));
                    }
                }
            }
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


