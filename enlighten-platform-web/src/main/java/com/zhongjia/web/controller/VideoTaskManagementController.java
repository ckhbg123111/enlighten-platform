package com.zhongjia.web.controller;

import com.zhongjia.biz.service.VideoGenerationMQService;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 视频任务管理控制器 (仅管理员使用)
 */
@Slf4j
@RestController
@Tag(name = "视频任务管理")
@RequestMapping("/api/video/admin")
public class VideoTaskManagementController {
    
    @Autowired
    private VideoGenerationMQService videoGenerationMQService;
    
    /**
     * 手动触发任务处理 (管理员功能)
     */
    @PostMapping("/process/{taskId}")
    @Operation(summary = "手动触发任务处理", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<String> processTask(@Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        UserContext.UserInfo user = requireAdmin();
        
        try {
            // 使用MQ版本的重试功能
            videoGenerationMQService.retryTask(taskId);
            log.info("管理员手动触发任务处理(MQ版本) - 操作者: {}, 任务ID: {}", user.userId(), taskId);
            return Result.success("任务重试消息已发送到MQ");
            
        } catch (Exception e) {
            log.error("管理员手动触发任务处理失败 - 操作者: {}, 任务ID: {}", user.userId(), taskId, e);
            return Result.error(500, "触发任务处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务统计信息 (管理员功能)
     */
    @GetMapping("/stats")
    @Operation(summary = "获取任务统计信息", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Object> getTaskStats() {
        UserContext.UserInfo user = requireAdmin();
        
        try {
            // 使用MQ版本的统计功能
            VideoGenerationMQService.TaskStatistics stats = videoGenerationMQService.getTaskStatistics();
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("createdCount", stats.createdCount);
            result.put("dhProcessingCount", stats.dhProcessingCount);
            result.put("dhDoneCount", stats.dhDoneCount);
            result.put("burnProcessingCount", stats.burnProcessingCount);
            result.put("completedCount", stats.completedCount);
            result.put("failedCount", stats.failedCount);
            result.put("totalCount", stats.getTotalCount());
            result.put("successRate", String.format("%.2f%%", stats.getSuccessRate()));
            result.put("timestamp", stats.timestamp);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取任务统计信息失败 - 操作者: {}", user.userId(), e);
            return Result.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量重试失败的任务 (管理员功能)
     */
    @PostMapping("/retry-failed")
    @Operation(summary = "批量重试失败的任务", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<String> retryFailedTasks(@RequestParam(defaultValue = "10") int maxCount) {
        UserContext.UserInfo user = requireAdmin();
        
        try {
            videoGenerationMQService.retryFailedTasks(maxCount);
            log.info("管理员批量重试失败任务 - 操作者: {}, 最大数量: {}", user.userId(), maxCount);
            return Result.success("批量重试任务已发送到MQ");
            
        } catch (Exception e) {
            log.error("管理员批量重试失败任务失败 - 操作者: {}, 最大数量: {}", user.userId(), maxCount, e);
            return Result.error(500, "批量重试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息并校验管理员权限
     */
    private UserContext.UserInfo requireAdmin() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        
        // 这里可以添加管理员权限校验逻辑
        // 比如检查用户角色是否为ADMIN
        
        return info;
    }
}
