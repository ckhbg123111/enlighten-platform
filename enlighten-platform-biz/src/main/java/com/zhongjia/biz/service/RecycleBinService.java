package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.RecycleBinItem;

import java.util.List;

public interface RecycleBinService {
    Page<RecycleBinItem> page(Long userId, int page, int size);
    boolean restoreArticles(Long userId, List<Long> recycleIds);
    boolean empty(Long userId, List<Long> recycleIds);
}


