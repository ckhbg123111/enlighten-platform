package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.RecycleBinItem;
import com.zhongjia.web.vo.RecycleBinItemVO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RecycleBinItemMapper {
    RecycleBinItemVO toVO(RecycleBinItem item);
    List<RecycleBinItemVO> toVOList(List<RecycleBinItem> items);
}


