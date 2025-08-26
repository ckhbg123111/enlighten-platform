package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 视频任务异步处理器 (已弃用，保留用于MQ故障时的备用方案)
 * 负责轮询和处理各个阶段的视频生成任务
 * 
 * 注意：此类已被MQ方案替代，默认禁用
 * 如需启用，请在配置中设置 app.video.fallback.scheduler.enabled=true
 */
@Slf4j
// @Service // 注释掉，默认不启动
public class VideoTaskWorkerServiceLegacy {
    
    @Autowired
    private VideoGenerationTaskRepository taskRepository;
    
    @Autowired
    private VideoGenerationService videoGenerationService;
    
    /**
     * 处理新创建的任务 - 启动数字人生成
     * 每10秒执行一次
     */
    @Scheduled(fixedDelay = 10000) // 10秒
    public void processCreatedTasks() {
        try {
            List<VideoGenerationTask> tasks = taskRepository.findPendingTasks("CREATED", 10);
            if (!tasks.isEmpty()) {
                log.info("发现 {} 个待处理的CREATED任务", tasks.size());
                
                for (VideoGenerationTask task : tasks) {
                    processCreatedTaskAsync(task);
                }
            }
        } catch (Exception e) {
            log.error("处理CREATED任务异常", e);
        }
    }
    
    /**
     * 轮询数字人生成状态
     * 每5秒执行一次
     */
    @Scheduled(fixedDelay = 5000) // 5秒
    public void pollDhProcessingTasks() {
        try {
            List<VideoGenerationTask> tasks = taskRepository.findPendingTasks("DH_PROCESSING", 20);
            if (!tasks.isEmpty()) {
                log.debug("轮询 {} 个DH_PROCESSING任务状态", tasks.size());
                
                for (VideoGenerationTask task : tasks) {
                    pollDhTaskAsync(task);
                }
            }
        } catch (Exception e) {
            log.error("轮询DH_PROCESSING任务异常", e);
        }
    }
    
    /**
     * 处理数字人完成的任务 - 启动字幕烧录
     * 每10秒执行一次
     */
    @Scheduled(fixedDelay = 10000) // 10秒
    public void processDhDoneTasks() {
        try {
            List<VideoGenerationTask> tasks = taskRepository.findPendingTasks("DH_DONE", 10);
            if (!tasks.isEmpty()) {
                log.info("发现 {} 个待处理的DH_DONE任务", tasks.size());
                
                for (VideoGenerationTask task : tasks) {
                    processBurnTaskAsync(task);
                }
            }
        } catch (Exception e) {
            log.error("处理DH_DONE任务异常", e);
        }
    }
    
    /**
     * 轮询字幕烧录状态
     * 每5秒执行一次
     */
    @Scheduled(fixedDelay = 5000) // 5秒
    public void pollBurnProcessingTasks() {
        try {
            List<VideoGenerationTask> tasks = taskRepository.findPendingTasks("BURN_PROCESSING", 20);
            if (!tasks.isEmpty()) {
                log.debug("轮询 {} 个BURN_PROCESSING任务状态", tasks.size());
                
                for (VideoGenerationTask task : tasks) {
                    pollBurnTaskAsync(task);
                }
            }
        } catch (Exception e) {
            log.error("轮询BURN_PROCESSING任务异常", e);
        }
    }
    
    /**
     * 清理超时任务
     * 每30分钟执行一次
     */
    @Scheduled(fixedDelay = 1800000) // 30分钟
    public void cleanupTimeoutTasks() {
        try {
            // 清理创建超过30分钟仍未完成的任务
            // 这里可以添加具体的清理逻辑
            log.info("执行超时任务清理");
            
        } catch (Exception e) {
            log.error("清理超时任务异常", e);
        }
    }
    
    // 异步处理方法
    
    @Async
    public CompletableFuture<Void> processCreatedTaskAsync(VideoGenerationTask task) {
        try {
            log.info("开始异步处理CREATED任务: {}", task.getId());
            videoGenerationService.processDhPhase(task);
        } catch (Exception e) {
            log.error("异步处理CREATED任务异常 - 任务ID: {}", task.getId(), e);
            videoGenerationService.updateTaskStatus(task, "FAILED", 0, "启动数字人生成失败: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    public CompletableFuture<Void> pollDhTaskAsync(VideoGenerationTask task) {
        try {
            boolean completed = videoGenerationService.pollDhStatus(task);
            if (completed) {
                log.info("数字人任务状态轮询完成 - 任务ID: {}, 最终状态: {}", task.getId(), task.getStatus());
            }
        } catch (Exception e) {
            log.error("异步轮询数字人任务状态异常 - 任务ID: {}", task.getId(), e);
            // 网络异常等不应该立即标记为失败，继续重试
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    public CompletableFuture<Void> processBurnTaskAsync(VideoGenerationTask task) {
        try {
            log.info("开始异步处理DH_DONE任务: {}", task.getId());
            videoGenerationService.processBurnPhase(task);
        } catch (Exception e) {
            log.error("异步处理DH_DONE任务异常 - 任务ID: {}", task.getId(), e);
            videoGenerationService.updateTaskStatus(task, "FAILED", 60, "启动字幕烧录失败: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    public CompletableFuture<Void> pollBurnTaskAsync(VideoGenerationTask task) {
        try {
            boolean completed = videoGenerationService.pollBurnStatus(task);
            if (completed) {
                log.info("字幕烧录任务状态轮询完成 - 任务ID: {}, 最终状态: {}", task.getId(), task.getStatus());
            }
        } catch (Exception e) {
            log.error("异步轮询字幕烧录任务状态异常 - 任务ID: {}", task.getId(), e);
            // 网络异常等不应该立即标记为失败，继续重试
        }
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 手动触发任务处理 (可用于重试失败的任务)
     */
    public void manualProcessTask(String taskId) {
        try {
            VideoGenerationTask task = taskRepository.getById(taskId);
            if (task == null) {
                log.warn("手动处理任务失败，任务不存在: {}", taskId);
                return;
            }
            
            log.info("手动触发任务处理 - 任务ID: {}, 当前状态: {}", taskId, task.getStatus());
            
            switch (task.getStatus()) {
                case "CREATED":
                    processCreatedTaskAsync(task);
                    break;
                case "DH_PROCESSING":
                    pollDhTaskAsync(task);
                    break;
                case "DH_DONE":
                    processBurnTaskAsync(task);
                    break;
                case "BURN_PROCESSING":
                    pollBurnTaskAsync(task);
                    break;
                case "FAILED":
                    // 重置状态并重新处理
                    task.setStatus("CREATED").setErrorMessage(null).setProgress(0);
                    taskRepository.updateById(task);
                    processCreatedTaskAsync(task);
                    break;
                default:
                    log.warn("任务状态不支持手动处理: {}", task.getStatus());
            }
            
        } catch (Exception e) {
            log.error("手动处理任务异常 - 任务ID: {}", taskId, e);
        }
    }
}
