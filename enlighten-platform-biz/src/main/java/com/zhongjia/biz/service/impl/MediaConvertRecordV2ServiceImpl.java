package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhongjia.biz.entity.MediaConvertRecordV2;
import com.zhongjia.biz.enums.MediaConvertStatus;
import com.zhongjia.biz.repository.MediaConvertRecordV2Repository;
import com.zhongjia.biz.service.MediaConvertRecordV2Service;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MediaConvertRecordV2ServiceImpl implements MediaConvertRecordV2Service {

    private final MediaConvertRecordV2Repository repository;

    public MediaConvertRecordV2ServiceImpl(MediaConvertRecordV2Repository repository) {
        this.repository = repository;
    }

    @Override
    public MediaConvertRecordV2 insertProcessing(Long userId, Long externalId, String platform) {
        MediaConvertRecordV2 po = new MediaConvertRecordV2();
        po.setUserId(userId);
        po.setExternalId(externalId);
        po.setPlatform(platform);
        po.setStatus(MediaConvertStatus.PROCESSING.name());
        po.setDeleted(0);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        boolean ok = repository.save(po);
        if (!ok) {
            throw new IllegalStateException("插入媒体转换记录失败");
        }
        return po;
    }

    @Override
    public boolean markSuccess(Long id) {
        return repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.SUCCESS.name())
                .set(MediaConvertRecordV2::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public boolean markFailed(Long id) {
        return repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.FAILED.name())
                .set(MediaConvertRecordV2::getUpdateTime, LocalDateTime.now()));
    }
}


