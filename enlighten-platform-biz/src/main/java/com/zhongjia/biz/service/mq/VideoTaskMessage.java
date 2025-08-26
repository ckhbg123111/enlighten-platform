package com.zhongjia.biz.service.mq;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 视频任务MQ消息
 */
@Data
@Accessors(chain = true)
public class VideoTaskMessage {
    
    /**
     * 消息类型枚举
     */
    public enum ActionType {
        PROCESS_DH("处理数字人阶段"),
        POLL_DH("轮询数字人状态"),
        PROCESS_BURN("处理字幕烧录阶段"),
        POLL_BURN("轮询字幕烧录状态"),
        RETRY_TASK("重试任务");
        
        private final String description;
        
        ActionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 操作类型
     */
    private ActionType action;
    
    /**
     * 延迟执行时间(秒)，用于实现轮询延迟
     */
    private Long delaySeconds;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 消息标签 (用于消息过滤)
     */
    private String tag;
    
    public VideoTaskMessage() {
        this.createTime = LocalDateTime.now();
        this.retryCount = 0;
        this.maxRetries = 3;
    }
    
    public VideoTaskMessage(String taskId, ActionType action) {
        this();
        this.taskId = taskId;
        this.action = action;
    }
    
    public VideoTaskMessage(String taskId, ActionType action, Long delaySeconds) {
        this(taskId, action);
        this.delaySeconds = delaySeconds;
    }
    
    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }
    
    /**
     * 是否超过最大重试次数
     */
    public boolean isMaxRetriesExceeded() {
        return this.retryCount != null && this.maxRetries != null && this.retryCount >= this.maxRetries;
    }
    
    /**
     * 生成消息Key (用于幂等和去重)
     */
    public String generateMessageKey() {
        return String.format("%s_%s_%d", taskId, action.name(), createTime.getNano());
    }
}
