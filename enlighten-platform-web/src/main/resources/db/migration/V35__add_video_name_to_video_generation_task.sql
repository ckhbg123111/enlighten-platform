-- 为视频生成任务表新增 video_name 列
ALTER TABLE video_generation_task
  ADD COLUMN video_name VARCHAR(255) COMMENT '视频名称';


