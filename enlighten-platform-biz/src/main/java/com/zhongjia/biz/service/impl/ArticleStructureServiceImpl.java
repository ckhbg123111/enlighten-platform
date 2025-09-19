package com.zhongjia.biz.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.service.ArticleStructureService;
import com.zhongjia.biz.service.dto.ArticleStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class ArticleStructureServiceImpl implements ArticleStructureService {

    @Autowired
    private HttpClient httpClient;

    @Value("${app.upstream.get-article-structure-url:http://192.168.1.65:8000/get_article_structure}")
    private String upstreamUrl;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public ArticleStructure parse(String essay) {
        try {
            String body = toBody(essay);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) {
                throw new IllegalStateException("上游返回非200:" + resp.statusCode());
            }
            UpstreamResult r = JSON.readValue(resp.body(), UpstreamResult.class);
            if (r == null || !Boolean.TRUE.equals(r.success) || r.data == null) {
                throw new IllegalStateException("上游失败:" + (r == null ? "null" : r.msg));
            }
            return JSON.convertValue(r.data, ArticleStructure.class);
        } catch (Exception e) {
            throw new RuntimeException("解析文章结构失败", e);
        }
    }

    private String toBody(String essay) {
        String content = essay == null ? "" : essay;
        // 根据接口文档，仅需传递 content 字段
        return "{" +
            "\"content\":\"" + escapeJson(content) + "\"" +
            "}";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private static class UpstreamResult {
        @SuppressWarnings("unused")
        public Integer code;
        public Boolean success;
        public String msg;
        public Object data;
    }
}


