-- 创建视频生成任务表
CREATE TABLE video_generation_task (
    id VARCHAR(36) NOT NULL COMMENT '主键ID (UUID)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    input_text TEXT NOT NULL COMMENT '输入文本',
    model_name VARCHAR(100) COMMENT '数字人模型名称',
    voice VARCHAR(50) DEFAULT 'Female_Voice_1' COMMENT '语音类型',
    
    dh_task_id VARCHAR(100) COMMENT '数字人任务ID',
    dh_status VARCHAR(50) COMMENT '数字人任务状态',
    dh_result_url TEXT COMMENT '数字人生成的视频URL',
    audio_data LONGTEXT COMMENT '数字人返回的音频数据(JSON格式)',
    
    burn_task_id VARCHAR(100) COMMENT '字幕烧录任务ID',
    burn_status VARCHAR(50) COMMENT '字幕烧录状态',
    output_url TEXT COMMENT '最终输出的带字幕视频URL',
    
    progress INT DEFAULT 0 COMMENT '任务进度 (0-100)',
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED' COMMENT '任务状态: CREATED/DH_PROCESSING/DH_DONE/BURN_PROCESSING/COMPLETED/FAILED',
    error_message TEXT COMMENT '错误信息',
    
    version INT DEFAULT 0 COMMENT '版本号(乐观锁)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除字段: 0-未删除, 1-已删除',
    
    PRIMARY KEY (id),
    INDEX idx_user_tenant (user_id, tenant_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    UNIQUE INDEX uk_dh_task_id (dh_task_id),
    UNIQUE INDEX uk_burn_task_id (burn_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频生成任务表';
