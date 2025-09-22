package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.entity.RecycleBinItem;
import com.zhongjia.biz.enums.RecycleFileType;
import com.zhongjia.biz.repository.GzhArticleRepository;
import com.zhongjia.biz.repository.RecycleBinItemRepository;
import com.zhongjia.biz.service.GzhArticleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

@Service
public class GzhArticleServiceImpl implements GzhArticleService {

    private final GzhArticleRepository articleRepository;
    private final RecycleBinItemRepository recycleRepository;

    public GzhArticleServiceImpl(GzhArticleRepository articleRepository, RecycleBinItemRepository recycleRepository) {
        this.articleRepository = articleRepository;
        this.recycleRepository = recycleRepository;
    }

    @Override
    public Long createInitial(Long userId, Long folderId, String name, String tag, String coverImageUrl, String originalText, String typesetContent) {
        // 自动命名：当 name 为空时，生成 MMdd-公众号-当日编号（同用户同日内递增且去重）
        String finalName = StringUtils.isBlank(name) ? generateAutoName(userId) : name;

        GzhArticle po = new GzhArticle();
        po.setUserId(userId);
        po.setFolderId(folderId);
        po.setName(finalName);
        po.setTag(StringUtils.isBlank(tag) ? "公众号文章" : tag);
        po.setCoverImageUrl(coverImageUrl);
        po.setOriginalText(originalText);
        po.setTypesetContent(typesetContent);
        po.setStatus("INITIAL");
        po.setDeleted(0);
        boolean ok = articleRepository.save(po);
        return ok ? po.getId() : null;
    }

    /**
     * 生成自动名称：MMdd-公众号-{序号}
     * 规则：以当日（本地时间）为范围，基于已存在记录计数 + 去重避免名称冲突
     */
    private String generateAutoName(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        long todayCount = articleRepository.count(new LambdaQueryWrapper<GzhArticle>()
                .eq(GzhArticle::getUserId, userId)
                .ge(GzhArticle::getCreateTime, start)
                .lt(GzhArticle::getCreateTime, end));
        AtomicInteger seq = new AtomicInteger((int) todayCount + 1);
        String datePart = String.format("%02d%02d", today.getMonthValue(), today.getDayOfMonth());
        while (true) {
            String candidate = datePart + "-公众号-" + seq.get();
            long exists = articleRepository.count(new LambdaQueryWrapper<GzhArticle>()
                    .eq(GzhArticle::getUserId, userId)
                    .eq(GzhArticle::getName, candidate));
            if (exists == 0) {
                return candidate;
            }
            seq.incrementAndGet();
        }
    }

    @Override
    public boolean updateEditing(Long userId, Long id, Long folderId, String name, String tag, String coverImageUrl, String originalText, String typesetContent) {
        GzhArticle exist = articleRepository.getById(id);
        if (exist == null || !exist.getUserId().equals(userId) || exist.getDeleted() != 0) {
            return false;
        }
        GzhArticle upd = new GzhArticle();
        upd.setId(id);
        if (folderId != null) upd.setFolderId(folderId);
        if (name != null) upd.setName(name);
        if (tag != null) upd.setTag(tag);
        if (coverImageUrl != null) upd.setCoverImageUrl(coverImageUrl);
        if (originalText != null) upd.setOriginalText(originalText);
        if (typesetContent != null) upd.setTypesetContent(typesetContent);
        upd.setStatus("EDITING");
        upd.setLastEditTime(LocalDateTime.now());
        return articleRepository.updateById(upd);
    }

    @Override
    public boolean softDelete(Long userId, Long id) {
        GzhArticle exist = articleRepository.getById(id);
        if (exist == null || !exist.getUserId().equals(userId) || exist.getDeleted() != 0) {
            return false;
        }
        boolean ok = articleRepository.update(new LambdaUpdateWrapper<GzhArticle>()
                .eq(GzhArticle::getId, id)
                .eq(GzhArticle::getUserId, userId)
                .set(GzhArticle::getDeleted, 1)
                .set(GzhArticle::getDeleteTime, LocalDateTime.now()));
        if (ok) {
            // 放入回收站
            RecycleBinItem item = new RecycleBinItem();
            item.setUserId(userId);
            item.setFileId(id);
            item.setFileType(RecycleFileType.WECHAT_ARTICLE.name());
            item.setDeleteTime(LocalDateTime.now());
            recycleRepository.save(item);
        }
        return ok;
    }

    @Override
    public boolean batchSoftDelete(Long userId, List<Long> ids) {
        boolean ok = articleRepository.update(new LambdaUpdateWrapper<GzhArticle>()
                .in(GzhArticle::getId, ids)
                .eq(GzhArticle::getUserId, userId)
                .eq(GzhArticle::getDeleted, 0)
                .set(GzhArticle::getDeleted, 1)
                .set(GzhArticle::getDeleteTime, LocalDateTime.now()));
        if (ok) {
            for (Long id : ids) {
                RecycleBinItem item = new RecycleBinItem();
                item.setUserId(userId);
                item.setFileId(id);
                item.setFileType(RecycleFileType.WECHAT_ARTICLE.name());
                item.setDeleteTime(LocalDateTime.now());
                recycleRepository.save(item);
            }
        }
        return ok;
    }

    @Override
    public boolean batchUpdateStatus(Long userId, List<Long> ids, String status) {
        return articleRepository.update(new LambdaUpdateWrapper<GzhArticle>()
                .in(GzhArticle::getId, ids)
                .eq(GzhArticle::getUserId, userId)
                .eq(GzhArticle::getDeleted, 0)
                .set(GzhArticle::getStatus, status)
                .set(GzhArticle::getLastEditTime, LocalDateTime.now()));
    }

    @Override
    public boolean batchMoveToFolder(Long userId, List<Long> ids, Long folderId) {
        return articleRepository.update(new LambdaUpdateWrapper<GzhArticle>()
                .in(GzhArticle::getId, ids)
                .eq(GzhArticle::getUserId, userId)
                .eq(GzhArticle::getDeleted, 0)
                .set(GzhArticle::getFolderId, folderId)
                .set(GzhArticle::getLastEditTime, LocalDateTime.now()));
    }

    @Override
    public Page<GzhArticle> pageQuery(Long userId, Long folderId, String nameLike, String tag, String status, int page, int size, String sortBy, boolean asc) {
        LambdaQueryWrapper<GzhArticle> w = new LambdaQueryWrapper<GzhArticle>()
                .eq(GzhArticle::getUserId, userId)
                .eq(GzhArticle::getDeleted, 0);
        if (folderId != null) w.eq(GzhArticle::getFolderId, folderId);
        if (StringUtils.isNotBlank(nameLike)) w.like(GzhArticle::getName, nameLike);
        if (StringUtils.isNotBlank(tag)) w.eq(GzhArticle::getTag, tag);
        if (StringUtils.isNotBlank(status)) w.eq(GzhArticle::getStatus, status);
        // 排序支持：last_edit_time、name/name_pinyin、create_time
        if ("last_edit_time".equalsIgnoreCase(sortBy)) {
            w.orderBy(true, asc, GzhArticle::getLastEditTime);
        } else if ("name".equalsIgnoreCase(sortBy)) {
            w.orderBy(true, asc, GzhArticle::getNamePinyin).orderBy(true, asc, GzhArticle::getName);
        } else {
            w.orderBy(true, asc, GzhArticle::getCreateTime);
        }
        Page<GzhArticle> p = new Page<>(page, size);
        return articleRepository.page(p, w);
    }

    @Override
    public boolean restoreFromRecycle(Long userId, List<Long> ids) {
        // ids 为文章ID集合
        boolean ok = articleRepository.update(new LambdaUpdateWrapper<GzhArticle>()
                .in(GzhArticle::getId, ids)
                .eq(GzhArticle::getUserId, userId)
                .eq(GzhArticle::getDeleted, 1)
                .set(GzhArticle::getDeleted, 0)
                .set(GzhArticle::getDeleteTime, null));
        if (ok) {
            // 从回收站移除
            recycleRepository.remove(new LambdaQueryWrapper<RecycleBinItem>()
                    .eq(RecycleBinItem::getUserId, userId)
                    .in(RecycleBinItem::getFileId, ids)
                    .eq(RecycleBinItem::getFileType, RecycleFileType.WECHAT_ARTICLE.name()));
        }
        return ok;
    }

    @Override
    public GzhArticle getById(Long id) {
        return articleRepository.getById(id);
    }

}


