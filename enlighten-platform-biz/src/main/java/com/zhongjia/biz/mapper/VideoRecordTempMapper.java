package com.zhongjia.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjia.biz.entity.VideoRecordTemp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoRecordTempMapper extends BaseMapper<VideoRecordTemp> {
    @Select("SELECT COUNT(*) FROM video_record_temp WHERE user_id = #{userId}")
    Long countAllByUserId(@Param("userId") Long userId);
}


