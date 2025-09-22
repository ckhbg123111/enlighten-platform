package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhongjia.biz.entity.MediaConvertRecordV2;
import com.zhongjia.biz.enums.MediaConvertStatus;
import com.zhongjia.biz.repository.MediaConvertRecordV2Repository;
import com.zhongjia.biz.service.MediaConvertRecordV2Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MediaConvertRecordV2ServiceImpl implements MediaConvertRecordV2Service {

    private final MediaConvertRecordV2Repository repository;

    public MediaConvertRecordV2ServiceImpl(MediaConvertRecordV2Repository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaConvertRecordV2 insertOrUpdateProcessing(Long userId, Long externalId, String platform) {
        LocalDateTime now = LocalDateTime.now();

        // 1) 尝试插入
        MediaConvertRecordV2 po = new MediaConvertRecordV2();
        po.setUserId(userId);
        po.setExternalId(externalId);
        po.setPlatform(platform);
        po.setStatus(MediaConvertStatus.PROCESSING.name());
        po.setDeleted(0);
        po.setCreateTime(now);
        po.setUpdateTime(now);
        try {
            boolean ok = repository.save(po);
            if (!ok) {
                throw new IllegalStateException("插入媒体转换记录失败");
            }
            return po;
        } catch (DuplicateKeyException dup) {
            // 2) 若唯一键冲突，则更新为 PROCESSING，并刷新更新时间
            boolean ok = repository.update(new LambdaUpdateWrapper<MediaConvertRecordV2>()
                    .eq(MediaConvertRecordV2::getUserId, userId)
                    .eq(MediaConvertRecordV2::getExternalId, externalId)
                    .eq(MediaConvertRecordV2::getPlatform, platform)
                    .eq(MediaConvertRecordV2::getDeleted, 0)
                    .set(MediaConvertRecordV2::getStatus, MediaConvertStatus.PROCESSING.name())
                    .set(MediaConvertRecordV2::getUpdateTime, now)
            );
            if (!ok) {
                throw new IllegalStateException("并发更新媒体转换记录失败");
            }
            // 3) 查询并返回最新一条
            LambdaQueryWrapper<MediaConvertRecordV2> qw = new LambdaQueryWrapper<MediaConvertRecordV2>()
                    .eq(MediaConvertRecordV2::getUserId, userId)
                    .eq(MediaConvertRecordV2::getExternalId, externalId)
                    .eq(MediaConvertRecordV2::getPlatform, platform)
                    .eq(MediaConvertRecordV2::getDeleted, 0)
                    .orderByDesc(MediaConvertRecordV2::getUpdateTime)
                    .last("limit 1");
            MediaConvertRecordV2 exist = repository.getOne(qw);
            if (exist == null) {
                throw new IllegalStateException("并发场景下未找到媒体转换记录");
            }
            return exist;
        }
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


