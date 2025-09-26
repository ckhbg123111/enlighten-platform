package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.VideoRecordTemp;
import com.zhongjia.biz.mapper.VideoRecordTempMapper;
import com.zhongjia.biz.repository.VideoRecordTempRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VideoRecordTempRepositoryImpl extends ServiceImpl<VideoRecordTempMapper, VideoRecordTemp>
        implements VideoRecordTempRepository {
}


