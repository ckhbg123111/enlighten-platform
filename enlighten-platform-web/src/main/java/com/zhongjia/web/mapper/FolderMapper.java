package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.Folder;
import com.zhongjia.web.vo.FolderVO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FolderMapper {
    FolderVO toVO(Folder folder);
    List<FolderVO> toVOList(List<Folder> folders);
}


