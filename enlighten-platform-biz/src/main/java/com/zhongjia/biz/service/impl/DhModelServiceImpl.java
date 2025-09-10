package com.zhongjia.biz.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.UserDhModel;
import com.zhongjia.biz.repository.UserDhModelRepository;
import com.zhongjia.biz.service.DhModelService;
import com.zhongjia.biz.service.dto.UpstreamResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DhModelServiceImpl implements DhModelService {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDhModelRepository userDhModelRepository;

    @Value("${app.upstream.dh-models-url:http://127.0.0.1:57599/dh/models}")
    private String dhModelsUrl;

    @Value("${app.upstream.dh-train-url:http://127.0.0.1:57599/dh/train}")
    private String dhTrainUrl;

    @Value("${app.dh.default-models:}")
    private String defaultModels;

    @Override
    public List<String> listModelsForUser(Long userId) {
        Set<String> models = new LinkedHashSet<>();

        // 1) 上游模型列表
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dhModelsUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200 && response.body() != null) {
                models.addAll(extractModelNames(response.body()));
            } else {
                log.warn("获取上游模型列表失败: status={}, body={}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("调用上游模型列表异常", e);
        }

        // 2) 默认模型（配置）
        if (defaultModels != null && !defaultModels.isBlank()) {
            for (String m : defaultModels.split(",")) {
                String name = m.trim();
                if (!name.isEmpty()) models.add(name);
            }
        }

        // 3) 用户自训练模型（DB）
        try {
            List<UserDhModel> userModels = userDhModelRepository.lambdaQuery()
                    .eq(UserDhModel::getUserId, userId)
                    .list();
            for (UserDhModel um : userModels) {
                if (um.getModelName() != null && !um.getModelName().isEmpty()) {
                    models.add(um.getModelName());
                }
            }
        } catch (Exception e) {
            log.error("查询用户自训练模型失败", e);
        }

        return new ArrayList<>(models);
    }

    @Override
    public List<java.util.Map<String, Object>> listModelDetailsForUser(Long userId) {
        // 目标：保持与字符串列表相同的合并顺序与筛选逻辑：上游 -> 默认 -> 用户
        // 返回详情对象；若某模型仅存在于默认/用户集合而不在上游列表中，则返回最小对象，仅包含 model_name。
        try {
            // 1) 拉取上游详情列表
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dhModelsUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200 || response.body() == null) {
                log.warn("获取上游模型详细列表失败: status={}, body={}", response.statusCode(), response.body());
                return java.util.Collections.emptyList();
            }

            List<JsonNode> upstreamNodes = extractModelObjects(response.body());
            // name -> node 映射，便于根据名称取详情
            Map<String, JsonNode> nameToNode = new LinkedHashMap<>();
            for (JsonNode n : upstreamNodes) {
                String name = pickModelName(n);
                if (name != null && !name.isEmpty() && !nameToNode.containsKey(name)) {
                    nameToNode.put(name, n);
                }
            }

            // 2) 组装最终名称集合（顺序：上游 -> 默认 -> 用户），保持与原字符串接口一致
            LinkedHashSet<String> finalNames = new LinkedHashSet<>();
            // 上游名称
            finalNames.addAll(nameToNode.keySet());
            // 默认模型
            if (defaultModels != null && !defaultModels.isBlank()) {
                for (String m : defaultModels.split(",")) {
                    String name = m.trim();
                    if (!name.isEmpty()) finalNames.add(name);
                }
            }
            // 用户自训练模型
            try {
                List<UserDhModel> userModels = userDhModelRepository.lambdaQuery()
                        .eq(UserDhModel::getUserId, userId)
                        .list();
                for (UserDhModel um : userModels) {
                    if (um.getModelName() != null && !um.getModelName().isEmpty()) {
                        finalNames.add(um.getModelName());
                    }
                }
            } catch (Exception e) {
                log.error("查询用户自训练模型失败", e);
            }

            // 3) 根据名称集合生成详情列表：优先使用上游详情，不存在则返回最小对象
            List<java.util.Map<String, Object>> result = new ArrayList<>(finalNames.size());
            for (String name : finalNames) {
                JsonNode n = nameToNode.get(name);
                if (n != null) {
                    result.add(objectMapper.convertValue(n, java.util.Map.class));
                } else {
                    java.util.Map<String, Object> minimal = new LinkedHashMap<>();
                    minimal.put("name", name);
                    result.add(minimal);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("调用上游模型详细列表异常", e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public UpstreamResult trainAndRecord(Long userId, String requestJson) {
        String modelName = extractModelNameFromRequest(requestJson);
        UpstreamResult result = new UpstreamResult();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dhTrainUrl))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/json")
                    .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return result.setCode(response.statusCode()).setSuccess(false).setMsg("上游返回非200").setDataRaw(null);
            }

            // 解析上游通用结构 { code, success, msg, data }
            try {
                JsonNode root = objectMapper.readTree(response.body());
                Integer code = root.has("code") && root.get("code").isInt() ? root.get("code").asInt() : 200;
                Boolean success = root.has("success") && root.get("success").isBoolean() ? root.get("success").asBoolean() : true;
                String msg = root.has("msg") ? root.get("msg").asText() : "ok";
                String dataRaw = root.has("data") && !root.get("data").isNull() ? objectMapper.writeValueAsString(root.get("data")) : null;

                result.setCode(code).setSuccess(success).setMsg(msg).setDataRaw(dataRaw);
                if (Boolean.TRUE.equals(success) && modelName != null && !modelName.isEmpty()) {
                    // 训练提交成功，写入用户-模型映射（幂等）
                    saveUserModelIfAbsent(userId, modelName);
                }
            } catch (Exception ignore) {
                // 如果上游不符合通用结构，则按成功处理并仍尝试写入映射
                result.setCode(200).setSuccess(true).setMsg("ok").setDataRaw(response.body());
                if (modelName != null && !modelName.isEmpty()) {
                    saveUserModelIfAbsent(userId, modelName);
                }
            }

        } catch (Exception e) {
            log.error("转发训练请求异常", e);
            result.setCode(500).setSuccess(false).setMsg(e.getMessage()).setDataRaw(null);
        }

        return result;
    }

    private void saveUserModelIfAbsent(Long userId, String modelName) {
        try {
            long count = userDhModelRepository.lambdaQuery()
                    .eq(UserDhModel::getUserId, userId)
                    .eq(UserDhModel::getModelName, modelName)
                    .count();
            if (count == 0) {
                userDhModelRepository.save(new UserDhModel()
                        .setUserId(userId)
                        .setModelName(modelName)
                        .setCreateTime(LocalDateTime.now()));
            }
        } catch (Exception e) {
            log.error("保存用户模型映射失败 userId={}, modelName={}", userId, modelName, e);
        }
    }

    private List<String> extractModelNames(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            List<String> names = new ArrayList<>();

            // 常见结构 1: { code, success, data: [ { model_name: "..." } ] }
            JsonNode dataNode = root.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode n : dataNode) {
                    String name = pickModelName(n);
                    if (name != null && !name.isEmpty()) names.add(name);
                }
                return names;
            }

            // 常见结构 2: 顶层数组 [ { model_name: ... } ]
            if (root.isArray()) {
                for (JsonNode n : root) {
                    String name = pickModelName(n);
                    if (name != null && !name.isEmpty()) names.add(name);
                }
                return names;
            }

            // 兜底：尝试 data.models / models
            JsonNode modelsNode = root.path("models");
            if (modelsNode.isArray()) {
                for (JsonNode n : modelsNode) {
                    String name = pickModelName(n);
                    if (name != null && !name.isEmpty()) names.add(name);
                }
            }
            return names;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<JsonNode> extractModelObjects(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            List<JsonNode> items = new ArrayList<>();

            // 常见结构 1: { code, success, data: [ {...}, {...} ] }
            JsonNode dataNode = root.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode n : dataNode) items.add(n);
                return items;
            }

            // 常见结构 2: 顶层数组 [ {...}, {...} ]
            if (root.isArray()) {
                for (JsonNode n : root) items.add(n);
                return items;
            }

            // 兜底：尝试 data.models / models
            JsonNode modelsNode = root.path("models");
            if (modelsNode.isArray()) {
                for (JsonNode n : modelsNode) items.add(n);
            }
            return items;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private String pickModelName(JsonNode node) {
        if (node == null) return null;
        if (node.isTextual()) return node.asText();
        if (node.has("model_name")) return node.get("model_name").asText();
        if (node.has("name")) return node.get("name").asText();
        return null;
    }

    private String extractModelNameFromRequest(String requestJson) {
        try {
            JsonNode root = objectMapper.readTree(requestJson);
            if (root.has("model_name")) return root.get("model_name").asText();
        } catch (Exception ignored) {}
        return null;
    }
}


