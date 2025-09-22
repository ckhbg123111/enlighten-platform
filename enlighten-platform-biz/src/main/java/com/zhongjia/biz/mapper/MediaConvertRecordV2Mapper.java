package com.zhongjia.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjia.biz.entity.MediaConvertRecordV2;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface MediaConvertRecordV2Mapper extends BaseMapper<MediaConvertRecordV2> {
    @Insert("INSERT INTO `media_convert_record_v2` (`user_id`,`external_id`,`platform`,`status`,`deleted`,`create_time`,`update_time`) "
        + "VALUES (#{userId},#{externalId},#{platform},#{status},#{deleted},#{createTime},#{updateTime}) "
        + "ON DUPLICATE KEY UPDATE `status`=VALUES(`status`), `update_time`=VALUES(`update_time`), `id`=LAST_INSERT_ID(`id`)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int upsertProcessing(MediaConvertRecordV2 po);
}


