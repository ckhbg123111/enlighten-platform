package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.mapper.MediaConvertRecordMapper;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MediaConvertRecordRepositoryImpl extends ServiceImpl<MediaConvertRecordMapper, MediaConvertRecord> implements MediaConvertRecordRepository {
}


