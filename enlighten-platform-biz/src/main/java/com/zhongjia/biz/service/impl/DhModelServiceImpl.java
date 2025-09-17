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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DhModelServiceImpl implements DhModelService {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDhModelRepository userDhModelRepository;

    @Autowired
    private WebClient webClient;

    @Value("${app.upstream.dh-models-url:http://127.0.0.1:57599/dh/models}")
    private String dhModelsUrl;

    @Value("${app.upstream.dh-train-url:http://127.0.0.1:57599/dh/train}")
    private String dhTrainUrl;

    @Value("${app.dh.default-models:}")
    private String defaultModels;

    @Override
    public List<String> listModelsForUser(Long userId) {
        Set<String> models = new LinkedHashSet<>();

        // 1) 默认模型（配置）
        if (defaultModels != null && !defaultModels.isBlank()) {
            for (String m : defaultModels.split(",")) {
                String name = m.trim();
                if (!name.isEmpty()) models.add(name);
            }
        }

        // 2) 用户自训练模型（DB）
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
        // 目标：仅返回“默认模型 + 用户自有模型”的集合；上游仅用于补全详情
        // 顺序：默认 -> 用户；若上游没有该模型的详情，则返回最小对象，仅包含 name。
        try {
            // 1) 构造最终名称集合：默认 -> 用户
            LinkedHashSet<String> finalNames = new LinkedHashSet<>();
            if (defaultModels != null && !defaultModels.isBlank()) {
                for (String m : defaultModels.split(",")) {
                    String name = m.trim();
                    if (!name.isEmpty()) finalNames.add(name);
                }
            }
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

            // 2) 拉取上游详情列表，用于补全（0916变更：改为 multipart/form-data，参数 user_id）
            Map<String, JsonNode> nameToNode = new LinkedHashMap<>();
            try {
                MultipartBodyBuilder mb = new MultipartBodyBuilder();
                mb.part("user_id", String.valueOf(userId));

                String upstreamBody = webClient.post()
                        .uri(dhModelsUrl)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(mb.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (upstreamBody != null) {
                    List<JsonNode> upstreamNodes = extractModelObjects(upstreamBody);
                    for (JsonNode n : upstreamNodes) {
                        String name = pickModelName(n);
                        if (name != null && !name.isEmpty() && !nameToNode.containsKey(name)) {
                            nameToNode.put(name, n);
                        }
                    }
                } else {
                    log.warn("获取上游模型详细列表失败: 上游无响应");
                }
            } catch (Exception e) {
                log.error("调用上游模型详细列表异常", e);
            }

            // 3) 根据名称集合生成详情列表
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
            log.error("组装模型详细列表异常", e);
            return java.util.Collections.emptyList();
        }
    }


    @Override
    public UpstreamResult trainWithFile(Long userId, String modelName, MultipartFile file) {
        UpstreamResult result = new UpstreamResult();
        if (modelName == null || modelName.isBlank()) {
            return result.setCode(400).setSuccess(false).setMsg("model_name 不能为空");
        }
        if (file == null || file.isEmpty()) {
            return result.setCode(400).setSuccess(false).setMsg("file 不能为空");
        }

        try {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : (modelName + ".mp4");
            MediaType partContentType = file.getContentType() != null ? MediaType.parseMediaType(file.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;

            MultipartBodyBuilder mb = new MultipartBodyBuilder();
            mb.part("model_name", modelName);
            mb.part("user_id", String.valueOf(userId));
            mb.part("file", file.getResource())
                    .filename(filename)
                    .contentType(partContentType);

            String traceId = org.slf4j.MDC.get("traceId");

            String body = webClient.post()
                    .uri(dhTrainUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .headers(h -> { if (traceId != null) h.add("X-Trace-Id", traceId); })
                    .body(BodyInserters.fromMultipartData(mb.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (body == null) {
                return result.setCode(502).setSuccess(false).setMsg("上游无响应");
            }

            try {
                JsonNode root = objectMapper.readTree(body);
                Integer code = root.has("code") && root.get("code").isInt() ? root.get("code").asInt() : 200;
                Boolean success = root.has("success") && root.get("success").isBoolean() ? root.get("success").asBoolean() : true;
                String msg = root.has("msg") ? root.get("msg").asText() : "ok";
                String dataRaw = root.has("data") && !root.get("data").isNull() ? objectMapper.writeValueAsString(root.get("data")) : null;

                result.setCode(code).setSuccess(success).setMsg(msg).setDataRaw(dataRaw);
                if (Boolean.TRUE.equals(success)) {
                    saveUserModelIfAbsent(userId, modelName);
                }
            } catch (Exception parseEx) {
                result.setCode(200).setSuccess(true).setMsg("ok").setDataRaw(body);
                saveUserModelIfAbsent(userId, modelName);
            }
        } catch (Exception e) {
            log.error("训练(文件)请求异常", e);
            result.setCode(500).setSuccess(false).setMsg(e.getMessage());
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

    
}


