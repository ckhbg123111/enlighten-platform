package com.zhongjia.biz.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import com.zhongjia.biz.service.VideoGenerationService;
import com.zhongjia.biz.service.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 视频生成服务实现类
 */
@Slf4j
@Service
public class VideoGenerationServiceImpl implements VideoGenerationService {
    
    private final VideoGenerationTaskRepository taskRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${app.upstream.dh-generate-url:http://frp5.mmszxc.xin:57599/dh/generate}")
    private String dhGenerateUrl;
    
    @Value("${app.upstream.dh-status-url:http://frp5.mmszxc.xin:57599/dh/status}")
    private String dhStatusUrl;
    
    @Value("${app.upstream.subtitle-burn-url:http://localhost:8081/api/subtitles/burn-url-srt/async}")
    private String subtitleBurnUrl;
    
    @Value("${app.upstream.subtitle-status-url:http://localhost:8081/api/subtitles/task}")
    private String subtitleStatusUrl;
    
    @Autowired
    public VideoGenerationServiceImpl(VideoGenerationTaskRepository taskRepository, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String createTask(Long userId, String inputText, String modelName, String voice) {
        log.info("创建视频生成任务 - 用户: {}, 文本长度: {}", userId, inputText.length());
        
        // 创建任务记录
        VideoGenerationTask task = new VideoGenerationTask()
                .setUserId(userId)
                .setInputText(inputText)
                .setModelName(modelName)
                .setVoice(voice != null ? voice : "Female_Voice_1")
                .setStatus("CREATED")
                .setProgress(0)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        
        taskRepository.save(task);
        
        log.info("视频生成任务创建成功 - 任务ID: {}", task.getId());
        return task.getId();
    }
    
    @Override
    public VideoGenerationTask getTaskStatus(String taskId, Long userId) {
        VideoGenerationTask task = taskRepository.getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        // 权限校验
        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问该任务");
        }
        
        return task;
    }
    
    @Override
    public void processDhPhase(VideoGenerationTask task) {
        try {
            log.info("开始处理数字人阶段 - 任务ID: {}", task.getId());
            
            // 调用数字人生成接口
            DigitalHumanRequest request = new DigitalHumanRequest()
                    .setModelName(task.getModelName())
                    .setText(task.getInputText())
                    .setVoice(task.getVoice());
            
            DigitalHumanResponse response = callDhGenerate(request);
            
            if (response.getSuccess() && response.getData() != null) {
                // 更新任务信息
                task.setDhTaskId(response.getData().getTaskId())
                    .setAudioData(objectMapper.writeValueAsString(response.getData().getAudioData()))
                    .setStatus("DH_PROCESSING")
                    .setProgress(5);
                
                updateTaskStatus(task, "DH_PROCESSING", 5, null);
                log.info("数字人任务提交成功 - 任务ID: {}, DH任务ID: {}", task.getId(), task.getDhTaskId());
            } else {
                updateTaskStatus(task, "FAILED", 0, "数字人任务提交失败: " + response.getMsg());
                log.error("数字人任务提交失败 - 任务ID: {}, 错误: {}", task.getId(), response.getMsg());
            }
            
        } catch (Exception e) {
            log.error("处理数字人阶段异常 - 任务ID: {}", task.getId(), e);
            updateTaskStatus(task, "FAILED", 0, "数字人阶段异常: " + e.getMessage());
        }
    }
    
    @Override
    public void processBurnPhase(VideoGenerationTask task) {
        try {
            log.info("开始处理字幕烧录阶段 - 任务ID: {}", task.getId());
            
            // 生成SRT字幕文件
            String srtContent = generateSrtContent(task.getAudioData());
            File srtFile = createTempSrtFile(srtContent);
            
            // 调用字幕烧录接口
            SubtitleBurnResponse response = callSubtitleBurn(task.getDhResultUrl(), srtFile);
            
            if (response.getSuccess() && response.getTaskId() != null) {
                // 更新任务信息
                task.setBurnTaskId(response.getTaskId())
                    .setStatus("BURN_PROCESSING")
                    .setProgress(65);
                
                updateTaskStatus(task, "BURN_PROCESSING", 65, null);
                log.info("字幕烧录任务提交成功 - 任务ID: {}, 烧录任务ID: {}", task.getId(), task.getBurnTaskId());
            } else {
                updateTaskStatus(task, "FAILED", 60, "字幕烧录任务提交失败: " + response.getMessage());
                log.error("字幕烧录任务提交失败 - 任务ID: {}, 错误: {}", task.getId(), response.getMessage());
            }
            
            // 清理临时文件
            if (srtFile.exists()) {
                srtFile.delete();
            }
            
        } catch (Exception e) {
            log.error("处理字幕烧录阶段异常 - 任务ID: {}", task.getId(), e);
            updateTaskStatus(task, "FAILED", 60, "字幕烧录阶段异常: " + e.getMessage());
        }
    }
    
    @Override
    public boolean pollDhStatus(VideoGenerationTask task) {
        try {
            DigitalHumanStatusResponse response = callDhStatus(task.getDhTaskId());
            
            if (response.getSuccess() && response.getData() != null) {
                DigitalHumanStatusResponse.DhStatusData data = response.getData();
                
                // 更新进度 (5-60)
                int progress = Math.min(60, 5 + (data.getProgress() != null ? data.getProgress() * 55 / 100 : 0));
                
                if ("COMPLETED".equals(data.getStatus()) && data.getResultUrl() != null) {
                    // 数字人阶段完成
                    task.setDhResultUrl(data.getResultUrl())
                        .setDhStatus("COMPLETED")
                        .setStatus("DH_DONE")
                        .setProgress(60);
                    
                    updateTaskStatus(task, "DH_DONE", 60, null);
                    log.info("数字人任务完成 - 任务ID: {}, 视频URL: {}", task.getId(), data.getResultUrl());
                    return true;
                    
                } else if ("FAILED".equals(data.getStatus())) {
                    // 数字人阶段失败
                    updateTaskStatus(task, "FAILED", progress, "数字人生成失败: " + data.getErrorMessage());
                    log.error("数字人任务失败 - 任务ID: {}, 错误: {}", task.getId(), data.getErrorMessage());
                    return true;
                    
                } else {
                    // 仍在处理中
                    task.setDhStatus(data.getStatus()).setProgress(progress);
                    updateTaskStatus(task, "DH_PROCESSING", progress, null);
                    return false;
                }
            } else {
                log.warn("数字人状态查询失败 - 任务ID: {}, 响应: {}", task.getId(), response.getMsg());
                return false;
            }
            
        } catch (Exception e) {
            log.error("轮询数字人状态异常 - 任务ID: {}", task.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean pollBurnStatus(VideoGenerationTask task) {
        try {
            SubtitleBurnStatusResponse response = callSubtitleStatus(task.getBurnTaskId());
            
            if (response.getSuccess() && response.getData() != null) {
                SubtitleBurnStatusResponse.BurnStatusData data = response.getData();
                
                // 更新进度 (65-100)
                int progress = Math.min(100, 65 + (data.getProgress() != null ? data.getProgress() * 35 / 100 : 0));
                
                if ("COMPLETED".equals(data.getState()) && data.getOutputUrl() != null) {
                    // 字幕烧录完成
                    task.setOutputUrl(data.getOutputUrl())
                        .setBurnStatus("COMPLETED")
                        .setStatus("COMPLETED")
                        .setProgress(100);
                    
                    updateTaskStatus(task, "COMPLETED", 100, null);
                    log.info("字幕烧录任务完成 - 任务ID: {}, 输出URL: {}", task.getId(), data.getOutputUrl());
                    return true;
                    
                } else if ("FAILED".equals(data.getState())) {
                    // 字幕烧录失败
                    updateTaskStatus(task, "FAILED", progress, "字幕烧录失败: " + data.getErrorMessage());
                    log.error("字幕烧录任务失败 - 任务ID: {}, 错误: {}", task.getId(), data.getErrorMessage());
                    return true;
                    
                } else {
                    // 仍在处理中
                    task.setBurnStatus(data.getState()).setProgress(progress);
                    updateTaskStatus(task, "BURN_PROCESSING", progress, null);
                    return false;
                }
            } else {
                log.warn("字幕烧录状态查询失败 - 任务ID: {}, 响应: {}", task.getId(), response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("轮询字幕烧录状态异常 - 任务ID: {}", task.getId(), e);
            return false;
        }
    }
    
    @Override
    public void updateTaskStatus(VideoGenerationTask task, String status, Integer progress, String errorMessage) {
        task.setStatus(status)
            .setProgress(progress)
            .setErrorMessage(errorMessage)
            .setUpdatedAt(LocalDateTime.now());
        
        taskRepository.updateById(task);
    }
    
    // 私有方法
    
    private DigitalHumanResponse callDhGenerate(DigitalHumanRequest request) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        
        String body = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(dhGenerateUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        
        if (response.statusCode() != 200) {
            throw new IllegalStateException("数字人接口返回非200状态码: " + response.statusCode());
        }
        
        return objectMapper.readValue(response.body(), DigitalHumanResponse.class);
    }
    
    private DigitalHumanStatusResponse callDhStatus(String taskId) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(dhStatusUrl + "/" + taskId))
                .timeout(Duration.ofSeconds(30))
                .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        
        if (response.statusCode() != 200) {
            throw new IllegalStateException("数字人状态查询接口返回非200状态码: " + response.statusCode());
        }
        
        return objectMapper.readValue(response.body(), DigitalHumanStatusResponse.class);
    }
    
    private SubtitleBurnResponse callSubtitleBurn(String videoUrl, File srtFile) throws Exception {
        // 这里简化实现，实际项目中可能需要使用 Spring 的 RestTemplate 或 WebClient
        // 来处理 multipart/form-data 请求
        
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        
        // 构建 multipart 请求体 (简化版)
        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
        String srtContent = Files.readString(srtFile.toPath());
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("--").append(boundary).append("\r\n");
        bodyBuilder.append("Content-Disposition: form-data; name=\"taskId\"\r\n\r\n");
        bodyBuilder.append(UUID.randomUUID().toString()).append("\r\n");
        
        bodyBuilder.append("--").append(boundary).append("\r\n");
        bodyBuilder.append("Content-Disposition: form-data; name=\"videoUrl\"\r\n\r\n");
        bodyBuilder.append(videoUrl).append("\r\n");
        
        bodyBuilder.append("--").append(boundary).append("\r\n");
        bodyBuilder.append("Content-Disposition: form-data; name=\"subtitleFile\"; filename=\"subtitle.srt\"\r\n");
        bodyBuilder.append("Content-Type: text/plain\r\n\r\n");
        bodyBuilder.append(srtContent).append("\r\n");
        
        bodyBuilder.append("--").append(boundary).append("--\r\n");
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(subtitleBurnUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                .POST(HttpRequest.BodyPublishers.ofString(bodyBuilder.toString(), StandardCharsets.UTF_8))
                .build();
        
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        
        if (response.statusCode() != 200) {
            throw new IllegalStateException("字幕烧录接口返回非200状态码: " + response.statusCode());
        }
        
        return objectMapper.readValue(response.body(), SubtitleBurnResponse.class);
    }
    
    private SubtitleBurnStatusResponse callSubtitleStatus(String taskId) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(subtitleStatusUrl + "/" + taskId))
                .timeout(Duration.ofSeconds(30))
                .header("X-Trace-Id", org.slf4j.MDC.get("traceId"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        
        if (response.statusCode() != 200) {
            throw new IllegalStateException("字幕状态查询接口返回非200状态码: " + response.statusCode());
        }
        
        return objectMapper.readValue(response.body(), SubtitleBurnStatusResponse.class);
    }
    
    private String generateSrtContent(String audioDataJson) throws Exception {
        List<DigitalHumanResponse.AudioData> audioDataList = objectMapper.readValue(
                audioDataJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, DigitalHumanResponse.AudioData.class)
        );
        
        StringBuilder srtBuilder = new StringBuilder();
        for (int i = 0; i < audioDataList.size(); i++) {
            DigitalHumanResponse.AudioData audioData = audioDataList.get(i);
            
            // SRT格式:
            // 序号
            // 时间行 (start --> end)
            // 文本行
            // 空行
            srtBuilder.append(i + 1).append("\n");
            srtBuilder.append(audioData.getTime().replace(" --> ", " --> ")).append("\n");
            srtBuilder.append(audioData.getText()).append("\n");
            srtBuilder.append("\n");
        }
        
        return srtBuilder.toString();
    }
    
    private File createTempSrtFile(String srtContent) throws Exception {
        File tempFile = File.createTempFile("subtitle_" + System.currentTimeMillis(), ".srt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(srtContent.getBytes(StandardCharsets.UTF_8));
        }
        return tempFile;
    }
}
