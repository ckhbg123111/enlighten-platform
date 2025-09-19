package com.zhongjia.biz.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleStructure {
    private String title;
    private Introduction introduction;
    private List<Section> sections;
    private String summary;

    @Data
    public static class Introduction {
        private String text;
    }

    @Data
    public static class Section {
        private String section_title;
        private List<Paragraph> section_paragraphs;
    }

    @Data
    public static class Paragraph {
        private String paragraph_title;
        private String paragraph_text;
        private String image_url;
    }
}


