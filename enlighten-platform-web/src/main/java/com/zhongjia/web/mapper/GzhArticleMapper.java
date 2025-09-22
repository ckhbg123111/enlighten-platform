package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.web.vo.GzhArticleVO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GzhArticleMapper {
    GzhArticleVO toVO(GzhArticle article);
    List<GzhArticleVO> toVOList(List<GzhArticle> articles);
}


