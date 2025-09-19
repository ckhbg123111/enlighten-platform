package com.zhongjia.biz.service;

import com.zhongjia.biz.service.dto.ArticleStructure;

/**
 * 文章结构解析服务：将原始文章内容解析为结构化数据
 */
public interface ArticleStructureService {

    /**
     * 调用上游服务解析文章结构
     * @param essay 原始文章内容
     * @return 结构化文章
     */
    ArticleStructure parse(String essay);
}


