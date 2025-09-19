package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.entity.TypesettingTemplate;
import com.zhongjia.biz.repository.TypesettingTemplateRepository;
import com.zhongjia.biz.service.TemplateApplyService;
import com.zhongjia.biz.service.dto.ArticleStructure;
import org.springframework.stereotype.Service;

@Service
public class TemplateApplyServiceImpl implements TemplateApplyService {

    private final TypesettingTemplateRepository templateRepository;

    public TemplateApplyServiceImpl(TypesettingTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public String render(Long templateId, ArticleStructure structure) {
        if (templateId == null || structure == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TypesettingTemplate template = templateRepository.getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }

        StringBuilder html = new StringBuilder();
        // header: 需要将封面图替换进 {PLACEHOLDER}。此处没有封面，保留原样或清空
        String header = safe(template.getHeader());
        header = replace(header, "{PLACEHOLDER}", "http://frp5.mmszxc.xin:57599/file/figure/header.png");
        html.append(nullToEmpty(header));

        // 标题
        if (structure.getTitle() != null && !structure.getTitle().isEmpty()) {
            String single = nullToEmpty(template.getSingleTitle());
            html.append(replace(single, "{PLACEHOLDER}", escape(structure.getTitle())));
        }

        // 引言
        if (structure.getIntroduction() != null && structure.getIntroduction().getText() != null) {
            String textTpl = nullToEmpty(template.getText());
            html.append(replace(textTpl, "{PLACEHOLDER}", escape(structure.getIntroduction().getText())));
        }

        // sections
        if (structure.getSections() != null) {
            for (ArticleStructure.Section section : structure.getSections()) {
                if (section.getSection_title() != null && !section.getSection_title().isEmpty()) {
                    String numbered = nullToEmpty(template.getNumberedTitle());
                    html.append(replace(numbered, "{PLACEHOLDER}", escape(section.getSection_title())));
                }
                if (section.getSection_paragraphs() != null) {
                    for (ArticleStructure.Paragraph p : section.getSection_paragraphs()) {
                        if (p.getParagraph_title() != null && !p.getParagraph_title().isEmpty()) {
                            String single = nullToEmpty(template.getSingleTitle());
                            html.append(replace(single, "{PLACEHOLDER}", escape(p.getParagraph_title())));
                        }
                        if (p.getParagraph_text() != null && !p.getParagraph_text().isEmpty()) {
                            String textTpl = nullToEmpty(template.getText());
                            html.append(replace(textTpl, "{PLACEHOLDER}", escape(p.getParagraph_text())));
                        }
                        if (p.getImage_url() != null && !p.getImage_url().isEmpty()) {
                            String imgTpl = nullToEmpty(template.getImage());
                            String imgTag = "<img src=\"" + escapeAttr(p.getImage_url()) + "\" alt=\"\"/>";
                            html.append(replace(imgTpl, "{PLACEHOLDER}", imgTag));
                        }
                    }
                }
            }
        }

        // summary
        if (structure.getSummary() != null && !structure.getSummary().isEmpty()) {
            String block = nullToEmpty(template.getBlockCard());
            String textTpl = nullToEmpty(template.getText());
            String content = replace(textTpl, "{PLACEHOLDER}", escape(structure.getSummary()));
            html.append(replace(block, "{PLACEHOLDER}", content));
        }

        // footer
        String footer = safe(template.getFooter());
        footer = replace(footer, "{PLACEHOLDER}", "http://frp5.mmszxc.xin:57599/file/figure/footer1.jpg");
        html.append(nullToEmpty(footer));

        return html.toString();
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private String safe(String s) { return s == null ? "" : s; }
    private String replace(String src, String token, String val) { return src == null ? "" : src.replace(token, val); }
    private String escape(String s) { return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;"); }
    private String escapeAttr(String s) { return s == null ? "" : s.replace("\"", "&quot;"); }
}


