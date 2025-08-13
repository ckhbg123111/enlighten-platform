package com.zhongjia.biz.service;

public interface FillInRecordService {
    /**
     * 调用上游并将 SSE 数据通过回调函数逐行写出；同时累积返回完整文本给调用方存档。
     * 回调函数 writeLine 接收已经拼接好的 "data:...\n\n" 行。
     */
    String streamFillIn(Long userId, Long tenantId, String content, java.util.function.Consumer<String> writeLine);
}


