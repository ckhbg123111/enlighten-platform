package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    public boolean markSuccess(Long id, String originalText, String generatedText) {
        return repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getId, id)
                .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.SUCCESS.name())
                .set(MediaConvertRecordV2::getOriginalText, originalText)
                .set(MediaConvertRecordV2::getGeneratedText, generatedText)
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
}


