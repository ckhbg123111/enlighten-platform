package com.zhongjia.biz.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import com.zhongjia.biz.service.VideoGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 视频任务消息消费者
 */
@Slf4j
@Service
@RocketMQMessageListener(
    topic = VideoTaskProducer.TOPIC_VIDEO_TASK,
    consumerGroup = "video_task_consumer_group",
    messageModel = MessageModel.CLUSTERING,
    consumeMode = ConsumeMode.CONCURRENTLY,
    maxReconsumeTimes = 3
)
public class VideoTaskConsumer implements RocketMQListener<String> {
    
    @Autowired
    private VideoGenerationTaskRepository taskRepository;
    
    @Autowired
    private VideoGenerationService videoGenerationService;
    
    @Autowired
    private VideoTaskProducer videoTaskProducer;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void onMessage(String messageBody) {
        VideoTaskMessage message = null;
        try {
            // 解析消息
            message = objectMapper.readValue(messageBody, VideoTaskMessage.class);
            log.info("收到视频任务消息 - 任务ID: {}, 动作: {}, 重试次数: {}", 
                    message.getTaskId(), message.getAction(), message.getRetryCount());
            
            // 查询任务是否存在
            VideoGenerationTask task = taskRepository.getById(message.getTaskId());
            if (task == null) {
                log.warn("任务不存在，跳过处理 - 任务ID: {}", message.getTaskId());
                return;
            }
            
            // 设置MDC用于日志追踪
            org.slf4j.MDC.put("taskId", message.getTaskId());
            org.slf4j.MDC.put("userId", String.valueOf(message.getUserId()));
            
            // 根据消息类型处理
            handleMessage(message, task);
            
        } catch (Exception e) {
            log.error("处理视频任务消息异常 - 消息体: {}", messageBody, e);
            
            // 如果解析消息成功，进行重试
            if (message != null && !message.isMaxRetriesExceeded()) {
                try {
                    videoTaskProducer.sendRetryMessage(message);
                } catch (Exception retryError) {
                    log.error("发送重试消息失败 - 任务ID: {}", message.getTaskId(), retryError);
                }
            } else if (message != null) {
                // 超过最大重试次数，标记任务失败
                try {
                    VideoGenerationTask task = taskRepository.getById(message.getTaskId());
                    if (task != null) {
                        videoGenerationService.updateTaskStatus(task, "FAILED", 
                                task.getProgress(), "任务处理失败，已超过最大重试次数: " + e.getMessage());
                    }
                } catch (Exception updateError) {
                    log.error("更新任务失败状态异常 - 任务ID: {}", message.getTaskId(), updateError);
                }
            }
        } finally {
            // 清理MDC
            org.slf4j.MDC.clear();
        }
    }
    
    /**
     * 处理具体的消息类型
     */
    private void handleMessage(VideoTaskMessage message, VideoGenerationTask task) {
        switch (message.getAction()) {
            case PROCESS_DH:
                handleProcessDh(message, task);
                break;
            case POLL_DH:
                handlePollDh(message, task);
                break;
            case PROCESS_BURN:
                handleProcessBurn(message, task);
                break;
            case POLL_BURN:
                handlePollBurn(message, task);
                break;
            case RETRY_TASK:
                handleRetryTask(message, task);
                break;
            default:
                log.warn("未知的消息动作类型 - 任务ID: {}, 动作: {}", message.getTaskId(), message.getAction());
        }
    }
    
    /**
     * 处理数字人生成阶段
     */
    private void handleProcessDh(VideoTaskMessage message, VideoGenerationTask task) {
        try {
            log.info("开始处理数字人生成阶段 - 任务ID: {}", task.getId());
            
            // 检查任务状态是否正确
            if (!"CREATED".equals(task.getStatus())) {
                log.warn("任务状态不正确，跳过处理 - 任务ID: {}, 当前状态: {}", task.getId(), task.getStatus());
                return;
            }
            
            // 执行数字人生成
            videoGenerationService.processDhPhase(task);
            
            // 成功后发送轮询消息
            videoTaskProducer.sendPollDhMessage(task.getId(), message.getUserId(), 5);
            
        } catch (Exception e) {
            log.error("处理数字人生成阶段失败 - 任务ID: {}", task.getId(), e);
            videoGenerationService.updateTaskStatus(task, "FAILED", 0, "数字人生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 轮询数字人状态
     */
    private void handlePollDh(VideoTaskMessage message, VideoGenerationTask task) {
        try {
            // 检查任务状态
            if (!"DH_PROCESSING".equals(task.getStatus())) {
                log.warn("任务状态不正确，跳过轮询 - 任务ID: {}, 当前状态: {}", task.getId(), task.getStatus());
                return;
            }
            
            // 轮询数字人状态
            boolean completed = videoGenerationService.pollDhStatus(task);
            
            if (completed) {
                // 重新查询任务状态
                task = taskRepository.getById(task.getId());
                if ("DH_DONE".equals(task.getStatus())) {
                    // 数字人完成，开始字幕烧录
                    videoTaskProducer.sendProcessBurnMessage(task.getId(), message.getUserId());
                    log.info("数字人完成，触发字幕烧录 - 任务ID: {}", task.getId());
                } else if ("FAILED".equals(task.getStatus())) {
                    log.error("数字人任务失败 - 任务ID: {}", task.getId());
                }
            } else {
                // 继续轮询，使用指数退避
                long nextDelay = Math.min(30, 5 + (message.getRetryCount() != null ? message.getRetryCount() : 0) * 2);
                videoTaskProducer.sendPollDhMessage(task.getId(), message.getUserId(), nextDelay);
            }
            
        } catch (Exception e) {
            log.error("轮询数字人状态异常 - 任务ID: {}", task.getId(), e);
            // 网络异常等不应该立即失败，继续重试
            videoTaskProducer.sendRetryMessage(message);
        }
    }
    
    /**
     * 处理字幕烧录阶段
     */
    private void handleProcessBurn(VideoTaskMessage message, VideoGenerationTask task) {
        try {
            log.info("开始处理字幕烧录阶段 - 任务ID: {}", task.getId());
            
            // 检查任务状态
            if (!"DH_DONE".equals(task.getStatus())) {
                log.warn("任务状态不正确，跳过处理 - 任务ID: {}, 当前状态: {}", task.getId(), task.getStatus());
                return;
            }
            
            // 执行字幕烧录
            videoGenerationService.processBurnPhase(task);
            
            // 成功后发送轮询消息
            videoTaskProducer.sendPollBurnMessage(task.getId(), message.getUserId(), 5);
            
        } catch (Exception e) {
            log.error("处理字幕烧录阶段失败 - 任务ID: {}", task.getId(), e);
            videoGenerationService.updateTaskStatus(task, "FAILED", 60, "字幕烧录失败: " + e.getMessage());
        }
    }
    
    /**
     * 轮询字幕烧录状态
     */
    private void handlePollBurn(VideoTaskMessage message, VideoGenerationTask task) {
        try {
            // 检查任务状态
            if (!"BURN_PROCESSING".equals(task.getStatus())) {
                log.warn("任务状态不正确，跳过轮询 - 任务ID: {}, 当前状态: {}", task.getId(), task.getStatus());
                return;
            }
            
            // 轮询字幕烧录状态
            boolean completed = videoGenerationService.pollBurnStatus(task);
            
            if (completed) {
                // 重新查询任务状态
                task = taskRepository.getById(task.getId());
                if ("COMPLETED".equals(task.getStatus())) {
                    log.info("视频生成任务完全完成 - 任务ID: {}", task.getId());
                } else if ("FAILED".equals(task.getStatus())) {
                    log.error("字幕烧录任务失败 - 任务ID: {}", task.getId());
                }
            } else {
                // 继续轮询
                long nextDelay = Math.min(30, 5 + (message.getRetryCount() != null ? message.getRetryCount() : 0) * 2);
                videoTaskProducer.sendPollBurnMessage(task.getId(), message.getUserId(), nextDelay);
            }
            
        } catch (Exception e) {
            log.error("轮询字幕烧录状态异常 - 任务ID: {}", task.getId(), e);
            // 网络异常等不应该立即失败，继续重试
            videoTaskProducer.sendRetryMessage(message);
        }
    }
    
    /**
     * 处理重试任务
     */
    private void handleRetryTask(VideoTaskMessage message, VideoGenerationTask task) {
        log.info("处理重试任务 - 任务ID: {}, 原动作: {}, 重试次数: {}", 
                task.getId(), message.getAction(), message.getRetryCount());
        
        // 根据任务当前状态决定重试动作
        switch (task.getStatus()) {
            case "CREATED":
                message.setAction(VideoTaskMessage.ActionType.PROCESS_DH);
                handleProcessDh(message, task);
                break;
            case "DH_PROCESSING":
                message.setAction(VideoTaskMessage.ActionType.POLL_DH);
                handlePollDh(message, task);
                break;
            case "DH_DONE":
                message.setAction(VideoTaskMessage.ActionType.PROCESS_BURN);
                handleProcessBurn(message, task);
                break;
            case "BURN_PROCESSING":
                message.setAction(VideoTaskMessage.ActionType.POLL_BURN);
                handlePollBurn(message, task);
                break;
            default:
                log.warn("无法重试的任务状态 - 任务ID: {}, 状态: {}", task.getId(), task.getStatus());
        }
    }
}
