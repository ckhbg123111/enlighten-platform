package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.entity.ScienceGenRecord;
import com.zhongjia.biz.repository.ScienceGenRecordRepository;
import com.zhongjia.biz.service.ScienceGenRecordService;
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
import java.util.function.Consumer;

@Service
public class ScienceGenRecordServiceImpl implements ScienceGenRecordService {

    private final ScienceGenRecordRepository scienceGenRecordRepository;

    public ScienceGenRecordServiceImpl(ScienceGenRecordRepository scienceGenRecordRepository) {
        this.scienceGenRecordRepository = scienceGenRecordRepository;
    }
    @org.springframework.beans.factory.annotation.Value("${app.upstream.science-generator-url:http://192.168.1.65:8000/science-generator}")
    private String upstreamUrl;

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
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMinutes(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(upstreamBody, StandardCharsets.UTF_8))
                .build();
            HttpResponse<java.io.InputStream> upstream = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
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


