package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.service.VideoGenerationService;
import com.zhongjia.biz.service.VideoGenerationMQService;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.req.VideoGenerateRequest;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.VideoGenerateResponse;
import com.zhongjia.web.vo.VideoStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * 视频生成控制器
 */
@Slf4j
@RestController
@Tag(name = "视频生成")
@RequestMapping("/api/video")
public class VideoGenerationController {

    @Value("${app.upstream.subtitle-download-url}")
    private String subtitleVideoDownloadBaseUrl;
    
    @Autowired
    private VideoGenerationService videoGenerationService;
    
    @Autowired
    private VideoGenerationMQService videoGenerationMQService;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 创建视频生成任务
     */
    @PostMapping("/generate")
    @Operation(summary = "创建视频生成任务", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<VideoGenerateResponse> generateVideo(@Valid @RequestBody VideoGenerateRequest request) {
        UserContext.UserInfo user = requireUser();
        
        try {
            // 使用MQ版本的服务
            String taskId = videoGenerationMQService.createTaskWithMQ(
                    user.userId(),
                    request.getText(),
                    request.getModelName(),
                    request.getVoice()
            );
            
            VideoGenerateResponse response = new VideoGenerateResponse()
                    .setTaskId(taskId)
                    .setMessage("视频生成任务创建成功");
            
            log.info("视频生成任务创建成功 - 用户: {}, 任务ID: {}", user.userId(), taskId);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("创建视频生成任务失败 - 用户: {}", user.userId(), e);
            return Result.error(500, "创建视频生成任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除视频生成任务(软删除)
     */
    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "删除视频生成任务(软删除)", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> softDeleteTask(@Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        UserContext.UserInfo user = requireUser();
        try {
            com.zhongjia.biz.service.VideoGenerationMQService.SoftDeleteResult r =
                    videoGenerationMQService.softDeleteById(user.userId(), taskId);
            switch (r) {
                case SUCCESS:
                    return Result.success(true);
                case FORBIDDEN:
                    return Result.error(403, "无权限");
                case NOT_FOUND:
                case ALREADY_DELETED:
                    return Result.error(404, "任务不存在");
                case FAILED:
                default:
                    return Result.error(500, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除视频任务异常 - 用户: {}, 任务ID: {}", user.userId(), taskId, e);
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 下载视频结果（返回直链，不重定向）
     */
    @GetMapping("/download2/{taskId}")
    @Operation(summary = "下载视频结果(返回直链)", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<java.util.Map<String, String>> downloadVideoLink(
            @Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        UserContext.UserInfo user = requireUser();
        
        try {
            VideoGenerationTask task = videoGenerationService.getTaskStatus(taskId, user.userId());
            
            if (!"COMPLETED".equals(task.getStatus())) {
                return Result.error(400, "任务尚未完成");
            }
            
            if (task.getOutputUrl() == null || task.getOutputUrl().isEmpty()) {
                return Result.error(404, "视频文件不存在");
            }
            
            String directUrl = subtitleVideoDownloadBaseUrl + "?path=" + task.getOutputUrl();
            log.info("视频直链返回 - 用户: {}, 任务ID: {}, URL: {}", user.userId(), taskId, directUrl);
            return Result.success(java.util.Map.of("url", directUrl));
            
        } catch (IllegalArgumentException e) {
            log.warn("下载视频直链失败 - 用户: {}, 任务ID: {}, 错误: {}", user.userId(), taskId, e.getMessage());
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("下载视频直链异常 - 用户: {}, 任务ID: {}", user.userId(), taskId, e);
            return Result.error(500, "下载失败");
        }
    }
    
    /**
     * 查询视频生成任务状态
     */
    @GetMapping("/status/{taskId}")
    @Operation(summary = "查询视频生成任务状态", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<VideoStatusResponse> getVideoStatus(
            @Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        UserContext.UserInfo user = requireUser();
        
        try {
            VideoGenerationTask task = videoGenerationService.getTaskStatus(taskId, user.userId());
            
            VideoStatusResponse response = new VideoStatusResponse()
                    .setTaskId(task.getId())
                    .setVideoName(task.getVideoName())
                    .setStatus(task.getStatus())
                    .setProgress(task.getProgress())
                    .setResultUrl(task.getOutputUrl())
                    .setMessage(task.getErrorMessage() != null ? task.getErrorMessage() : getStatusMessage(task.getStatus()))
                    .setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().format(DATETIME_FORMATTER) : null)
                    .setUpdatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().format(DATETIME_FORMATTER) : null);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("查询视频任务状态失败 - 用户: {}, 任务ID: {}, 错误: {}", user.userId(), taskId, e.getMessage());
            return Result.error(404, e.getMessage());
            
        } catch (Exception e) {
            log.error("查询视频任务状态异常 - 用户: {}, 任务ID: {}", user.userId(), taskId, e);
            return Result.error(500, "查询任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载视频结果 (重定向到实际URL)
     */
    @GetMapping("/download/{taskId}")
    @Operation(summary = "下载视频结果", security = {@SecurityRequirement(name = "bearer-jwt")})
    public void downloadVideo(
            @Parameter(description = "任务ID") @PathVariable("taskId") String taskId,
            HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();
        
        try {
            VideoGenerationTask task = videoGenerationService.getTaskStatus(taskId, user.userId());
            
            if (!"COMPLETED".equals(task.getStatus())) {
                response.setStatus(400);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":400,\"success\":false,\"msg\":\"任务尚未完成\",\"data\":null}");
                return;
            }
            
            if (task.getOutputUrl() == null || task.getOutputUrl().isEmpty()) {
                response.setStatus(404);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":404,\"success\":false,\"msg\":\"视频文件不存在\",\"data\":null}");
                return;
            }
            
            // 重定向到实际的视频URL
            response.sendRedirect(subtitleVideoDownloadBaseUrl + "?path=" + task.getOutputUrl());
            log.info("视频下载重定向 - 用户: {}, 任务ID: {}, URL: {}", user.userId(), taskId, task.getOutputUrl());
            
        } catch (IllegalArgumentException e) {
            log.warn("下载视频失败 - 用户: {}, 任务ID: {}, 错误: {}", user.userId(), taskId, e.getMessage());
            response.setStatus(404);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":404,\"success\":false,\"msg\":\"" + escapeJson(e.getMessage()) + "\",\"data\":null}");
            
        } catch (Exception e) {
            log.error("下载视频异常 - 用户: {}, 任务ID: {}", user.userId(), taskId, e);
            response.setStatus(500);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":500,\"success\":false,\"msg\":\"下载失败\",\"data\":null}");
        }
    }
    
    /**
     * 获取用户的视频任务列表
     */
    @GetMapping("/tasks")
    @Operation(summary = "获取用户的视频任务列表", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<java.util.List<VideoStatusResponse>> getUserTasks(
            @RequestParam(value = "statuses", required = false) java.util.List<String> statuses) {
        UserContext.UserInfo user = requireUser();
        
        try {
            java.util.List<com.zhongjia.biz.entity.VideoGenerationTask> tasks =
                    videoGenerationMQService.listTasksByUserAndStatuses(user.userId(), statuses);
            java.util.List<VideoStatusResponse> list = new java.util.ArrayList<>(tasks.size());
            for (com.zhongjia.biz.entity.VideoGenerationTask t : tasks) {
                VideoStatusResponse dto = new VideoStatusResponse()
                        .setTaskId(t.getId())
                        .setVideoName(t.getVideoName())
                        .setStatus(t.getStatus())
                        .setProgress(t.getProgress())
                        .setResultUrl(t.getOutputUrl())
                        .setMessage(t.getErrorMessage())
                        .setCreatedAt(t.getCreatedAt() == null ? null : t.getCreatedAt().format(DATETIME_FORMATTER))
                        .setUpdatedAt(t.getUpdatedAt() == null ? null : t.getUpdatedAt().format(DATETIME_FORMATTER));
                list.add(dto);
            }
            return Result.success(list);
            
        } catch (Exception e) {
            log.error("获取用户视频任务列表异常 - 用户: {}", user.userId(), e);
            return Result.error(500, "获取任务列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息并进行权限校验
     */
    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return info;
    }
    
    /**
     * 根据状态获取友好的状态消息
     */
    private String getStatusMessage(String status) {
        if (status == null) return "状态未知";
        
        switch (status) {
            case "CREATED":
                return "任务已创建";
            case "DH_PROCESSING":
                return "数字人视频生成中...";
            case "DH_DONE":
                return "数字人视频生成完成，正在准备字幕烧录...";
            case "BURN_PROCESSING":
                return "字幕烧录中...";
            case "COMPLETED":
                return "视频生成完成";
            case "FAILED":
                return "任务执行失败";
            default:
                return "未知状态: " + status;
        }
    }
    
    /**
     * 转义JSON字符串
     */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
