package com.zhongjia.web.controller;

import com.zhongjia.biz.service.VideoTaskWorkerService;
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
    private VideoTaskWorkerService videoTaskWorkerService;
    
    /**
     * 手动触发任务处理 (管理员功能)
     */
    @PostMapping("/process/{taskId}")
    @Operation(summary = "手动触发任务处理", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<String> processTask(@Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        UserContext.UserInfo user = requireAdmin();
        
        try {
            videoTaskWorkerService.manualProcessTask(taskId);
            log.info("管理员手动触发任务处理 - 操作者: {}, 任务ID: {}", user.userId(), taskId);
            return Result.success("任务处理已触发");
            
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
            // 这里可以添加任务统计逻辑
            // 比如各状态的任务数量等
            
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("message", "任务统计功能待实现");
            stats.put("timestamp", java.time.LocalDateTime.now());
            
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取任务统计信息失败 - 操作者: {}", user.userId(), e);
            return Result.error(500, "获取统计信息失败: " + e.getMessage());
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
