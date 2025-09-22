package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.GzhArticle;

public interface GzhArticleService {
    Long createInitial(Long userId, Long folderId, String name, String tag, String coverImageUrl, String originalText, String typesetContent);
    boolean updateEditing(Long userId, Long id, Long folderId, String name, String tag, String coverImageUrl, String originalText, String typesetContent);
    boolean softDelete(Long userId, Long id);
    boolean batchSoftDelete(Long userId, java.util.List<Long> ids);
    boolean batchUpdateStatus(Long userId, java.util.List<Long> ids, String status);
    boolean batchMoveToFolder(Long userId, java.util.List<Long> ids, Long folderId);
    Page<GzhArticle> pageQuery(Long userId, Long folderId, String nameLike, String tag, String status, int page, int size, String sortBy, boolean asc);
    boolean restoreFromRecycle(Long userId, java.util.List<Long> ids);
    GzhArticle getById(Long id);
}


