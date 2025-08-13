package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.DraftPO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

public interface DraftService {
    /**
     * 新建或按 essayCode 更新当前用户的草稿，返回草稿ID。
     */
    Long saveOrUpdateByEssayCode(Long userId, Long tenantId, String essayCode, String title, String content, List<String> mediaCodeList);

    /**
     * 分页查询当前用户未删除的草稿，按更新时间倒序。
     */
    Page<DraftPO> pageByUser(Long userId, int page, int pageSize);

    /**
     * 编辑草稿（只允许本人）。
     */
    boolean editDraft(Long userId, Long draftId, String title, String content);

    /**
     * 软删除草稿（只允许本人）。
     */
    boolean softDelete(Long userId, Long draftId);

    /**
     * 恢复草稿（只允许本人）。
     */
    boolean restore(Long userId, Long draftId);
}



