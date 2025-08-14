package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.DraftPO;
import com.zhongjia.web.vo.DraftVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DraftMapper {

    @Mapping(target = "mediaCodeList", source = "mediaCodeListString", qualifiedByName = "stringToList")
    DraftVO toVO(DraftPO po);

    List<DraftVO> toVOList(List<DraftPO> list);

    @Named("stringToList")
    default List<String> stringToList(String mediaCodeListString) {
        if (mediaCodeListString == null || mediaCodeListString.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(mediaCodeListString.split(","));
    }
}


