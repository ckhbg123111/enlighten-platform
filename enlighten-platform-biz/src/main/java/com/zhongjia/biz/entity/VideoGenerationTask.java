package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 视频生成任务实体类
 */
@Data
@Accessors(chain = true)
@TableName("video_generation_task")
public class VideoGenerationTask {
    
    /**
     * 主键ID (UUID字符串)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    
    /**
     * 输入文本
     */
    private String inputText;
    
    /**
     * 视频名称（例如：视频1、视频2...）
     */
    private String videoName;
    
    /**
     * 数字人模型名称
     */
    private String modelName;
    
    /**
     * 语音类型
     */
    private String voice;
    
    /**
     * 数字人任务ID
     */
    private String dhTaskId;
    
    /**
     * 数字人任务状态
     */
    private String dhStatus;
    
    /**
     * 数字人生成的视频URL
     */
    private String dhResultUrl;
    
    /**
     * 数字人返回的音频数据(JSON格式)
     */

    private String audioData;
    
    /**
     * 字幕烧录任务ID
     */
    private String burnTaskId;
    
    /**
     * 字幕烧录状态
     */
    private String burnStatus;
    
    /**
     * 最终输出的带字幕视频URL
     */
    private String outputUrl;
    
    /**
     * 任务进度 (0-100)
     */
    private Integer progress;
    
    /**
     * 任务状态: CREATED/DH_PROCESSING/DH_DONE/BURN_PROCESSING/COMPLETED/FAILED
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 版本号(乐观锁)
     */
    @Version
    private Integer version;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted;
}
