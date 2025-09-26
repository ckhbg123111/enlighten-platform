package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.MediaConvertRecordV2;
import com.zhongjia.biz.enums.MediaConvertStatus;
import com.zhongjia.biz.mapper.MediaConvertRecordV2Mapper;
import com.zhongjia.biz.repository.MediaConvertRecordV2Repository;
import com.zhongjia.biz.service.MediaConvertRecordV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MediaConvertRecordV2ServiceImpl implements MediaConvertRecordV2Service {

    @Autowired
    private MediaConvertRecordV2Repository repository;
    @Autowired
    private MediaConvertRecordV2Mapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertProcessingRecord(Long userId, Long externalId, String platform) {
        LocalDateTime now = LocalDateTime.now();

        MediaConvertRecordV2 po = new MediaConvertRecordV2();
        po.setUserId(userId);
        po.setExternalId(externalId);
        po.setPlatform(platform);
        po.setStatus(MediaConvertStatus.PROCESSING.name());
        po.setDeleted(0);
        po.setCreateTime(now);
        po.setUpdateTime(now);
        int affected = mapper.insertProcessing(po);
        if (affected <= 0 || po.getId() == null) {
            throw new IllegalStateException("insert 媒体转换记录失败");
        }
        return po.getId();
    }

    @Override
    public boolean markSuccess(Long id, String originalText, String generatedText, String title) {
        return repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.SUCCESS.name())
                .set(MediaConvertRecordV2::getOriginalText, originalText)
                .set(MediaConvertRecordV2::getGeneratedText, generatedText)
                .set(MediaConvertRecordV2::getTitle, title)
                .set(MediaConvertRecordV2::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public boolean markFailed(Long id, String originalText) {
        return repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.FAILED.name())
                .set(MediaConvertRecordV2::getOriginalText, originalText)
                .set(MediaConvertRecordV2::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public boolean markInterrupted(Long id, String originalText) {
        return repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.INTERRUPTED.name())
                .set(MediaConvertRecordV2::getOriginalText, originalText)
                .set(MediaConvertRecordV2::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public SoftDeleteResult softDeleteById(Long userId, Long id) {
        MediaConvertRecordV2 exist = repository.getById(id);
        if (exist == null) {
            return SoftDeleteResult.NOT_FOUND;
        }
        if (!exist.getUserId().equals(userId)) {
            return SoftDeleteResult.FORBIDDEN;
        }
        if (exist.getDeleted() != null && exist.getDeleted() == 1) {
            return SoftDeleteResult.ALREADY_DELETED;
        }
        boolean ok = repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .eq(MediaConvertRecordV2::getUserId, userId)
                .set(MediaConvertRecordV2::getDeleted, 1)
                .set(MediaConvertRecordV2::getDeleteTime, LocalDateTime.now())
                .set(MediaConvertRecordV2::getUpdateTime, LocalDateTime.now()));
        return ok ? SoftDeleteResult.SUCCESS : SoftDeleteResult.FAILED;
    }

    @Override
    public Page<MediaConvertRecordV2> pageRecords(Long userId, String platform, java.util.List<com.zhongjia.biz.enums.MediaConvertStatus> statuses, int page, int size) {
        LambdaQueryWrapper<MediaConvertRecordV2> qw = new LambdaQueryWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getUserId, userId)
                .eq(MediaConvertRecordV2::getDeleted, 0)
                .orderByDesc(MediaConvertRecordV2::getUpdateTime);
        if (platform != null && !platform.isEmpty()) {
            qw.eq(MediaConvertRecordV2::getPlatform, platform);
        }
        if (statuses != null && !statuses.isEmpty()) {
            java.util.List<String> statusNames = statuses.stream().map(Enum::name).toList();
            qw.in(MediaConvertRecordV2::getStatus, statusNames);
        }
        Page<MediaConvertRecordV2> p = new Page<>(page, size);
        return repository.page(p, qw);
    }

    @Override
    public MediaConvertRecordV2 getById(Long id) {
        return repository.getById(id);
    }
}


