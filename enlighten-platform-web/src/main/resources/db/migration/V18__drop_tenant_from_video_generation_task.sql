-- 删除视频生成任务表中的租户字段与相关索引
ALTER TABLE video_generation_task
  DROP INDEX idx_user_tenant,
  DROP COLUMN tenant_id;

-- 为 user_id 单独创建索引，保持查询性能
CREATE INDEX idx_user_id ON video_generation_task (user_id);


