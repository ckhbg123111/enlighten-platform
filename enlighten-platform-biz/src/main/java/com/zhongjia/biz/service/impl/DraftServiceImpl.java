package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.DraftPO;
import com.zhongjia.biz.repository.DraftRepository;
import com.zhongjia.biz.service.DraftService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DraftServiceImpl implements DraftService {

    private final DraftRepository draftRepository;

    public DraftServiceImpl(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }
    @Override
    public Long saveOrUpdateByEssayCode(Long userId, Long tenantId, String essayCode, String title, String content, List<String> mediaCodeList) {
        DraftPO exist = draftRepository.getOne(new LambdaQueryWrapper<DraftPO>()
            .eq(DraftPO::getUserId, userId)
            .eq(DraftPO::getEssayCode, essayCode)
            .last("limit 1"));
        if (exist == null) {
            DraftPO draftPO = new DraftPO()
                .setUserId(userId)
                .setTenantId(tenantId)
                .setEssayCode(essayCode)
                .setTitle(title)
                .setContent(content)
                .setMediaIdListString(mediaCodeList == null ? null : String.join(",", mediaCodeList))
                .setDeleted(0)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());
            draftRepository.save(draftPO);
            return draftPO.getId();
        } else {
            exist.setTitle(title);
            exist.setContent(content);
            exist.setMediaIdListString(mediaCodeList == null ? null : String.join(",", mediaCodeList));
            exist.setDeleted(0);
            exist.setUpdateTime(LocalDateTime.now());
            draftRepository.updateById(exist);
            return exist.getId();
        }
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
        return draftRepository.updateById(draftPO);
    }

    @Override
    public boolean restore(Long userId, Long draftId) {
        DraftPO draftPO = draftRepository.getById(draftId);
        if (draftPO == null || !draftPO.getUserId().equals(userId)) return false;
        draftPO.setDeleted(0);
        draftPO.setDeleteTime(null);
        draftPO.setUpdateTime(LocalDateTime.now());
        return draftRepository.updateById(draftPO);
    }
}



