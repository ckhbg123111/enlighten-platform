package com.zhongjia.biz.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RenderResult {
    private String title;
    private String html;

    public RenderResult(String title, String html) {
        this.title = title;
        this.html = html;
    }
}
