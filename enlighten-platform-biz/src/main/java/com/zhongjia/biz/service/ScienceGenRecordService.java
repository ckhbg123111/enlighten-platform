package com.zhongjia.biz.service;

import java.util.function.Consumer;

public interface ScienceGenRecordService {
    /**
     * 科普生成：承接上游 SSE，逐行写出，并返回完整文本以存档。
     */
    String streamGenerate(Long userId, Long tenantId, String code, String upstreamBody, Consumer<String> writeLine);

    String streamReGenerate(String code,  Consumer<String> writeLine);
}


