package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.Folder;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.entity.RecycleBinItem;
import com.zhongjia.biz.enums.RecycleFileType;
import com.zhongjia.biz.repository.FolderRepository;
import com.zhongjia.biz.repository.GzhArticleRepository;
import com.zhongjia.biz.repository.RecycleBinItemRepository;
import com.zhongjia.biz.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    @Autowired
    private RecycleBinItemRepository recycleRepository;
    @Autowired
    private GzhArticleRepository articleRepository;
    @Autowired
    private FolderRepository folderRepository;

    @Override
    public Page<RecycleBinItem> page(Long userId, int page, int size) {
        Page<RecycleBinItem> p = new Page<>(page, size);
        return recycleRepository.page(p, new LambdaQueryWrapper<RecycleBinItem>()
                .eq(RecycleBinItem::getUserId, userId)
                .ge(RecycleBinItem::getDeleteTime, java.time.LocalDateTime.now().minusDays(30))
                .orderByDesc(RecycleBinItem::getDeleteTime));
    }

    @Override
    public boolean restoreArticles(Long userId, List<Long> recycleIds) {
        // 1) 找到回收站条目 -> 取文章ID
        List<RecycleBinItem> items = recycleRepository.list(new LambdaQueryWrapper<RecycleBinItem>()
                .eq(RecycleBinItem::getUserId, userId)
                .in(RecycleBinItem::getId, recycleIds)
                .eq(RecycleBinItem::getFileType, RecycleFileType.WECHAT_ARTICLE.name()));
        if (items.isEmpty()) return true;
        List<Long> articleIds = items.stream().map(RecycleBinItem::getFileId).toList();

        // 2) 查询文章与其文件夹状态
        List<GzhArticle> articles = articleRepository.list(new LambdaQueryWrapper<GzhArticle>()
                .in(GzhArticle::getId, articleIds)
                .eq(GzhArticle::getUserId, userId));
        Set<Long> folderIds = articles.stream()
                .map(GzhArticle::getFolderId)
                .filter(fid -> fid != null)
                .collect(Collectors.toSet());
        Map<Long, Folder> folderMap = folderIds.isEmpty() ? java.util.Collections.emptyMap() :
                folderRepository.list(new LambdaQueryWrapper<Folder>().in(Folder::getId, folderIds))
                        .stream().collect(Collectors.toMap(Folder::getId, f -> f));

        // 3) 拆分：文件夹有效（未删除）与无效（不存在或已删除）
        List<Long> idsWithInvalidFolder = articles.stream().filter(a -> {
            Long fid = a.getFolderId();
            if (fid == null) return false; // 根目录无需处理
            Folder f = folderMap.get(fid);
            return f == null || (f.getDeleted() != null && f.getDeleted() == 1);
        }).map(GzhArticle::getId).toList();
        List<Long> idsWithValidFolder = articles.stream()
                .map(GzhArticle::getId)
                .filter(id -> !idsWithInvalidFolder.contains(id))
                .toList();

        boolean ok = true;
        // 4) 先恢复无效文件夹下的文章：置为根目录
        if (!idsWithInvalidFolder.isEmpty()) {
            boolean part = articleRepository.update(
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<GzhArticle>()
                            .in(GzhArticle::getId, idsWithInvalidFolder)
                            .eq(GzhArticle::getUserId, userId)
                            .eq(GzhArticle::getDeleted, 1)
                            .set(GzhArticle::getFolderId, null)
                            .set(GzhArticle::getDeleted, 0)
                            .set(GzhArticle::getDeleteTime, null)
            );
            ok = ok && part;
        }
        // 5) 再恢复其余文章：保留原文件夹
        if (!idsWithValidFolder.isEmpty()) {
            boolean part = articleRepository.update(
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<GzhArticle>()
                            .in(GzhArticle::getId, idsWithValidFolder)
                            .eq(GzhArticle::getUserId, userId)
                            .eq(GzhArticle::getDeleted, 1)
                            .set(GzhArticle::getDeleted, 0)
                            .set(GzhArticle::getDeleteTime, null)
            );
            ok = ok && part;
        }

        if (ok) {
            recycleRepository.remove(new LambdaQueryWrapper<RecycleBinItem>()
                    .eq(RecycleBinItem::getUserId, userId)
                    .in(RecycleBinItem::getId, recycleIds));
        }
        return ok;
    }

    @Override
    public boolean empty(Long userId, List<Long> recycleIds) {
        return recycleRepository.remove(new LambdaQueryWrapper<RecycleBinItem>()
                .eq(RecycleBinItem::getUserId, userId)
                .in(RecycleBinItem::getId, recycleIds));
    }
}


