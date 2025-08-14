package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.DraftMediaMap;
import com.zhongjia.biz.entity.DraftPO;
import com.zhongjia.biz.repository.DraftMediaMapRepository;
import com.zhongjia.biz.repository.DraftRepository;
import com.zhongjia.biz.service.DraftService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DraftServiceImpl implements DraftService {

    private final DraftRepository draftRepository;
    private final DraftMediaMapRepository draftMediaMapRepository;

    public DraftServiceImpl(DraftRepository draftRepository, DraftMediaMapRepository draftMediaMapRepository) {
        this.draftRepository = draftRepository;
        this.draftMediaMapRepository = draftMediaMapRepository;
    }

    @Override
    public Long saveOrUpdateByEssayCode(Long userId, Long tenantId, String essayCode, String title, String content, List<String> mediaCodeList, String tags) {
        LocalDateTime now = LocalDateTime.now();

        DraftPO exist = draftRepository.getOne(new LambdaQueryWrapper<DraftPO>()
                .eq(DraftPO::getUserId, userId)
                .eq(DraftPO::getEssayCode, essayCode)
                .last("limit 1"));

        Long draftId;
        if (exist != null) {
            exist.setTitle(title);
            exist.setContent(content);
            if (mediaCodeList != null) {
                exist.setMediaCodeListString(mediaCodeList.isEmpty() ? null : String.join(",", mediaCodeList));
            }
            exist.setTags(tags);
            exist.setUpdateTime(now);
            draftRepository.updateById(exist);
            draftId = exist.getId();
        } else {
            DraftPO draftPO = new DraftPO()
                    .setUserId(userId)
                    .setTenantId(tenantId)
                    .setEssayCode(essayCode)
                    .setTitle(title)
                    .setContent(content)
                    .setMediaCodeListString(mediaCodeList == null || mediaCodeList.isEmpty() ? null : String.join(",", mediaCodeList))
                    .setTags(tags)
                    .setDeleted(0)
                    .setCreateTime(now)
                    .setUpdateTime(now);
            draftRepository.save(draftPO);
            draftId = draftPO.getId();
        }

        // 维护 草稿-媒体 映射。规则：
        // - mediaCodeList == null 不改动
        // - mediaCodeList 为空：清空（软删除）
        // - 否则：先软删除旧映射，再按当前列表重建
        if (mediaCodeList != null) {
            // 软删除旧映射
            draftMediaMapRepository.update(new DraftMediaMap().setDeleted(1).setDeleteTime(now).setUpdateTime(now),
                    new LambdaUpdateWrapper<DraftMediaMap>()
                            .eq(DraftMediaMap::getUserId, userId)
                            .eq(DraftMediaMap::getDraftId, draftId)
                            .eq(DraftMediaMap::getDeleted, 0));

            if (!mediaCodeList.isEmpty()) {
                Set<String> distinctCodes = new HashSet<>(mediaCodeList);
                for (String code : distinctCodes) {
                    DraftMediaMap map = new DraftMediaMap()
                            .setUserId(userId)
                            .setTenantId(tenantId)
                            .setDraftId(draftId)
                            .setMediaCode(code)
                            .setTag(tags)
                            .setDeleted(0)
                            .setCreateTime(now)
                            .setUpdateTime(now);
                    draftMediaMapRepository.save(map);
                }
            }
        }

        return draftId;
    }

    @Override
    public Page<DraftPO> pageByUser(Long userId, int page, int pageSize) {
        Page<DraftPO> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<DraftPO> w = new LambdaQueryWrapper<DraftPO>()
                .eq(DraftPO::getUserId, userId)
                .eq(DraftPO::getDeleted, 0)
                .orderByDesc(DraftPO::getUpdateTime);
        return draftRepository.page(p, w);
    }

    @Override
    public boolean editDraft(Long userId, Long draftId, String title, String content) {
        DraftPO draftPO = draftRepository.getById(draftId);
        if (draftPO == null || !draftPO.getUserId().equals(userId)) return false;
        if (title != null) draftPO.setTitle(title);
        if (content != null) draftPO.setContent(content);
        draftPO.setUpdateTime(LocalDateTime.now());
        return draftRepository.updateById(draftPO);
    }

    @Override
    public boolean softDelete(Long userId, Long draftId) {
        DraftPO draftPO = draftRepository.getById(draftId);
        if (draftPO == null || !draftPO.getUserId().equals(userId)) return false;
        draftPO.setDeleted(1);
        draftPO.setDeleteTime(LocalDateTime.now());
        draftPO.setUpdateTime(LocalDateTime.now());
        boolean ok = draftRepository.updateById(draftPO);
        if (ok) {
            LocalDateTime now = LocalDateTime.now();
            draftMediaMapRepository.update(new DraftMediaMap().setDeleted(1).setDeleteTime(now).setUpdateTime(now),
                    new LambdaUpdateWrapper<DraftMediaMap>()
                            .eq(DraftMediaMap::getUserId, userId)
                            .eq(DraftMediaMap::getDraftId, draftId)
                            .eq(DraftMediaMap::getDeleted, 0));
        }
        return ok;
    }

    @Override
    public boolean restore(Long userId, Long draftId) {
        DraftPO draftPO = draftRepository.getById(draftId);
        if (draftPO == null || !draftPO.getUserId().equals(userId)) return false;
        draftPO.setDeleted(0);
        draftPO.setDeleteTime(null);
        draftPO.setUpdateTime(LocalDateTime.now());
        boolean ok = draftRepository.updateById(draftPO);
        if (ok) {
            LocalDateTime now = LocalDateTime.now();
            draftMediaMapRepository.update(new DraftMediaMap().setDeleted(0).setDeleteTime(null).setUpdateTime(now),
                    new LambdaUpdateWrapper<DraftMediaMap>()
                            .eq(DraftMediaMap::getUserId, userId)
                            .eq(DraftMediaMap::getDraftId, draftId));
        }
        return ok;
    }
}



