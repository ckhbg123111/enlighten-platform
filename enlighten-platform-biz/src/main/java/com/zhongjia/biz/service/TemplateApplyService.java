package com.zhongjia.biz.service;

import com.zhongjia.biz.service.dto.ArticleStructure;

/**
 * 模板套用服务：将结构化文章与模板合成可发布HTML
 */
public interface TemplateApplyService {

    /**
     * 使用给定模板渲染结构化文章为HTML
     * @param templateId 模板ID
     * @param structure 文章结构
     * @return 渲染后的HTML
     */
    String render(Long templateId, ArticleStructure structure);
}


