package com.zhongjia.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjia.biz.entity.VideoGenerationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 视频生成任务 Mapper 接口
 */
@Mapper
public interface VideoGenerationTaskMapper extends BaseMapper<VideoGenerationTask> {

    /**
     * 根据状态查询待处理的任务
     * @param status 任务状态
     * @param limit 限制条数
     * @return 任务列表
     */
    @Select("SELECT * FROM video_generation_task WHERE status = #{status} AND deleted = 0 ORDER BY created_at ASC LIMIT #{limit}")
    List<VideoGenerationTask> selectPendingTasks(@Param("status") String status, @Param("limit") int limit);
    
    /**
     * 根据数字人任务ID查询
     * @param dhTaskId 数字人任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM video_generation_task WHERE dh_task_id = #{dhTaskId} AND deleted = 0 LIMIT 1")
    VideoGenerationTask selectByDhTaskId(@Param("dhTaskId") String dhTaskId);
    
    /**
     * 根据字幕烧录任务ID查询
     * @param burnTaskId 字幕烧录任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM video_generation_task WHERE burn_task_id = #{burnTaskId} AND deleted = 0 LIMIT 1")
    VideoGenerationTask selectByBurnTaskId(@Param("burnTaskId") String burnTaskId);
    
    /**
     * 查询用户的任务列表
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM video_generation_task WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<VideoGenerationTask> selectByUser(@Param("userId") Long userId);

    /**
     * 查询用户的任务列表（可按状态过滤）
     * @param userId 用户ID
     * @param statuses 状态列表，可为空或空列表表示不限
     * @return 任务列表
     */
    @Select({
            "<script>",
            "SELECT * FROM video_generation_task",
            "WHERE user_id = #{userId} AND deleted = 0",
            "<if test='statuses != null and statuses.size() > 0'>",
            "  AND status IN",
            "  <foreach collection='statuses' item='st' open='(' separator=',' close=')'>",
            "    #{st}",
            "  </foreach>",
            "</if>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    List<VideoGenerationTask> selectByUserAndStatuses(@Param("userId") Long userId, @Param("statuses") java.util.List<String> statuses);
}
