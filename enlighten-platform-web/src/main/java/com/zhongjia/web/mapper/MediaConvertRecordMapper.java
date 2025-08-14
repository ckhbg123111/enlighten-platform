package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.web.vo.MediaConvertRecordVO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaConvertRecordMapper {
    MediaConvertRecordVO toVO(MediaConvertRecord record);
    List<MediaConvertRecordVO> toVOList(List<MediaConvertRecord> list);
}


