package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.MediaConvertRecordV2;
import com.zhongjia.web.vo.MediaConvertRecordV2VO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaConvertRecordV2WebMapper {
    MediaConvertRecordV2VO toVO(MediaConvertRecordV2 record);
    List<MediaConvertRecordV2VO> toVOList(List<MediaConvertRecordV2> records);
}


