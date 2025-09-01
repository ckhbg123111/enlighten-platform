package com.zhongjia.biz.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.ScienceChatRecord;
import com.zhongjia.biz.repository.ScienceChatRecordRepository;
import com.zhongjia.biz.service.ScienceChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class ScienceChatServiceImpl implements ScienceChatService {

    @Autowired
    private ScienceChatRecordRepository chatRecordRepository;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private HttpClient httpClient;
    
    @Autowired
    private WebClient webClient;

    @Value("${app.upstream.science-chat-url:http://192.168.1.65:8000/science-chat}")
    private String upstreamUrl;

    @Value("${app.chat.history-limit:10}")
    private int defaultHistoryLimit;

    @Value("${app.chat.redis-ttl-seconds:86400}")
    private long redisTtlSeconds;

    @Override
    public String streamChat(Long userId,
                             String sessionId,
                             String messagesJson,
                             Boolean needRecommend,
                             String prompt,
                             int historyLimit,
                             Consumer<String> sseWriter) {
        int limit = historyLimit > 0 ? historyLimit : defaultHistoryLimit;
        // 缓存 Key
        String redisKey = buildRedisKey(userId, sessionId);

        // 合并历史
        String mergedMessages = mergeHistory(redisKey, messagesJson, limit);

        // 先落库请求
        ScienceChatRecord record = new ScienceChatRecord()
                .setUserId(userId)
                .setSessionId(sessionId)
                .setReqMessages(messagesJson)
                .setNeedRecommend(needRecommend)
                .setPrompt(prompt)
                .setCreateTime(LocalDateTime.now());
        chatRecordRepository.save(record);

        StringBuilder respBuf = new StringBuilder(256);
        try {
            String body = buildUpstreamBody(mergedMessages, needRecommend, prompt);
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
                        sseWriter.accept(line + "\n\n");
                        String piece = extractContentDelta(line);
                        if (!piece.isEmpty()) {
                            respBuf.append(piece);
                        }
                    }
                })
                .blockLast();
            record.setSuccess(true).setRespContent(respBuf.toString());
        } catch (Exception ex) {
            record.setSuccess(false).setErrorMessage(ex.getMessage());
            sseWriter.accept("data: {\"type\":\"llm_token\",\"choices\":[{\"finish_reason\":\"null\",\"delta\":{\"role\":\"assistant\",\"content\":\"\"}}]}\n\n");
            sseWriter.accept("data: {\"type\":\"llm_token\",\"choices\":[{\"finish_reason\":\"stop\",\"delta\":{\"role\":\"assistant\",\"content\":\"\"}}]}\n\n");
        } finally {
            chatRecordRepository.updateById(record);
        }

        // 写入会话历史缓存（仅保留 user/assistant 对话消息，限制长度）
        tryAppendHistory(redisKey, messagesJson, respBuf.toString(), limit);
        return respBuf.toString();
    }

    private String buildRedisKey(Long userId, String sessionId) {
        return "chat:" + userId + ":" + sessionId;
    }

    private String mergeHistory(String redisKey, String latestMessagesJson, int limit) {
        try {
            List<JsonNode> history = new ArrayList<>();
            String cached = stringRedisTemplate.opsForValue().get(redisKey);
            if (cached != null && !cached.isEmpty()) {
                JsonNode arr = objectMapper.readTree(cached);
                if (arr.isArray()) {
                    for (JsonNode n : arr) history.add(n);
                }
            }
            JsonNode latestArr = objectMapper.readTree(latestMessagesJson);
            if (latestArr.isArray()) {
                for (JsonNode n : latestArr) history.add(n);
            }
            // 截断到 limit 条
            int from = Math.max(0, history.size() - limit);
            List<JsonNode> sliced = history.subList(from, history.size());
            return objectMapper.writeValueAsString(sliced);
        } catch (Exception e) {
            return latestMessagesJson;
        }
    }

    private void tryAppendHistory(String redisKey, String reqMessagesJson, String assistantText, int limit) {
        try {
            // 将本次用户消息与AI回复转为两条标准消息追加
            List<JsonNode> newItems = new ArrayList<>();
            JsonNode reqArr = objectMapper.readTree(reqMessagesJson);
            if (reqArr.isArray() && reqArr.size() > 0) {
                // 取最后一条作为最新用户输入
                newItems.add(reqArr.get(reqArr.size() - 1));
            }
            // AI 回复
            JsonNode assistant = objectMapper.readTree("{\"role\":\"assistant\",\"content\":" + objectMapper.writeValueAsString(assistantText) + "}");

            String cached = stringRedisTemplate.opsForValue().get(redisKey);
            List<JsonNode> merged = new ArrayList<>();
            if (cached != null && !cached.isEmpty()) {
                JsonNode arr = objectMapper.readTree(cached);
                if (arr.isArray()) for (JsonNode n : arr) merged.add(n);
            }
            merged.addAll(newItems);
            merged.add(assistant);

            int from = Math.max(0, merged.size() - limit);
            List<JsonNode> sliced = merged.subList(from, merged.size());
            stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(sliced), redisTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    private String buildUpstreamBody(String messagesJson, Boolean needRecommend, String prompt) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        sb.append("\"messages\":").append(messagesJson == null ? "[]" : messagesJson);
        if (needRecommend != null) {
            sb.append(",\"need_recommend\":").append(needRecommend ? "true" : "false");
        }
        if (prompt != null && !prompt.isEmpty()) {
            sb.append(",\"prompt\":").append('"').append(escapeJson(prompt)).append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String extractContentDelta(String dataLine) {
        // 仅在 type=llm_token 时解析 content
        if (!dataLine.contains("\"type\":\"llm_token\"")) return "";
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


